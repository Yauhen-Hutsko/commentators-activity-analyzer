package com.hutsko.command;

import com.hutsko.services.BringCommentsTask;
import com.hutsko.services.util.TaskStorageSingletone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ShutdownAllCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(ShutdownAllCommand.class);
    private Map<String, BringCommentsTask> tasks = TaskStorageSingletone.getInstance();
    private ScheduledThreadPoolExecutor executor = TaskStorageSingletone.getExecutor();

    @Override
    public String execute(HttpServletRequest request) {

        executor.shutdownNow();
        for (BringCommentsTask value : tasks.values()) {
            value.getThread().interrupt();
            log.info("Tracking of [" + value.getName() + "] was stopped. Thread interruption status: " + value.getThread().isInterrupted());
        }
        tasks.clear();
        log.info("Found tasks: " + tasks.size() + ". Scheduled tasks: " + executor.getQueue().size());
        log.info("Approximate number of threads that are actively executing tasks: " + executor.getActiveCount());
        return PATH_PAGE_WELCOME;
    }
}
