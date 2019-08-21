package com.tools.areaspider.utils;

import java.util.List;

/**
 * 单向链表
 */
public final class Linked<T> {
    private T value;
    public Linked<T> next;

    public Linked(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }
}