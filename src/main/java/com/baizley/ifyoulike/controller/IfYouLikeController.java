package com.baizley.ifyoulike.controller;

import com.baizley.ifyoulike.model.Blank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IfYouLikeController {

    @RequestMapping("/ifyoulike{blank}")
    public String ifYouLike(@PathVariable String blank, Model model) {
        model.addAttribute("blank", new Blank(blank));

        return "ifyoulike";
    }
}
