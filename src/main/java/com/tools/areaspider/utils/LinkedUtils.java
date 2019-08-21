package com.tools.areaspider.utils;

import java.util.List;

/**
 * linked utility
 * */
public final class LinkedUtils {
    public static <TValue> Linked<TValue> toLinked(List<TValue> list){
        if(list.isEmpty())
            return null;

        Linked<TValue> head=new Linked<>(list.get(0));
        Linked<TValue> current=head;

        for (int i = 1; i < list.size(); i++) {
            current.next=new Linked<>(list.get(i));
            current=current.next;
        }

        return head;
    }
}

