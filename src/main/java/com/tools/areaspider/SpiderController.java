package com.tools.areaspider;

import com.tools.areaspider.domain.Area;
import com.tools.areaspider.domain.ProxyIpAddr;
import com.tools.areaspider.parser.LatestYearStatsPageParser;
import com.tools.areaspider.parser.PageParser;
import com.tools.areaspider.utils.Linked;
import com.tools.areaspider.utils.RegexUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@RestController
public class SpiderController {

    // request html page retry times when request fail

    Linked<PageParser> linked = new Linked<>(new LatestYearStatsPageParser());

    // proxy flag
    private final static AtomicBoolean isUserProxy = new AtomicBoolean(false);

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
        if (levelClassName == classList.get(1)) {
            System.out.println(parent.getName());
        }

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
                    markErrorFile(url);
                    throw new Exception("document exception");
                }

                area.setCode(code);
                area.setName(name);
                list.add(area);
            }
        }

        if (list.size() == 0) {
            markErrorFile(url);
        }
        return list;
    }

    private static void markErrorFile(String url) {
        Path path = getEncodeFilePath(url);
        if (Files.exists(path)) {
            String name = "error-" + base64EncodeName(url);
            path.toFile().renameTo(new File(Paths.get(cacheRootPath.toString(), name).toString()));
        }
    }

    private static Path getEncodeFilePath(String url) {
        return Paths.get(cacheRootPath.toString(), base64EncodeName(url));
    }

    private static String base64EncodeName(String url) {
        return Base64.getEncoder().encodeToString(url.getBytes()) + ".html";
    }



}
