package com.tools.areaspider.parser;

import com.tools.areaspider.CharsetManager;
import com.tools.areaspider.ProxyIpManager;
import com.tools.areaspider.UserAgentManager;
import com.tools.areaspider.domain.Area;
import com.tools.areaspider.domain.ProxyIpAddr;
import com.tools.areaspider.utils.RegexUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PageParser {


    protected final String className;

    public PageParser(String className) {
        this.className = className;
    }

    public List<Area> Parser() {
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

    // load page html



}
