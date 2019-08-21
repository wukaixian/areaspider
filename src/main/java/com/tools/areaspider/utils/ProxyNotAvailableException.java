package com.tools.areaspider.utils;

/**
 * 屏蔽异常
 * */
public class ProxyNotAvailableException extends Exception {
    public ProxyNotAvailableException() {
        super("proxy not available.");
    }
}
