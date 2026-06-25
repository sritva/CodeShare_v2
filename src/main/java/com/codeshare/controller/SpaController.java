package com.codeshare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = {
        "/", "/home", "/login", "/register",
        "/my-snippets", "/create", "/search",
        "/snippet/**", "/edit/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
