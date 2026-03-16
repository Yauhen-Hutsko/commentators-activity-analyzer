package com.hutsko.command;

import com.hutsko.tools.services.UserProfilesCollection;
import com.hutsko.tools.services.util.UserProfilesCollectionScheduler;

import javax.servlet.http.HttpServletRequest;
import java.util.ResourceBundle;

/**
 * Starts the user profile crawler from the web UI (admin-protected).
 */
public class StartUserProfilesCollectionCommand implements Command {
    private static final String PARAM_ADMIN_PASSWORD = "adminPassword";
    private static final String ATTR_CRAWLER_STATUS = "userProfilesStatus";

    @Override
    public String execute(HttpServletRequest request) {
        String password = request.getParameter(PARAM_ADMIN_PASSWORD);
        ResourceBundle bundle = ResourceBundle.getBundle("project");
        String expected = bundle.getString("admin.password");
        int pauseSeconds = Integer.parseInt(bundle.getString("crawler.request.pause.seconds"));

        if (password == null || !password.equals(expected)) {
            request.setAttribute(ATTR_CRAWLER_STATUS, "Admin password is invalid.");
            return "/profile.jsp";
        }

        boolean started = UserProfilesCollectionScheduler.start(new UserProfilesCollection(pauseSeconds));
        if (started) {
            request.setAttribute(ATTR_CRAWLER_STATUS, "User profile crawler started.");
        } else {
            request.setAttribute(ATTR_CRAWLER_STATUS, "User profile crawler is already running.");
        }
        return "/profile.jsp";
    }
}
