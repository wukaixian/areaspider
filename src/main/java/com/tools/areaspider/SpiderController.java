package com.tools.areaspider;

import com.tools.areaspider.domain.Area;
import com.tools.areaspider.utils.Spider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class SpiderController {

    @RequestMapping("/")
    public ResponseEntity<?> runSpider() {
        return null;
    }

    public static String getLatestStatsUrl() {
        Document doc = Jsoup.connect("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/").get();
        String[] urls = doc.getElementsByClass("cont_tit")
                .stream()
                .map(x -> {
                    return x.attr("href");
                })
                .toArray(String[]::new);

        Arrays.stream(urls);
        
        return null;
    }

    public List<Area> getProvince() {
        String provinceUrl = getLatestAreaUrl();
        String html = Spider.getHtml(provinceUrl, null);

        // 各省份地址
        List<String> provinces = Spider.extractValues(html, "\\d+\\.html.+?<\\/a>");
        for (String province : provinces) {
            String url = Spider.extractAttributeValue(province, "href");
            String name = Spider.extractAttributeValue(province, "");
        }

        return null;
    }

    // 获取最新一年的区域数据
    public String getLatestAreaUrl() {
        String html = Spider.getHtml("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/", null);

        // 提取所有年份的url
        List<String> urls = Spider.extractValues(html, "\\/tjsj\\/tjbz\\/tjyqhdmhcxhfdm\\/(\\d{4})\\/index.html");

        // 提取最后一年url
        Integer latestYear = urls.stream().map(this::extractYear).max(Math::max).get();
        for (String url : urls) {
            if (url.indexOf(latestYear.toString()) > -1)
                return "http://www.stats.gov.cn" + url;
        }

        return null;
    }

    private int extractYear(String input) {
        String year = Spider.extractValue(input, "\\d+");

        return Integer.valueOf(year);
    }
}
