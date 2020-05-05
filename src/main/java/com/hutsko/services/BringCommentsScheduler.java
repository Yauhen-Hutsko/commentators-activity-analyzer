package com.hutsko.services;

import java.util.TimerTask;

public class BringCommentsScheduler extends TimerTask {

    private DifferenceComparatorService service;

    public BringCommentsScheduler(String url) {
        this.service = new DifferenceComparatorService(url);
    }

    @Override
    public void run() {
        service.process();
    }
}
