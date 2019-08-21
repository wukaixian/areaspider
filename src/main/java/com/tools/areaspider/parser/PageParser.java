package com.tools.areaspider.parser;

import com.tools.areaspider.domain.Area;
import com.tools.areaspider.domain.ProxyIpAddrWrapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class PageParser {

    protected final String className;
    protected ProxyIpAddrWrapper proxyWrapper;

    public PageParser(String className, ProxyIpAddrWrapper proxy) {
        this.className = className;
        this.proxyWrapper = proxy;
    }

    public List<Area> runParser(String url) {
        Document doc = PageCacheManager.getHtml(url, proxyWrapper);
        if (doc == null)
            return null;

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
                    PageCacheManager.markError(url);
                    continue;
                }

                area.setCode(code);
                area.setName(name);
                list.add(area);
            }
        }

        if (list.size() == 0) {
            PageCacheManager.markError(url);
        }

        return list;
    }
}
