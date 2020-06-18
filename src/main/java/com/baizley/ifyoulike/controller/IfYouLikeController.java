package com.baizley.ifyoulike.controller;

import com.baizley.ifyoulike.service.CommentService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.UnsupportedEncodingException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
public class IfYouLikeController {

    private final CommentService commentService;

    @Autowired
    public IfYouLikeController(CommentService commentService) {
        this.commentService = commentService;
    }

    @RequestMapping(value = "/ifyoulike{blank}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> ifYouLikeJson(@PathVariable String blank) throws UnsupportedEncodingException {
        JSONArray comments = commentService.retrieveTopLevelComments(blank);
        return ResponseEntity.ok(comments.toString());
    }

    @RequestMapping(value = "/ifyoulike{blank}")
    public String ifYouLikeHtml(@PathVariable String blank, Model model) {

        model.addAttribute("comments", commentService.retrieveTopLevelComments(blank));

        return "index";
    }
}
