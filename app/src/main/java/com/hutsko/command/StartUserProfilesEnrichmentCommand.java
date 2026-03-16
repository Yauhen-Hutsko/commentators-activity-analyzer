package com.hutsko.command;

import com.hutsko.tools.services.UserProfilesEnrichmentCrawler;
import com.hutsko.tools.services.util.UserProfilesEnrichmentScheduler;

import javax.servlet.http.HttpServletRequest;
import java.util.ResourceBundle;

/**
 * Starts the user profile enrichment crawler from the web UI (admin-protected).
 */
public class StartUserProfilesEnrichmentCommand implements Command {
    private static final String PARAM_ADMIN_PASSWORD = "adminPassword";
    private static final String ATTR_ENRICH_STATUS = "userProfilesEnrichmentStatus";

    @Override
    public String execute(HttpServletRequest request) {
        String password = request.getParameter(PARAM_ADMIN_PASSWORD);
        ResourceBundle bundle = ResourceBundle.getBundle("project");
        String expected = bundle.getString("admin.password");
        int pauseSeconds = Integer.parseInt(bundle.getString("crawler.request.pause.seconds"));

        if (password == null || !password.equals(expected)) {
            request.setAttribute(ATTR_ENRICH_STATUS, "Admin password is invalid.");
            return "/profile.jsp";
        }

        boolean started = UserProfilesEnrichmentScheduler.start(new UserProfilesEnrichmentCrawler(pauseSeconds));
        if (started) {
            request.setAttribute(ATTR_ENRICH_STATUS, "User profile enrichment started.");
        } else {
            request.setAttribute(ATTR_ENRICH_STATUS, "User profile enrichment is already running.");
        }
        return "/profile.jsp";
    }
}
