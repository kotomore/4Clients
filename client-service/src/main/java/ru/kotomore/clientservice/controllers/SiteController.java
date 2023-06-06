package ru.kotomore.clientservice.controllers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.kotomore.clientservice.services.ClientService;

@Controller
@AllArgsConstructor
public class SiteController {

    private final ClientService clientService;

    @GetMapping("/{id}")
    public String openAgentSite(@PathVariable String id, Model model) {
        String vanityUrl = clientService.getAgentIdByVanityUrl(id);
        model.addAttribute("agentId", vanityUrl == null ? id : vanityUrl);
        return "frontend/appointment";
    }
}
