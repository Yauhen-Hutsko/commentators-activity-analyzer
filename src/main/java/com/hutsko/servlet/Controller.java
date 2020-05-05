package com.hutsko.servlet;

import com.hutsko.JsonAnalyzer;
import com.hutsko.OnlinerArticleLoader;
import com.hutsko.OnlinerJsonAnalyzer;
import com.hutsko.impl.CommentParserApiImpl;
import com.hutsko.services.BringCommentsScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@WebServlet(name = "controller", urlPatterns = "/controller")
public class Controller extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(Controller.class);
    private String url;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    private void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        url = req.getParameter("articleURL");

        // todo: create a factory that will validate url and create correct Parser.
        OnlinerArticleLoader parser = new OnlinerArticleLoader(url);
        JsonAnalyzer jsonAnalyzer = new OnlinerJsonAnalyzer(parser.getCommentsRootNode());

        CommentParserApiImpl parserApi = new CommentParserApiImpl(jsonAnalyzer);

        req.setAttribute("comments", jsonAnalyzer.getComments());
        req.getRequestDispatcher("/jsp/welcome.jsp").forward(req, resp);

    }

    //TODO: implement this functionality on JSP page and test it.
    private void scheduledPoll() {
        log.info("Scheduler activated");
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        BringCommentsScheduler scheduler = new BringCommentsScheduler(url);
        executor.scheduleAtFixedRate(scheduler, 0, 5, TimeUnit.MINUTES);
        while (executor.getCompletedTaskCount() > 5) {
            executor.shutdown();
            log.info("Scheduler terminated");
        }

    }
}
