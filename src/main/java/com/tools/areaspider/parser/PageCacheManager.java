package com.tools.areaspider.parser;

import com.tools.areaspider.CharsetManager;
import com.tools.areaspider.ProxyIpManager;
import com.tools.areaspider.UserAgentManager;
import com.tools.areaspider.domain.ProxyIpAddrWrapper;
import com.tools.areaspider.utils.ProxyNotAvailableException;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.HttpRetryException;
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
    private static String toBase64(String url) {
        return Base64.getEncoder().encodeToString(url.getBytes());
    }

    // url to full filename
    private static Path urlToFilePath(String url) {
        return Paths.get(cacheRootPath.toString(), urlToFileName(url));
    }

    // combine filename with extension
    private static String urlToFileName(String url) {
        return toBase64(url) + ".html";
    }

    // get html with default times
    public static Document getHtml(String url, ProxyIpAddrWrapper wrapper) {
        return getHtml(url, wrapper, DEFAULT_RETRY_TIMES);
    }

    // get html offline or online(local without cache file)
    public static Document getHtml(String url, ProxyIpAddrWrapper wrapper, int retry) {
        checkCacheRootPath();

        Document doc = readLocalHtml(url);
        if (doc != null) {
            return doc;
        }

        try {
            doc = getHtmlOnline(url, wrapper);
            if (isBlockContent(doc)) {
                throw new ProxyNotAvailableException();
            }

            // 保存到本地
            String charsetName = doc.charset().name();
            CharsetManager.saveCharset(toBase64(url), charsetName);

            byte[] data = doc.outerHtml().getBytes(charsetName);
            Files.write(urlToFilePath(url), data);
        } catch (ProxyNotAvailableException | HttpStatusException
                | HttpRetryException | SocketTimeoutException e) {
            e.printStackTrace();

            // switch proxy
            if (wrapper != null) {
                ProxyIpManager.blockIp(wrapper.getProxyIpAddr());
                wrapper.setProxyIpAddr(ProxyIpManager.getProxyIp());
            }

        } catch (Exception e) {
            if (wrapper != null) {
                ProxyIpManager.failure(wrapper.getProxyIpAddr());
            }

            e.printStackTrace();
        }

        if (doc == null) {
            return getHtml(url, wrapper, --retry);
        }

        return doc;
    }

    // check the response content is block
    private static boolean isBlockContent(Document doc) {
        if (doc.title().equals("访问验证")) {
            return true;
        }

        return doc.getElementsByTag("noscript").outerHtml().contains("请开启JavaScript");
    }

    // get page online
    private static Document getHtmlOnline(String url, ProxyIpAddrWrapper wrapper) throws Exception {
        Connection connection = Jsoup.connect(url)
                .referrer("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/")
                .userAgent(UserAgentManager.getRandomUserAgent())
                .timeout(10000); // 10s timeout

        if (wrapper != null) {
            // use proxy
            connection.proxy(wrapper.getProxyIpAddr().getIp(), wrapper.getProxyIpAddr().getPort());
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

    // mark cache error flag
    public static void markError(String url) {
        Path path = urlToFilePath(url);
        if (Files.exists(path)) {
            String name = "error-" + urlToFileName(url);
            path.toFile().renameTo(new File(Paths.get(cacheRootPath.toString(), name).toString()));
        }
    }
}
