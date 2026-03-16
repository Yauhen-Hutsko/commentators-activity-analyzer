package com.hutsko.command;

import com.hutsko.tools.services.util.UserProfilesEnrichmentScheduler;

import javax.servlet.http.HttpServletRequest;

/**
 * Stops the user profile enrichment crawler from the web UI.
 */
public class StopUserProfilesEnrichmentCommand implements Command {
    private static final String ATTR_ENRICH_STATUS = "userProfilesEnrichmentStatus";

    @Override
    public String execute(HttpServletRequest request) {
        boolean stopped = UserProfilesEnrichmentScheduler.stop();
        if (stopped) {
            request.setAttribute(ATTR_ENRICH_STATUS, "User profile enrichment stopping. Data will be persisted.");
        } else {
            request.setAttribute(ATTR_ENRICH_STATUS, "User profile enrichment is not running.");
        }
        return "/profile.jsp";
    }
}
