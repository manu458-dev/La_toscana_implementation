package com.latoscana.branchnode.inventory.presentation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerUIController {

    @GetMapping("/inventory")
    public String inventoryView() {
        return "manager-inventory";
    }

    @GetMapping("/audit")
    public String auditView() {
        return "manager-audit";
    }
}
