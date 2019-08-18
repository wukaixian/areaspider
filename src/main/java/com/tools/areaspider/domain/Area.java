package com.tools.areaspider.domain;

import java.util.ArrayList;
import java.util.List;


/*区域实体
* */

public class Area {


    private int id;
    private int pid;
    private String name;
    private String code;

    private List<Area> children;

    private String url;

    public Area(){
        this.children=new ArrayList<>();
    }

    public Area(int id,int pid,String name,String code,String url){
        this.id=id;
        this.pid=pid;
        this.name=name;
        this.code=code;
        this.url=url;

        this.children=new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Area> getChildren() {
        return children;
    }

    public void setChildren(List<Area> children) {
        this.children = children;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
