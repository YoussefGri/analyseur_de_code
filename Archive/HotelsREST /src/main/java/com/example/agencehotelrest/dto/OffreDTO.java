package com.example.agencehotelrest.dto;

import com.example.agencehotelrest.models.Offre;

import lombok.Data;

@Data
public class OffreDTO {
    private int idOffre;
    private String dateDebutOffre;
    private String dateFinOffre;
    private int typeChambre; // 1: simple, 2: double, 3: triple
    private String imageUrl;
    private String nomHotel;
    private int idHotel;
    private double prix;

    public OffreDTO(Offre offre) {
        this.idOffre = offre.getId();
        this.dateDebutOffre = offre.getDateDebutOffre();
        this.dateFinOffre = offre.getDateFinOffre();
        this.typeChambre = offre.getTypeChambre();
        this.imageUrl = "/images/chambre_type_" + offre.getTypeChambre() + ".jpg"; // Ajout du chemin correct
        this.nomHotel = offre.getHotel().getNom();
        this.idHotel = offre.getHotel().getId();
    }
}

