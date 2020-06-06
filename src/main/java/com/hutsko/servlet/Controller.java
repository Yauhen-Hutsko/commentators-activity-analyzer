package com.hutsko.servlet;

import com.hutsko.JsonAnalyzer;
import com.hutsko.OnlinerArticleLoader;
import com.hutsko.OnlinerJsonAnalyzer;
import com.hutsko.impl.CommentParserApiImpl;
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
import java.util.concurrent.TimeUnit;

@WebServlet(name = "controller", urlPatterns = "/controller")
public class Controller extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(Controller.class);
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private Map<String, BringCommentsTask> tasks = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    private void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("Controller invoked.");
        String projectRoot = req.getContextPath();
        String watchedArticle = req.getParameter("tracked");
        String interval = req.getParameter("interval");
        String shutdownAll = req.getParameter("shutdownAllTasks");
        String stopOne = req.getParameter("stop-follow");
        if (watchedArticle != null && !watchedArticle.isEmpty()) {
            scheduledPoll(watchedArticle, Integer.valueOf(interval));
        }
        if (shutdownAll != null) {
            executor.shutdownNow();
            for (BringCommentsTask value : tasks.values()) {
                value.getThread().interrupt();
                log.info("Tracking of [" + value.getName() + "] was stopped. Thread interruption status: " + value.getThread().isInterrupted());
            }
            tasks.clear();
            log.info("Found tasks: " + tasks.size() + ". Scheduled tasks: " + executor.getQueue().size());
            log.info("Approximate number of threads that are actively executing tasks: " + executor.getActiveCount());
            resp.setStatus(HttpServletResponse.SC_OK);
        }
        if (stopOne != null) {
            BringCommentsTask task = tasks.get(stopOne);
            task.cancel();
            tasks.remove(stopOne);
            executor.purge();
            log.info("The article " + task.getName() + " not tracked anymore. Stopped by user.");
            log.info("Found tasks: " + tasks.size() + ". Scheduled tasks: " + executor.getQueue().size());
        }
        // todo: create a factory that will validate url and create correct Parser.

        String url = req.getParameter("articleURL");
        if (url != null) {
            OnlinerArticleLoader parser = new OnlinerArticleLoader(url);
            JsonAnalyzer jsonAnalyzer = new OnlinerJsonAnalyzer(parser.getCommentsRootNode());

            CommentParserApiImpl parserApi = new CommentParserApiImpl(jsonAnalyzer);
            req.setAttribute("comments", jsonAnalyzer.getComments());
        }
        req.getSession().setAttribute("tasks", getTasks());
        req.getServletContext().setAttribute("servCont", 42); //test line
        req.getRequestDispatcher("/jsp/welcome.jsp").forward(req, resp);
//        resp.sendRedirect(projectRoot + "/jsp/welcome.jsp");
    }

    //TODO: implement this functionality on JSP page and test it.
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

    public Map<String, BringCommentsTask> getTasks() {
        return tasks;
    }
}
