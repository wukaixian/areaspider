package com.tools.areaspider.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexUtils {

    /**
     * extract number for string
     * */
    public static Integer extractNumber(String input) {
        Pattern regex = Pattern.compile("\\d+");
        Matcher matcher = regex.matcher(input);

        if (matcher.find()) {
            return Integer.valueOf(matcher.group());
        }

        return null;
    }

    /**
     * extract ip and port in html element
     * */
    public static List<String> extractIpAddress(String input) {
        Pattern pattern = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+");
        Matcher matcher = pattern.matcher(input);

        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group().trim());
        }

        return list;
    }
}
