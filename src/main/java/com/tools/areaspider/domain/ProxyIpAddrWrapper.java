package com.tools.areaspider.domain;

// 代理ip包装器
public class ProxyIpAddrWrapper {
    private ProxyIpAddr proxyIpAddr;

    public ProxyIpAddrWrapper(ProxyIpAddr proxyIpAddr) {
        this.proxyIpAddr = proxyIpAddr;
    }

    public ProxyIpAddr getProxyIpAddr() {
        return proxyIpAddr;
    }

    public void setProxyIpAddr(ProxyIpAddr proxyIpAddr) {
        this.proxyIpAddr = proxyIpAddr;
    }
}
