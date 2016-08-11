package com.cmbellis.caffeevento.lib.api.lib;

import java.util.Set;

/**
 * Created by chris on 7/14/16.
 */
public interface SetLogger<T> {
    void add(T element);
    boolean contains(T element);
    Set<T> contents();
}
