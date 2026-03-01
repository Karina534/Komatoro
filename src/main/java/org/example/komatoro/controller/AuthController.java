package org.example.komatoro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/users")
public class AuthController {
    @GetMapping("/login")
    public String loginPage(){
        System.out.println("I am in get login method controller");
        return "redirect:/login.html";
    }

    @GetMapping("/home")
    public String homePage(){
        System.out.println("I am in get home method controller");
        return "redirect:/home.html";
    }
}
