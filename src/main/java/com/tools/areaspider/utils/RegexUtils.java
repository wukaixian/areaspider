package com.tools.areaspider.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexUtils {

    /*extract number for string
    * */
    public static Integer extractNumber(String input){
        Pattern regex = Pattern.compile("\\d+");
        Matcher matcher = regex.matcher(input);

        if(matcher.find()){
            return Integer.valueOf(matcher.group());
        }

        return null;
    }
}
