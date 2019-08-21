package com.tools.areaspider.parser;

import com.tools.areaspider.CharsetManager;
import com.tools.areaspider.ProxyIpManager;
import com.tools.areaspider.UserAgentManager;
import com.tools.areaspider.domain.ProxyIpAddr;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PageCacheManager {

    private static final int DEFAULT_RETRY_TIMES = 5;
    private static final Path cacheRootPath = Paths.get(System.getProperty("user.dir"), "cache");
    private static AtomicBoolean atomicCheck = new AtomicBoolean(false);

    // base64 encoding
    public static String toBase64(String url) {
        return Base64.getEncoder().encodeToString(url.getBytes());
    }

    // url to full filename
    public static Path urlToFilePath(String url) {
        return Paths.get(cacheRootPath.toString(), urlToFileName(url));
    }

    // combine filename with extension
    public static String urlToFileName(String url) {
        return toBase64(url) + ".html";
    }

    // get html with default times
    public static Document getHtml(String url) {
        return getHtml(url, DEFAULT_RETRY_TIMES);
    }

    // get html offline or online(local without cache file)
    public static Document getHtml(String url, int retry) {
        checkCacheRootPath();

        Document doc = readLocalHtml(url);
        if (doc != null) {
            return doc;
        }

        ProxyIpAddr proxy = null;
        try {

            doc = getHtmlOnline(url, proxy);
            if (isBlockContent(doc)) {
                if (proxy != null) {
                    ProxyIpManager.blockIp(proxy);
                    proxy = ProxyIpManager.getProxyIp();
                }

                return getHtml(url, --retry);
            }

            // 保存到本地
            String charsetName = doc.charset().name();
            CharsetManager.saveCharset(toBase64(url), charsetName);

            byte[] data = doc.outerHtml().getBytes(charsetName);
            Files.write(fullName, data);

            return doc;

        } catch (SocketTimeoutException e) {
            if (proxy != null) {
                ProxyIp Manager.blockIp(proxy);
            }

            e.printStackTrace();
        } catch (Exception e) {
            if (proxy != null) {
                ProxyIpManager.failure(proxy);
            }

            e.printStackTrace();
        }

        if (retry > 0) {
            return getHtml(url, --retry);
        }

        return doc;
    }

    // check the response content is block
    private static boolean isBlockContent(Document doc) {
        if (doc.title().equals("访问验证")) {
            return true;
        }

        if (doc.getElementsByTag("noscript").outerHtml().contains("请开启JavaScript")) {
            return true;
        }

        return false;
    }

    // get page online
    private static Document getHtmlOnline(String url, ProxyIpAddr proxy) throws IOException {
        Connection connection = Jsoup.connect(url)
                .referrer("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/")
                .userAgent(UserAgentManager.getRandomUserAgent())
                .timeout(5000);

        if (proxy != null) {
            // use proxy
            connection.proxy(proxy.getIp(), proxy.getPort());
        }

        return connection.get();
    }

    // read page html on local cache
    private static Document readLocalHtml(String url) {
        Path file = urlToFilePath(url);
        if (!Files.exists(file)) {
            return null;
        }

        Document doc = null;

        try {
            doc = Jsoup.parse(file.toFile(), CharsetManager.getCharset(toBase64(url)));
            doc.setBaseUri(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return doc;
    }

    private static void checkCacheRootPath() {
        if (atomicCheck.get())
            return;

        if (!Files.exists(cacheRootPath)) {
            try {
                Files.createDirectories(cacheRootPath);
                atomicCheck.compareAndSet(false, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
