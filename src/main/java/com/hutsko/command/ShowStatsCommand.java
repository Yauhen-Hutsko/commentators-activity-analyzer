package com.hutsko.command;

import com.hutsko.JsonAnalyzer;
import com.hutsko.OnlinerArticleLoader;
import com.hutsko.OnlinerJsonAnalyzer;
import com.hutsko.impl.CommentParserApiImpl;
import com.hutsko.services.BringCommentsTask;
import com.hutsko.services.util.TaskStorageSingletone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class ShowStatsCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(ShowStatsCommand.class);
    private Map<String, BringCommentsTask> tasks = TaskStorageSingletone.getInstance();

    @Override
    public String execute(HttpServletRequest request) {
        // todo: create a factory that will validate url and create correct Parser.
        String url = request.getParameter(PARAM_ARTICLE_URL);

        OnlinerArticleLoader parser = new OnlinerArticleLoader(url);
        JsonAnalyzer jsonAnalyzer = new OnlinerJsonAnalyzer(parser.getCommentsRootNode());
        CommentParserApiImpl parserApi = new CommentParserApiImpl(jsonAnalyzer);

        request.setAttribute("comments", jsonAnalyzer.getComments());
        request.getSession().setAttribute("tasks", tasks);
        return PATH_PAGE_WELCOME;
    }
}
