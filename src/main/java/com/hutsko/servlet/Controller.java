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
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@WebServlet(name = "controller", urlPatterns = "/controller")
public class Controller extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(Controller.class);
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);;
    private ArrayList<BringCommentsTask> tasks = new ArrayList<>();

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
        String shutdown = req.getParameter("shutdownTasks");
        if (watchedArticle != null && !watchedArticle.isEmpty()) {
            scheduledPoll(watchedArticle);
        }
        if (shutdown != null ) {
            for (BringCommentsTask task : tasks) {
                task.getThread().interrupt();
            }
            executor.shutdown();
        }
        // todo: create a factory that will validate url and create correct Parser.

        String url = req.getParameter("articleURL");
        if (url != null) {
            OnlinerArticleLoader parser = new OnlinerArticleLoader(url);
            JsonAnalyzer jsonAnalyzer = new OnlinerJsonAnalyzer(parser.getCommentsRootNode());

            CommentParserApiImpl parserApi = new CommentParserApiImpl(jsonAnalyzer);
            req.setAttribute("comments", jsonAnalyzer.getComments());
            req.setAttribute("tasks", getTasks());
        }
//        req.getRequestDispatcher("/jsp/welcome.jsp").forward(req, resp);
        resp.sendRedirect(projectRoot + "/jsp/welcome.jsp");
    }

    //TODO: implement this functionality on JSP page and test it.
    private void scheduledPoll(String url) {
        BringCommentsTask task = new BringCommentsTask(url);
        tasks.add(task);
        executor.scheduleAtFixedRate(task, 0, 5, TimeUnit.MINUTES);
        log.debug("Found tasks: (%1$s). Scheduled tasks: 2$s | 2s", tasks.size(), executor.getQueue().size());
    }

    public ArrayList<BringCommentsTask> getTasks() {
        return tasks;
    }
}
