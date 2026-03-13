package com.hutsko.command;

import com.hutsko.services.BringCommentsTask;
import com.hutsko.services.util.TaskStorageSingletone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TrackArticleCommand implements Command{
    private static final Logger log = LoggerFactory.getLogger(TrackArticleCommand.class);
    private Map<String, BringCommentsTask> tasks = TaskStorageSingletone.getInstance();
    private ScheduledThreadPoolExecutor executor = TaskStorageSingletone.getExecutor();


    @Override
    public String execute(HttpServletRequest request) {
        String trackedArticle = request.getParameter(PARAM_TRACKED);
        String interval = request.getParameter(PARAM_INTERVAL);
        if (trackedArticle != null && !trackedArticle.isEmpty()) {
            scheduledPoll(trackedArticle, Integer.valueOf(interval));
            request.getSession().setAttribute("tasks", tasks);
        }
        return PATH_PAGE_WELCOME;
    }

    private void scheduledPoll(String url, int interval) {
        BringCommentsTask task = new BringCommentsTask(url);
        if (!tasks.containsKey(url)) {
            tasks.put(url, task);
            executor.scheduleAtFixedRate(task, 0, interval, TimeUnit.MINUTES);
            log.info("Found tasks: " + tasks.size() + ". Scheduled tasks: " + executor.getQueue().size());
        } else {
            log.info("Article " + task.getName() + " is already tracking. Stop previous tracking before add new one.");
        }
    }
}
