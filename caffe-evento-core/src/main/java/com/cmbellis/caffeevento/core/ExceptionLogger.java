package com.cmbellis.caffeevento.core;

import org.apache.commons.logging.Log;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

/**
 * Created by chris on 8/30/16.
 */
public class ExceptionLogger implements ExceptionListener {
    private Log log;

    public ExceptionLogger(Log log) {
        this.log = log;
    }

    @Override
    public void onException(JMSException exception) {
        log.error("Exception from connection", exception);
    }
}
