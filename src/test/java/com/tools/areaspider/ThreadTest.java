package com.tools.areaspider;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ThreadTest {

    @Test
    public void Test() throws Exception {
        List<Thread> list=new ArrayList<>();

        for (int i = 10; i > 0; i--) {
            int local = i;
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(200 * local);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println(local);
            });

            t.start();
            list.add(t);
        }

        for (Thread t : list) {
            //t.join();
        }

        System.out.println("finish");
    }
}
