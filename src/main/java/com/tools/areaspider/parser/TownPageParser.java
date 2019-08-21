package com.tools.areaspider.parser;

import com.tools.areaspider.domain.ProxyIpAddrWrapper;

/**
 * 镇
 */
public class TownPageParser extends PageParser {
    public TownPageParser(ProxyIpAddrWrapper wrapper) {
        super("towntr", wrapper);
    }
}
