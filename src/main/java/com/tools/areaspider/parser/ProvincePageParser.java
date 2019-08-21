package com.tools.areaspider.parser;

import com.tools.areaspider.domain.Area;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class ProvincePageParser extends PageParser {

    public ProvincePageParser() {
        super("provincetr");
    }


    @Override
    public List<Area> Parser() {
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
}
