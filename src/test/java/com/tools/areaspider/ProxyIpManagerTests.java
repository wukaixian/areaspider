package com.tools.areaspider;

import com.tools.areaspider.domain.ProxyIpAddr;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.properties.PropertyMapping;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProxyIpManagerTests {
    @Test
    public void getIpTest() {
        ProxyIpAddr ip = ProxyIpManager.getProxyIp();
    }

    @Test
    public void formatTest() {
        String s = String.format("%s %s", "name", "age");

    }

    @Test
    public void proxyIpTest() {

        for (int i = 0; i < 10; i++) {
            ProxyIpAddr proxyIp = ProxyIpManager.getProxyIp();
            try {
                Jsoup.connect("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2018/index.html")
                        .proxy(proxyIp.getIp(), proxyIp.getPort())
                        .timeout(5000)
                        .get();
            } catch (SocketTimeoutException e) {
                ProxyIpManager.blockIp(proxyIp);
                e.printStackTrace();
            } catch (Exception e) {
                ProxyIpManager.failure(proxyIp);
                e.printStackTrace();
            }

            System.out.println("finish");
        }
    }

    @Test
    public void name() {
        try {
            Jsoup.connect("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2018/index.html")
                    .proxy("192.168.1.34", 80)
                    .timeout(5000)
                    .get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void piorTest() {
        for (int i = 0; i < 100; i++) {
            ProxyIpAddr ip = ProxyIpManager.getProxyIp();
            System.out.println(ip.getIp() + ":" + ip.getWeight());
        }

        ProxyIpManager.blockIp(ProxyIpManager.getProxyIp());
    }

    @Test
    public void sortedTest() {
        List<ProxyIpAddr> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(new ProxyIpAddr("192.168.0." + i, i));
        }

        list.get(0).decrementWeight();
        System.out.println(list.get(0).getWeight());

        Collections.sort(list, (x, y) -> {
            return x.getWeight() > y.getWeight() ? -1 : 1;
        });

        System.out.println(list.get(0).getWeight());
    }
}
