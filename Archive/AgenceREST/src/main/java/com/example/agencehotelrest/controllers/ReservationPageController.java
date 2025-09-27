package com.example.agencehotelrest.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class ReservationPageController {

    @RequestMapping("/reservation-form")
    public String reservationForm(
            @RequestParam(required = false) Integer offreId,
            @RequestParam(required = false) String idAgence,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin,
            @RequestParam(required = false) Integer nbPersonnes,
            Model model) {


        // Vérification si les paramètres sont présents et ajout à la vue
        if (offreId != null) model.addAttribute("offreId", offreId);
        if (idAgence != null) model.addAttribute("idAgence", idAgence);
        if (dateDebut != null) model.addAttribute("dateDebut", dateDebut);
        if (dateFin != null) model.addAttribute("dateFin", dateFin);
        if (nbPersonnes != null) model.addAttribute("nbPersonnes", nbPersonnes);

        return "Reservation";
    }
}
