package com.tools.areaspider.utils;

/**
 * 单向链表
 */
public final class Linked<T> {
    private T value;
    public T next;

    public Linked(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }
}
