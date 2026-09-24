package pl.edu.pjwstk.lpfap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/admin", "/admin/"})
    public String adminRoot() {
        return "redirect:/admin/animals.html";
    }
}
