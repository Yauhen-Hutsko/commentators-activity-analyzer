package com.hutsko.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.OptionalInt;
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

    public int getViews() {
        return service.getViews();
    }

    public int getTotal() {
        return service.getTotalComments();
    }

    public float getInvolvedPercent(){
        if (service.getViews() == 0) {
            return 0;
        }
        float involved = (float)(service.getTotalComments()*100)/service.getViews();
        String s = String.format("%.2f %n", involved);
        return Float.valueOf(s);
    }
}
