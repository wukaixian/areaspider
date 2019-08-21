package com.tools.areaspider.parser;

import com.tools.areaspider.domain.Area;
import com.tools.areaspider.domain.ProxyIpAddrWrapper;
import com.tools.areaspider.utils.RegexUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProvincePageParser extends PageParser {

    public ProvincePageParser(ProxyIpAddrWrapper wrapper) {
        super("provincetr", wrapper);
    }

    @Override
    public List<Area> runParser(String provinceUrl) {
        Document doc = null;
        try {
            provinceUrl = getLatestStatsUrl();
            doc = PageCacheManager.getHtml(provinceUrl, proxyWrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    // 最新一年统计 数据
    private String getLatestStatsUrl() {
        Document doc = PageCacheManager.getHtml("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/", proxyWrapper);
        List<String> urls = doc.getElementsByClass("center_list_contlist")
                .first()
                .getElementsByTag("a")
                .stream()
                .map(x -> x.absUrl("href"))
                .collect(Collectors.toList());

        Integer latestYear = urls.stream()
                .map(RegexUtils::extractNumber)
                .min(Comparator.naturalOrder()).get();

        for (String url : urls) {
            if (url.contains(latestYear.toString())) {
                return url;
            }
        }

        return null;
    }
}
