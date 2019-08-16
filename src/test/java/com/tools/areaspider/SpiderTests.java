package com.tools.areaspider;

import com.tools.areaspider.utils.Spider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

public class SpiderTests {

    @Test
    public void getHtmlTest() {
        String html = Spider.getHtml("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2018/index.html", null);

        System.out.println(html);
    }

    @Test
    public void jsoupTest() throws Exception {
        Document doc = Jsoup.connect("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/").get();
        Element ul = doc.body().getElementsByClass("center_list_contlist").first();
        Elements links = ul.getElementsByTag("a");
        for (Element link : links) {
            System.out.println(link.attr("href"));
        }
    }
}
