package com.bike_assembly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class loginController {

    @GetMapping("/login")
    public String iniciarSesion() {
        return "/login"; 
    }
}