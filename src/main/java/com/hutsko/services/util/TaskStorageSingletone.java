package com.hutsko.services.util;

import com.hutsko.services.BringCommentsTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class TaskStorageSingletone {
    private static Map<String, BringCommentsTask> tasks = new HashMap<>();
    private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    private TaskStorageSingletone() {
    }

    synchronized public static Map<String, BringCommentsTask> getInstance() {
        return tasks;
    }

    synchronized public static ScheduledThreadPoolExecutor getExecutor() {
        return executor;
    }
}
