package com.tools.areaspider;

import com.tools.areaspider.domain.ProxyIpAddr;
import com.tools.areaspider.utils.RegexUtils;
import org.jsoup.Jsoup;
import org.springframework.util.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * random generate ip manager
 * weight:使用一次减1，失败减2，权重范围：[0,1000]，新加入的ip权重为10
 */
public final class ProxyIpManager {

    // 请求可用ip数
    private static final int AVAILABLE_PROXYIP_COUNT = 10;

    // ip white list path
    private static final Path proxyIpWhitelistPath = Paths.get(System.getProperty("user.dir"), "cache", "proxy_ip_whitelist.txt");

    private static List<ProxyIpAddr> proxyIps;

    static {
        initProxyIp();
    }

    private static void initProxyIp() {
        proxyIps = new ArrayList<>();
        loadProxyIpOffline();

        if (proxyIps.size() < AVAILABLE_PROXYIP_COUNT) {
            loadProxyIpOnline();

            writeToWhitelist();
        }
    }

    /**
     * 从本地加载代理ip
     */
    private static synchronized void loadProxyIpOffline() {
        try {
            if (!Files.exists(proxyIpWhitelistPath)) {
                Files.createFile(proxyIpWhitelistPath);
                return;
            }

            List<String> lines = Files.readAllLines(proxyIpWhitelistPath);
            if (lines.size() == 0)
                return;

            for (String line : lines) {
                if (StringUtils.isEmpty(line)) {
                    continue;
                }

                //style->127.0.0.1 80 10
                String[] entries = line.trim().split("\\s+");
                proxyIps.add(new ProxyIpAddr(entries[0], Integer.valueOf(entries[1]), Integer.valueOf(entries[2])));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从线上加载代理ip
     */
    private static synchronized void loadProxyIpOnline() {
        try {
            URL url = new URL("http://www.66ip.cn/mo.php?sxb=%B1%B1%BE%A9&tqsl=20");
            String text = Jsoup.parse(url, 5000).outerHtml();

            List<String> extractIps = RegexUtils.extractIpAddress(text);
            for (String ip : extractIps) {
                String[] items = ip.split(":");
                if (checkIpIsReachable(items[0])) {
                    proxyIps.add(new ProxyIpAddr(items[0], Integer.valueOf(items[1])));
                }
            }

            if (proxyIps.size() < AVAILABLE_PROXYIP_COUNT) {
                loadProxyIpOnline();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 按权重对ip地址排序,权重大的ip优先使用
     */
    private static synchronized List<ProxyIpAddr> sortedByWeight() {
        return proxyIps.stream()
                .sorted((a, b) -> a.getWeight() >= b.getWeight() ? 1 : 0)
                .collect(Collectors.toList());
    }

    /**
     * get proxy ip
     */
    public static synchronized ProxyIpAddr getProxyIp() {
        // 可用ip降低一半后，重新加载补充
        if (proxyIps.size() < AVAILABLE_PROXYIP_COUNT / 2) {
            loadProxyIpOnline();
        }

        ProxyIpAddr ip = sortedByWeight().get(0);
        ip.decrementWeight();

        return ip;
    }

    /**
     * 更新proxy到白名单文件
     */
    private static synchronized void writeToWhitelist() {
        try {

            List<String> list = proxyIps.stream()
                    .map(x -> String.format("%s %s %s", x.getIp(), x.getPort(), x.getWeight()))
                    .collect(Collectors.toList());

            byte[] data = String.join(System.lineSeparator(), list).getBytes();

            FileOutputStream fs = new FileOutputStream(proxyIpWhitelistPath.toString());
            fs.write(data);
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * check ip address is reachable
     */
    private static boolean checkIpIsReachable(String ip) {
        try {
            return InetAddress.getByName(ip).isReachable(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * ip已被block
    * */
    public static synchronized void blockIp(ProxyIpAddr ip) {
        if (proxyIps.contains(ip)) {
            proxyIps.remove(ip);
        }

        writeToWhitelist();
    }

    /**
     * 标记失败
     * */
    public static synchronized void failure(ProxyIpAddr ip) {
        ip.failure();

        if (ip.getWeight() == 0) {
            if (proxyIps.contains(ip)) {
                proxyIps.remove(ip);
            }
            writeToWhitelist();
        }
    }
}
