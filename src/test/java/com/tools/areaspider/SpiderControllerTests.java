package com.tools.areaspider;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class SpiderControllerTests {

    @Test
    public void getLatestAreaUrlTest() {
        String url = new SpiderController().getLatestAreaUrl();
        assertTrue(url.indexOf("2018") > 0);
    }

    @Test
    public void Test(){
        assertTrue("/tjsj/tjbz/tjyqhdmhcxhfdm/2018/index.html".indexOf(2018)>0);
    }
}
