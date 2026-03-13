package com.hutsko.command;

import javax.servlet.http.HttpServletRequest;

public class EmptyCommand implements Command {
    private static final String PATH_PAGE_INDEX = "/index.jsp";

    @Override
    public String execute(HttpServletRequest request) {
        return PATH_PAGE_INDEX;
    }
}
