package com.cmbellis.caffeevento.lib.impl.lib;


import com.cmbellis.caffeevento.lib.api.lib.SetLogger;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chris on 7/14/16.
 */
public class RotatedSetLogger<T> implements SetLogger<T> {
    public static final int DEFAULT_MAX = 3;
    private Set<T> currentLog;
    private EvictingQueue<Set<T>> logs;

    public RotatedSetLogger() {
        this(DEFAULT_MAX);
    }

    public RotatedSetLogger(int numLogs) {
        logs = EvictingQueue.create(numLogs);
        currentLog = new HashSet<>();
        logs.add(currentLog);
    }

    public void add(T element) {
        currentLog.add(element);
    }

    public boolean contains(T element) {
        return logs.stream().anyMatch(l -> l.contains(element));
    }

    public Set<T> contents() {
        return logs.stream().reduce(new HashSet<>(), Sets::union);
    }

    public void rotateLog() {
        currentLog = new HashSet<>();
        logs.add(currentLog);
    }
}
