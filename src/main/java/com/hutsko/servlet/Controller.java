package com.hutsko.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.hutsko.JsonAnalyzer;
import com.hutsko.OnlinerCommentsLoader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "controller", urlPatterns = "/controller")
public class Controller extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    private void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getParameter("articleURL");

        OnlinerCommentsLoader parser = new OnlinerCommentsLoader(url);
        JsonNode commentsNode = parser.getCommentsNode();
        JsonAnalyzer jsonAnalyzer = new JsonAnalyzer(commentsNode);

//        CommentParserApi cpa = new CommentParserApiImpl(jsonAnalyzer);

        req.setAttribute("comments", jsonAnalyzer.getComments());
        req.getRequestDispatcher("/jsp/welcome.jsp").forward(req, resp);
    }
}
