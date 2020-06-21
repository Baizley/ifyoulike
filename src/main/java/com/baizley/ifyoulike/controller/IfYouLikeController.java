package com.baizley.ifyoulike.controller;

import com.baizley.ifyoulike.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
public class IfYouLikeController {

    private final CommentService commentService;

    @Autowired
    public IfYouLikeController(CommentService commentService) {
        this.commentService = commentService;
    }

    @RequestMapping(value = "/ifyoulike{blank}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> ifYouLikeJson(@PathVariable String blank) {
        return ResponseEntity.ok(commentService.retrieveTopLevelComments(blank));
    }

    @RequestMapping(value = "/ifyoulike{blank}")
    public String ifYouLikeHtml(@PathVariable String blank, Model model) {

        model.addAttribute("comments", commentService.retrieveTopLevelComments(blank));

        return "index";
    }
}
