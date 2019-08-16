package com.tools.areaspider.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;


/*区域实体
* */
@Entity
public class Area {

    @Id
    private int id;
    private int pid;
    private String name;
    private String code;

    @Transient
    private List<Area> children;

    public Area(int id,int pid,String name,String code){
        this.id=id;
        this.pid=pid;
        this.name=name;
        this.code=code;

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
}
