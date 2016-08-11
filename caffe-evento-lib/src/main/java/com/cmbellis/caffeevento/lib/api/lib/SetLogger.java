package com.cmbellis.caffeevento.lib.api.lib;

import com.cmbellis.caffeevento.lib.annotation.CEExport;

import java.util.Set;

/**
 * Created by chris on 7/14/16.
 */
@CEExport
public interface SetLogger<T> {
    void add(T element);
    boolean contains(T element);
    Set<T> contents();
}
