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
import org.springframework.web.server.adapter.AbstractReactiveWebInitializer;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class SpiderController {

    @RequestMapping("/")
    public ResponseEntity<?> runSpider() throws Exception {
        List<Area> areas = getCity();
        return ResponseEntity.ok(areas);
    }

    public static List<Area> getCity() throws Exception {
        List<Area> province = getProvince();

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

                    String childUrl = url.replaceAll("(\\d+|\\w+)\\.html", link.attr("href"));
                    area.setUrl(childUrl);
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
            /*_trs_uv=jzcmw6vf_6_bkwx; __utmz=207252561.1565870812.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); AD_RS_COOKIE=20082855; __utma=207252561.2068155420.1565870812.1565870812.1566132421.2; __utmc=207252561; __utmt=1; __utmb=207252561.1.10.1566132421; wzws_cid=303c41b64c98b347518cc7a1a380d9867ee6cc7313f7a47d16f49702e0d246ff31ca5ccf3aefed47ffe234f54fa1121acfe63e10f92eb887a542a81a33e1b404; _trs_ua_s_1=jzgytnre_6_kuaj
             */
            String cookie = "_trs_uv=jzcmw6vf_6_bkwx; __utmz=207252561.1565870812.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); AD_RS_COOKIE=20082855; __utma=207252561.2068155420.1565870812.1565870812.1566132421.2; __utmc=207252561; __utmt=1; __utmb=207252561.1.10.1566132421; wzws_cid=303c41b64c98b347518cc7a1a380d9867ee6cc7313f7a47d16f49702e0d246ff31ca5ccf3aefed47ffe234f54fa1121acfe63e10f92eb887a542a81a33e1b404; _trs_ua_s_1=jzgytnre_6_kuaj";
            Map<String, String> cookies = new HashMap<>();
            for (String item : cookie.split(";")) {
                cookies.put(item.split("=")[0], item.split("=")[1]);
            }

            doc = Jsoup.connect(url).cookies(cookies).get();
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (retry > 0) {
            return getHtml(url, --retry);
        }

        return doc;
    }

    // 省份数据
    public static List<Area> getProvince() {
        String provinceUrl = getLatestStatsUrl();
        Document doc = null;
        try {
            doc = getHtml(provinceUrl, 3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Elements elements = doc.getElementsByClass("provincetr");

        List<Area> province = new ArrayList<>();
        for (Element element : elements) {
            for (Element a : element.getElementsByTag("a")) {
                String url = provinceUrl.replaceAll("\\w+\\.html", a.attr("href"));

                Area area = new Area();
                area.setName(a.text().trim());
                area.setUrl(url);

                province.add(area);
            }
        }

        return province;
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
