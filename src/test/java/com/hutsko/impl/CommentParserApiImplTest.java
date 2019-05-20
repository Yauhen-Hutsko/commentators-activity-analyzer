package com.hutsko.impl;

import com.hutsko.CommentParserApi;
import com.hutsko.JsonAnalyzer;
import com.hutsko.entity.Comment;
import com.hutsko.entity.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentParserApiImplTest {

    @Mock
    private CommentParserApi parserAPI;
    private Map<Integer, User> users = new HashMap<>();
    private List<Comment> comments = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        JsonAnalyzer analyzer = Mockito.mock(JsonAnalyzer.class);
        parserAPI = new CommentParserApiImpl(analyzer);
        prepareComments();

        Mockito.when(analyzer.getCommentators()).thenReturn(users);
        Mockito.when(analyzer.getComments()).thenReturn(comments);

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getUserActivity() {

    }

    @Test
    public void getBestComment() {
        Comment comment = parserAPI.getBestComment();
        System.out.format("Worst comment has %s likes\n", comment.getLikes());
        Assert.assertEquals(6, comment.getLikes());
    }

    @Test
    public void getWorstComment() {
        Comment comment = parserAPI.getWorstComment();
        System.out.format("Worst comment has %s dislikes\n", comment.getDislikes());
        Assert.assertEquals(10, comment.getDislikes());
    }

    @Test
    public void getBestUser() {
        User user = parserAPI.getBestUser();
        System.out.format("Best user has %s likes\n", user.getLikeCounter());
        Assert.assertEquals(60, user.getLikeCounter());
    }

    @Test
    public void getWorstUser() {
        User user = parserAPI.getWorstUser();
        System.out.format("Worst user has %s dislikes\n", user.getDislikeCounter());
        Assert.assertEquals(100, user.getDislikeCounter());
    }

    private void prepareComments() {
        for (int i = 0; i < 3; i++) {
            Comment comment = new Comment();
            comment.setLikes(i * 3);
            comment.setDislikes(i * 5);
            comments.add(comment);
            User user = new User();
            user.setLikeCounter(i * 30);
            user.setDislikeCounter(i * 50);
            users.put(i, user);
        }
    }
}
