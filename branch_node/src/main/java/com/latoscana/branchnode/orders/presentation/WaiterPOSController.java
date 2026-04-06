package com.latoscana.branchnode.orders.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/waiter")
public class WaiterPOSController {

    @GetMapping
    public String showWaiterPOS(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }
        return "waiter-pos";
    }
}
