package com.hutsko.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

public class BringCommentsScheduler extends TimerTask {
    private static Logger log = LoggerFactory.getLogger(BringCommentsScheduler.class);

    private DifferenceComparatorService service;

    public BringCommentsScheduler(String url) {
        this.service = new DifferenceComparatorService(url);
    }

    @Override
    public void run() {
        service.process();
        log.info(service.getClass().getName() + " activated.");
    }
}
