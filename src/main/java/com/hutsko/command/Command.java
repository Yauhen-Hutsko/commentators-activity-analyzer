package com.hutsko.command;

import javax.servlet.http.HttpServletRequest;

public interface Command {
    String PATH_PAGE_WELCOME = "/jsp/welcome.jsp";

    String PARAM_TRACKED = "tracked";
    String PARAM_INTERVAL = "interval";
    String PARAM_STOP_FOLLOW = "stopFollow";
    String PARAM_ARTICLE_URL = "articleURL";

    String execute(HttpServletRequest request);
}
