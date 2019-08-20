package com.tools.areaspider.domain;

/**
 * 代理ip
 */
public class ProxyIpAddr {
    // 默认权重
    private static final int DEFAULT_WEIGHT = 100;


    // ip addr
    private String ip;

    // tcp port
    private int port;

    // 权重
    private int weight;

    private int failTimes = 0;

    public ProxyIpAddr(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.weight = ProxyIpAddr.DEFAULT_WEIGHT;
    }

    public ProxyIpAddr(String ip, int port, int weight) {
        this.ip = ip;
        this.port = port;
        this.weight = weight;
    }

    public String getIp() {
        return ip;
    }


    public int getPort() {
        return port;
    }


    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }


    public void failure() {
        this.failTimes++;
        // 失败超过两次，block ip
        if (failTimes >= 2) {
            decrementWeight(weight);
        } else {
            decrementWeight(weight - 1);
        }

    }


    // 降低权重
    public void decrementWeight() {
        decrementWeight(1);
    }

    private void decrementWeight(int seed) {
        if (this.weight - seed <= 0 && seed > 1) {
            this.weight = 0;
        } else {
            this.weight--;
            // reset init state
            if (seed == 1 && weight == 0) {
                this.weight = DEFAULT_WEIGHT;
            }
        }
    }
}

