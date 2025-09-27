package com.example.agencehotelrest.controllers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomePageController {

    @RequestMapping("/")
    public String showHomePage() {
        return "home";  // Cette méthode retournera le fichier home.html!
    }


}

