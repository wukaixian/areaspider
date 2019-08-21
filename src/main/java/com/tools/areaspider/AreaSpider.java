package com.tools.areaspider;

import com.tools.areaspider.domain.Area;
import com.tools.areaspider.domain.ProxyIpAddr;
import com.tools.areaspider.domain.ProxyIpAddrWrapper;
import com.tools.areaspider.parser.*;
import com.tools.areaspider.utils.Linked;
import com.tools.areaspider.utils.LinkedUtils;

import java.util.ArrayList;
import java.util.List;

public final class AreaSpider implements Runnable {

    private final Area province;

    // 解析层级链表
    private final Linked<PageParser> levelLinked;

    public AreaSpider(Area province, ProxyIpAddrWrapper wrapper) {
        this.province = province;

        // linked initialize
        List<PageParser> parsers = new ArrayList<PageParser>() {
            {
                add(new CityPageParser(wrapper));
                add(new CountyPageParser(wrapper));
                add(new TownPageParser(wrapper));
                add(new VillagePageParser(wrapper));
            }
        };

        levelLinked = LinkedUtils.toLinked(parsers);
    }

    @Override
    public void run() {
        detectChildren(province, levelLinked);
    }

    // 下一级地区数据加载
    private void detectChildren(Area parent, Linked<PageParser> parser) {
        try {
            List<Area> children = parser.getValue().runParser(parent.getUrl());
            for (Area child : children) {
                if (child.getUrl() != null) {
                    detectChildren(child, parser.next);
                }
            }

            parent.setChildren(children);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
