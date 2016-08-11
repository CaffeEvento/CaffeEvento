package com.cmbellis.caffeevento.lib.impl.lib;

import com.cmbellis.caffeevento.lib.annotation.CEExport;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by chris on 7/14/16.
 */
@CEExport
public class AutoRotatedSetLogger<T> extends RotatedSetLogger<T> {
    private RotatedSetLogger<T> delegate;
    private ScheduledExecutorService executorService;

    public static final TimeUnit DEFAULT_UNIT = TimeUnit.SECONDS;
    public static final long DEFAULT_DELAY = 100;
    public static final long DEFAULT_PERIOD = 120;

    public AutoRotatedSetLogger() {
        this(DEFAULT_DELAY, DEFAULT_PERIOD, DEFAULT_UNIT, DEFAULT_MAX);
    }

    public AutoRotatedSetLogger(ScheduledExecutorService executorService, long delay, long period, TimeUnit unit, int numLogs) {
        this.executorService = executorService;
        delegate = new RotatedSetLogger<>(numLogs);

        executorService.scheduleAtFixedRate(() -> delegate.rotateLog(), delay, period, unit);
    }

    public AutoRotatedSetLogger(long delay, long period, TimeUnit unit, int numLogs) {
        this(Executors.newScheduledThreadPool(1), delay, period, unit, numLogs);
    }

    @Override
    public void add(T element) {
        delegate.add(element);
    }

    @Override
    public boolean contains(T element) {
        return delegate.contains(element);
    }

    @Override
    public void rotateLog() {
        delegate.rotateLog();
    }

}
