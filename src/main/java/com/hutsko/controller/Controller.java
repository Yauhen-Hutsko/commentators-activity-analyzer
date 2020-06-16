package com.hutsko.controller;

import com.hutsko.command.Command;
import com.hutsko.command.EmptyCommand;
import com.hutsko.command.util.ActionFactory;
import com.hutsko.services.BringCommentsTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@WebServlet(name = "controller", urlPatterns = "/controller")
public class Controller extends HttpServlet {
    private static final String PATH_INDEX_PAGE = "/index.jsp";
    private static Logger log = LoggerFactory.getLogger(Controller.class);
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private Map<String, BringCommentsTask> tasks = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("Controller invoked.");
        String page;
        String action = req.getParameter("command");

        Command command = ActionFactory.defineCommand(action).orElse(new EmptyCommand());
        page = command.execute(req);
        if (page != null) {
            req.getRequestDispatcher("/jsp/welcome.jsp").forward(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + PATH_INDEX_PAGE);
        }
    }
}
