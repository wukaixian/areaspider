package com.tools.areaspider;

import com.tools.areaspider.domain.Area;
import com.tools.areaspider.utils.RegexUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SpiderController {

    // request html page retry times when request fail
    private static final int RETRY_TIMES = 5;
    private static final List<String> classList = new ArrayList<String>() {{
        add("provincetr");
        add("citytr");
        add("countytr");
        add("towntr");
        add("villagetr");
    }};

    /*
     * area spider startup
     * */
    @RequestMapping("/")
    public ResponseEntity<?> runSpider() {
        CharsetManager.init();

        List<Area> areas = getAreaData();

        return ResponseEntity.ok(areas);
    }

    public List<Area> getAreaData() {
        String provinceUrl = getLatestStatsUrl();
        List<Area> provinces = provinceParser(provinceUrl, classList.get(0));

        // 并行各个省爬数据
        provinces.parallelStream()
                .forEach(p -> detectChildren(p, classList.get(1)));

        return provinces;
    }

    // 下一级地区数据加载
    private void detectChildren(Area parent, String levelClassName) {
        try {
            List<Area> children = pageParser(parent.getUrl(), levelClassName);
            for (Area child : children) {
                if (child.getUrl() != null) {
                    String childClassName = classList.get(classList.indexOf(levelClassName) + 1);
                    detectChildren(child, childClassName);
                }
            }

            parent.setChildren(children);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 省份解析
    private List<Area> provinceParser(String provinceUrl, String className) {
        Document doc = getHtml(provinceUrl, RETRY_TIMES);
        if (doc == null) {
            return null;
        }


        Elements elements = doc.getElementsByClass(className);

        List<Area> provinces = new ArrayList<>();
        for (Element element : elements) {
            for (Element a : element.getElementsByTag("a")) {
                String url = a.absUrl("href");
                Area province = new Area();
                province.setName(a.text().trim());
                province.setUrl(url);

                provinces.add(province);
            }
        }

        return provinces;
    }

    // 页面解析器
    private static List<Area> pageParser(String url, String className) throws Exception {
        Document doc = null;
        try {
            doc = getHtml(url, 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Elements elements = doc.getElementsByClass(className);

        List<Area> list = new ArrayList<>();
        for (Element element : elements) {
            Area area = new Area();
            String name, code;
            Elements tags = element.getElementsByTag("a");
            if (tags.size() > 0) {
                try {
                    Element link = element.getElementsByTag("a").get(0);
                    code = link.text().trim();
                    name = element.getElementsByTag("a").get(1).text().trim();

                    area.setCode(code);
                    area.setName(name);

                    area.setUrl(link.absUrl("href"));
                    list.add(area);
                } catch (Exception e) {
                    System.out.println(url);
                    throw e;
                }
            } else {
                tags = element.getElementsByTag("td");
                if (tags.size() == 2) {
                    code = tags.get(0).text().trim();
                    name = tags.get(1).text().trim();
                } else if (tags.size() == 3) {
                    code = tags.get(0).text().trim();
                    name = tags.get(2).text().trim();
                } else {
                    throw new Exception("document exception");
                }

                area.setCode(code);
                area.setName(name);
            }
        }

        return list;
    }

    // load page html
    private static Document getHtml(String url, int retry) {
        String base64fileName = Base64.getEncoder().encodeToString(url.getBytes());
        String root = System.getProperty("user.dir") + "\\cache";
        if (!Files.exists(Paths.get(root))) {
            try {
                Files.createDirectories(Paths.get(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Path fullName = Paths.get(root, base64fileName + ".html");

        Document doc = null;
        // 从本地缓存读html
        if (Files.exists(fullName)) {
            try {
                doc = Jsoup.parse(fullName.toFile(), CharsetManager.getCharset(base64fileName));
                doc.setBaseUri(url);

                return doc;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            doc = Jsoup.connect(url).timeout(5000).get();
            if (doc.title().equals("访问验证")) {
                return null;
            }
            // 保存到本地
            String charsetName = doc.charset().name();
            CharsetManager.saveCharset(base64fileName, charsetName);

            byte[] data = doc.outerHtml().getBytes(charsetName);
            Files.write(fullName, data);

            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (retry > 0) {
            return getHtml(url, --retry);
        }

        return doc;
    }

    // 最新一年统计 数据
    public static String getLatestStatsUrl() {
        Document doc = null;
        try {
            doc = getHtml("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/", 3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> urls = doc.getElementsByClass("center_list_contlist")
                .first()
                .getElementsByTag("a")
                .stream()
                .map(x -> x.attr("href"))
                .collect(Collectors.toList());

        Integer latestYear = urls.stream()
                .map(RegexUtils::extractNumber)
                .max(Math::max).get();

        for (String url : urls) {
            if (url.indexOf(latestYear.toString()) > -1)
                return url;
        }

        return null;
    }
}
