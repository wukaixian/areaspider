package com.tools.areaspider;

import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// html file charset manager
public final class CharsetManager {

    // cache directory
    private static final Path charsetFile = Paths.get(System.getProperty("user.dir"), "cache", "charset.txt");

    // charset map
    private static final ConcurrentMap<String, String> charsetMap = new ConcurrentHashMap<>();

    // 加载本地文件编码
    public static void init() {
        if (charsetMap.size() > 0)
            return;

        if (!Files.exists(charsetFile)) {
            try {
                Files.createFile(charsetFile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return;
        }

        List<String> lines = null;
        try {
            lines = Files.readAllLines(charsetFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String line : lines) {
            if (StringUtils.isEmpty(line)) {
                continue;
            }

            String[] set = line.split(":");
            if (!charsetMap.containsKey(set[0].trim())) {
                charsetMap.put(set[0].trim(), set[1].trim());
            }
        }
    }

    // 保存编码
    public static void saveCharset(String fileName, String charsetName) {
        if (charsetMap.containsKey(fileName))
            return;

        // default encoding is gb2312,no need save
        if (charsetName.equalsIgnoreCase("GB2312"))
            return;

        charsetMap.put(fileName, charsetName);

        try {
            String keyValue = fileName + ":" + charsetName + System.lineSeparator();
            Files.write(charsetFile, keyValue.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 查询编码
    public static String getCharset(String fileName) {
        return charsetMap.containsKey(fileName) ? charsetMap.get(fileName) : "GB2312";
    }
}
