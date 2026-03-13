package com.hutsko.command;

import com.hutsko.services.BringCommentsTask;
import com.hutsko.services.util.TaskStorageSingletone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class StopFollowCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(StopFollowCommand.class);
    private Map<String, BringCommentsTask> tasks = TaskStorageSingletone.getInstance();
    private ScheduledThreadPoolExecutor executor = TaskStorageSingletone.getExecutor();

    @Override
    public String execute(HttpServletRequest request) {
        String stopOne = request.getParameter(PARAM_STOP_FOLLOW);

        BringCommentsTask task = tasks.get(stopOne);
        task.cancel();
        tasks.remove(stopOne);
        executor.purge();
        log.info("The article " + task.getName() + " not tracked anymore. Stopped by user.");
        log.info("Found tasks: " + tasks.size() + ". Scheduled tasks: " + executor.getQueue().size());
        return PATH_PAGE_WELCOME;
    }
}
