package com.latoscana.branchnode.financial.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/cashier")
public class CashierPOSController {

    @GetMapping
    public String showCashierPOS(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }
        return "cashier-pos";
    }
}
