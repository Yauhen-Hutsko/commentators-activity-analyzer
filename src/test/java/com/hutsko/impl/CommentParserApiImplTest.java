package com.hutsko.impl;

import com.hutsko.CommentParserApi;
import com.hutsko.JsonAnalyzer;
import com.hutsko.entity.Comment;
import com.hutsko.entity.User;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.LocalDateTime;
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
    public void testGetUserActivity() {
        List<LocalDateTime> timestamps = parserAPI.getUserActivity(666);
        LocalDateTime expected1 = LocalDateTime.parse("2019-05-04T11:42:55.434");
        LocalDateTime expected2 = LocalDateTime.parse("2019-05-04T11:40:55.434");
        LocalDateTime expected3 = LocalDateTime.parse("2019-05-04T11:41:55.434");
        Assert.assertEquals(expected1, timestamps.get(0));
        Assert.assertEquals(expected2, timestamps.get(1));
        Assert.assertEquals(expected3, timestamps.get(2));
    }

    @Test
    public void testGetBestComment() {
        Comment comment = parserAPI.getBestComment();
        System.out.format("Worst comment has %s likes\n", comment.getLikes());
        Assert.assertEquals(6, comment.getLikes());
    }

    @Test
    public void testGetWorstComment() {
        Comment comment = parserAPI.getWorstComment();
        System.out.format("Worst comment has %s dislikes\n", comment.getDislikes());
        Assert.assertEquals(10, comment.getDislikes());
    }

    @Test
    public void testGetBestUser() {
        User user = parserAPI.getBestUser();
        System.out.format("Best user has %s likes\n", user.getLikeCounter());
        Assert.assertEquals(60, user.getLikeCounter());
    }

    @Test
    public void testGetWorstUser() {
        User user = parserAPI.getWorstUser();
        System.out.format("Worst user has %s dislikes\n", user.getDislikeCounter());
        Assert.assertEquals(100, user.getDislikeCounter());
    }

    @Test
    public void testFindCommentByWords(){
        List<Comment> found = parserAPI.findCommentsByText("1");
        List<Comment> found2 = parserAPI.findCommentsByText("empty text2");
        List<Comment> found3 = parserAPI.findCommentsByText("empty text");
        List<Comment> notFound = parserAPI.findCommentsByText("emty ");
        List<Comment> notFound2 = parserAPI.findCommentsByText(" ");

        Assert.assertEquals(1, found.size());
        Assert.assertEquals(1, found2.size());
        Assert.assertEquals(3, found3.size());
        Assert.assertEquals(0, notFound.size());
        Assert.assertEquals(0, notFound2.size()); // the method shouldn't found whitespaces only
    }

    @Test
    public void testGetUserByName(){
        User user = parserAPI.getUserByName("Vasya");

        Assert.assertEquals("Vasya", user.getName());
        Assert.assertNotEquals("Zenya", user.getName());
    }

    @Test
    public void testGetUserById(){
        User user = parserAPI.getUserById(667);

        Assert.assertEquals(667, user.getId());
        Assert.assertNotEquals(837, user.getId());
    }

    private void prepareComments() {
        for (int i = 0; i < 3; i++) {
            Comment comment = new Comment();
            comment.setLikes(i * 3);
            comment.setDislikes(i * 5);
            comment.setCreated(String.format("2019-05-04T11:4%s:55.434000+03:00", i));
            comment.setText("empty text" + i);
            comments.add(comment);
        }
        prepareUsers(comments);
    }

    private void prepareUsers(List<Comment> comments) {
        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setId(665 + i);
            user.setName("Vasya");
            user.setLikeCounter(i * 30);
            user.setDislikeCounter(i * 50);
//            user.addMessage(comments.get(i).getCreated(), comments.get(i).getText());
            comments.forEach(comment -> user.addMessage(comment.getCreated(), comment.getText()));
            users.put(665 + i, user);
        }
    }
}
