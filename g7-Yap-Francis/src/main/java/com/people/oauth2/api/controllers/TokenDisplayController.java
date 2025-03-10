package com.people.oauth2.api.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TokenDisplayController {
    
    @GetMapping("/display-token")
    public String displayToken(@RequestParam("token") String token, Model model) {
        model.addAttribute("accessToken", token);
        return "index";
    }
}