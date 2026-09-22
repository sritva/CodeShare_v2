package com.codeshare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
        "/", "/home", "/login", "/register",
        "/my-snippets", "/create", "/search",
        "/snippet/**", "/edit/**", "/share/**", "/starred", "/user/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
