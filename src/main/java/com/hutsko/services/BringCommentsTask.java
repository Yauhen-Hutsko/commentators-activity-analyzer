package com.hutsko.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

public class BringCommentsTask extends TimerTask {
    private static Logger log = LoggerFactory.getLogger(BringCommentsTask.class);
    private String name;
    private DifferenceComparatorService service;

    public BringCommentsTask(String url) {
        this.service = new DifferenceComparatorService(url);
        name = url.substring(url.lastIndexOf('/') + 1);
    }

    @Override
    public void run() {
        log.info(this.getClass().getName() + " started.");
        service.process();
    }

    public Thread getThread() {
        return Thread.currentThread();
    }

    public String getName() {
        return name;
    }
}
