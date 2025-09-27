package com.example.agencehotelrest.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class ComparateurPageController {

    @GetMapping("/comparateur")
    public String showComparateurPage() {
        // Retourne le nom de la vue "comparateur" (fichier comparateur.html)
        return "Comparateur";
    }



//    @RequestMapping("/comparateur-form")
//    public String showComparatore() {
//        return "Comparateur";
//    }
}

