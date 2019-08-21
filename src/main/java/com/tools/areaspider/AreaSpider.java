package com.tools.areaspider;

import com.tools.areaspider.domain.Area;
import com.tools.areaspider.utils.RegexUtils;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.stream.Collectors;

public final class AreaSpider {
    public List<Area> getAreaData(){

    }

    // 最新一年统计 数据
    public String getLatestStatsUrl() {
        Document doc = null;
        try {
            doc = getHtml("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/", 3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> urls = doc.getElementsByClass(className)
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
