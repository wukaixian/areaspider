package com.tools.areaspider;

import com.tools.areaspider.domain.Area;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;


public class SpiderControllerTests {


    @Test
    public void getProvinceTest() throws Exception{
        List<Area> city = SpiderController.getCity();
        List<Area> children = city.get(0).getChildren();

        assertTrue(children.size()>0);
    }
}
