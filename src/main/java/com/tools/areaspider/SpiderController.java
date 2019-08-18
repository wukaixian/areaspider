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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class SpiderController {

    // request html page retry times when request fail
    private static final int RETRY_TIMES = 5;


    /*
     * area spider startup
     * */
    @RequestMapping("/")
    public ResponseEntity<?> runSpider() throws Exception {
        List<Area> areas = getAreaData();

        return ResponseEntity.ok(areas);
    }

    public List<Area> getAreaData() throws Exception {
        String provinceUrl = getLatestStatsUrl();
        List<Area> provinces = provinceParser(provinceUrl);

        provinces.parallelStream()
                .forEach(x -> getChildren(x, "citytr"));

        for (int i = 0; i < 5; i++) {
            provinces.get(i).getChildren()
                    .parallelStream()
                    .forEach(x -> getChildren(x, "countytr"));
        }

        return provinces;
    }

    private void getChildren(Area parent, String levelClassName) {
        try {
            List<Area> children = pageParser(parent.getUrl(), levelClassName);
            parent.setChildren(children);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Area> provinceParser(String provinceUrl) {
        Document doc = getHtml(provinceUrl, RETRY_TIMES);
        if (doc == null) {
            return null;
        }


        Elements elements = doc.getElementsByClass("provincetr");

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

    public static List<Area> getCity() throws Exception {
        List<Area> province = new ArrayList<>();// getProvince();

        List<Thread> threads = new ArrayList<>();
        for (Area area : province) {
            Area local = area;
            Thread t = new Thread(() -> {
                List<Area> children = null;
                try {
                    children = getCitys(local);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                local.setChildren(children);
            });

            t.start();
            threads.add(t);
        }

        for (Thread thread : threads) {
            thread.join();
        }

        return province;
    }

    public static List<Area> getCitys(Area area) throws Exception {
        // city
        List<Area> cityChildren = pageParser(area.getUrl(), "citytr");

        for (Area child : cityChildren) {

            // county
            List<Area> countyChildren = pageParser(child.getUrl(), "countytr");
            for (Area countyChild : countyChildren) {
                // town
                List<Area> townChildren = pageParser(countyChild.getUrl(), "towntr");

                for (Area townChild : townChildren) {
                    // village
                    List<Area> villageChildren = pageParser(townChild.getUrl(), "villagetr");
                    townChild.setChildren(villageChildren);
                }
                countyChild.setChildren(townChildren);
            }
            child.setChildren(countyChildren);
        }

        return cityChildren;
    }

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

    private static Document getHtml(String url, int retry) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
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
