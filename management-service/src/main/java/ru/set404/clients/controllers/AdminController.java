package ru.set404.clients.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/site")
    public String index() {
        return "admin/index";
    }
}
