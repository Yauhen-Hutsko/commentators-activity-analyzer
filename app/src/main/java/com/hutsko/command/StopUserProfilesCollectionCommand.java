package com.hutsko.command;

import com.hutsko.tools.services.util.UserProfilesCollectionScheduler;

import javax.servlet.http.HttpServletRequest;

/**
 * Stops the user profile crawler from the web UI.
 */
public class StopUserProfilesCollectionCommand implements Command {
    private static final String ATTR_CRAWLER_STATUS = "userProfilesStatus";

    @Override
    public String execute(HttpServletRequest request) {
        boolean stopped = UserProfilesCollectionScheduler.stop();
        if (stopped) {
            request.setAttribute(ATTR_CRAWLER_STATUS, "User profile crawler stopping. Data will be persisted.");
        } else {
            request.setAttribute(ATTR_CRAWLER_STATUS, "User profile crawler is not running.");
        }
        return "/profile.jsp";
    }
}
