package ru.set404.clients.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class SiteController {

    @GetMapping("/{id}")
    public String openAgentSite(@PathVariable String id, Model model) {
        model.addAttribute("agentId", id);
        return "/frontend/appointment";
    }
}
