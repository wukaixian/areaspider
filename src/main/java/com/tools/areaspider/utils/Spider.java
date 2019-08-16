package com.tools.areaspider.utils;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Spider {

    public static String getHtml(String url, Charset charset) {
        Charset localCharset = charset == null ? StandardCharsets.UTF_8 : charset;

        RestTemplate client = new RestTemplate();
        client.getMessageConverters().set(1, new StringHttpMessageConverter(localCharset));

        ResponseEntity<String> responseEntity = client.getForEntity(url, String.class);
        String html = responseEntity.getBody();

        if (charset == null) {
            // checked charset
            String originalCharset = extractAttributeValue(html, "charset");
            if (originalCharset != null && !originalCharset.equalsIgnoreCase("utf-8")
                    && !originalCharset.equalsIgnoreCase("utf8")) {

                // use original charset load html again
                return getHtml(url, Charset.forName(originalCharset));
            }
        }

        return html;
    }

    // extract value for html attribute
    public static String extractAttributeValue(String input, String attrName) {
        Pattern pattern = Pattern.compile("(?<=" + attrName + "=)(.*?)(?=\"|;)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    public static String extractText(String input,String label){
        Pattern pattern=Pattern.compile("");
        return null;
    }

    public static List<String> extractAttributeValues(String input, String attrName) {
        Pattern pattern = Pattern.compile("(?<=" + attrName + "=)(.*?)(?=\"|;)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);

        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }

        return results;
    }


    public static List<String> extractValues(String input, String regexStr) {
        Pattern pattern = Pattern.compile(regexStr);
        Matcher matcher = pattern.matcher(input);

        List<String> values=new ArrayList<>();
        while (matcher.find()){
            values.add(matcher.group());
        }

        return values;
    }

    public static String extractValue(String input,String regexStr){
        Pattern pattern=Pattern.compile(regexStr);
        Matcher matcher = pattern.matcher(input);

        if(matcher.find()){
            return matcher.group();
        }

        return null;
    }
}
