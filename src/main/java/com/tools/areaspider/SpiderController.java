package com.tools.areaspider;

import com.tools.areaspider.domain.Area;
import com.tools.areaspider.domain.ProxyIpAddrWrapper;
import com.tools.areaspider.parser.ProvincePageParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SpiderController {

    /*
     * area spider startup
     * */
    @RequestMapping("/")
    public ResponseEntity<?> runSpider() throws InterruptedException {
        List<Area> provinces = new ProvincePageParser(new ProxyIpAddrWrapper(ProxyIpManager.getProxyIp()))
                .runParser(null);

        List<Thread> tasks = new ArrayList<>();
        for (Area province : provinces) {
            AreaSpider spider = new AreaSpider(province, new ProxyIpAddrWrapper(ProxyIpManager.getProxyIp()));
            Thread task = new Thread(spider);
            task.start();

            tasks.add(task);
        }

        for (Thread task : tasks) {
            task.join();
        }

        return ResponseEntity.ok(provinces);
    }
}
