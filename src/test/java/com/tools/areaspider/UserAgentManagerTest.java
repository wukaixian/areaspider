package com.tools.areaspider;


import org.jsoup.Jsoup;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertTrue;

public class UserAgentManagerTest {

    @Test
    public void randomUserAgentTest() {
        IntStream.range(1, 10024).parallel()
                .forEach(x -> {
                    String agent = UserAgentManager.getRandomUserAgent();
                    System.out.println(agent);
                });

        System.out.println("finish");
    }

    @Test
    public void fetchPageTest() throws IOException {
        URL url = new URL("http://www.66ip.cn/mo.php?sxb=%B1%B1%BE%A9&tqsl=20");
        String html = Jsoup.parse(url, 5000)
                .outerHtml();

        System.out.println(html);

//        Document doc = Jsoup.connect("http://www.66ip.cn/mo.php?sxb=" + URLEncoder.encode("浙江","utf-8") + "&tqsl=20")
//                .get();

    }

    @Test
    public void Test() {
        String input = "192.168.0.1:8080     20 ";
        String[] lines = input.trim().split("\\s+");

        assertTrue(lines.length == 2);
        System.out.println(lines[0]);
        System.out.println(lines[1]);
    }

    @Test
    public void mapsortedByValuesTest() {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put(String.valueOf(i), i);
        }

        Map<String, Integer> sortedMap = map.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> x, LinkedHashMap::new));

        for (Map.Entry<String, Integer> stringIntegerEntry : sortedMap.entrySet()) {
            System.out.println(stringIntegerEntry.getKey() + ":" + stringIntegerEntry.getValue());
        }
    }

    @Test
    public void fileTimeTest() {
        try {
            FileTime fileTime = Files.readAttributes(Paths.get("D:\\java.iml"), BasicFileAttributes.class).lastModifiedTime();

            System.out.println(fileTime);
            System.out.println(Instant.now());

            Duration between = Duration.between(fileTime.toInstant(), Instant.now());
            System.out.println(between.toDays());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void listSortedTest() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }

        Iterator<Integer> iterator = list.iterator();

        list = list.stream()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());

        list.add(1024);

        for (Integer integer : list) {
            System.out.println(integer);
        }

        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    @Test
    public void writeEmptyFileTest() throws Exception {
        FileOutputStream fs = new FileOutputStream("D:\\a.txt");
        fs.write("".getBytes());
        fs.close();
    }

    @Test
    public void emptyListTest(){
        List<Integer> list=new ArrayList<>();
        List<String> collect = list.stream()
                .map(x -> String.valueOf(x))
                .map(String::new)
                .collect(Collectors.toList());

        String txt= String.join(System.lineSeparator(),collect);
        byte[] bytes = txt.getBytes();
        assertTrue(txt.equalsIgnoreCase(""));
    }

    @Test
    public void Test2() throws IOException {
        Jsoup.connect("http://www.google.com").get();
    }
}
