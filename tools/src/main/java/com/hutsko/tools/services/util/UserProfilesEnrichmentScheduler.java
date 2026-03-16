package com.hutsko.tools.services.util;

import com.hutsko.tools.services.UserProfilesEnrichmentCrawler;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Single-instance scheduler for the profile enrichment crawler.
 */
public class UserProfilesEnrichmentScheduler {
    private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private static Future<?> scheduledTask;
    private static UserProfilesEnrichmentCrawler runningTask;

    private UserProfilesEnrichmentScheduler() {
    }

    /**
     * Starts the crawler if not already running.
     */
    public static synchronized boolean start(UserProfilesEnrichmentCrawler task) {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            return false;
        }
        runningTask = task;
        scheduledTask = executor.submit(task);
        return true;
    }

    /**
     * @return true if crawler task is active
     */
    public static synchronized boolean isRunning() {
        return scheduledTask != null && !scheduledTask.isCancelled();
    }

    /**
     * Requests a graceful stop and interrupts the running task.
     */
    public static synchronized boolean stop() {
        if (scheduledTask == null || scheduledTask.isCancelled()) {
            return false;
        }
        if (runningTask != null) {
            runningTask.requestStop();
        }
        scheduledTask.cancel(true);
        return true;
    }
}
