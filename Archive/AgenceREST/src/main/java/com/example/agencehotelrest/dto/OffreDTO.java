package com.example.agencehotelrest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OffreDTO {
    private int idOffre;
    private String dateDebutOffre;
    private String dateFinOffre;
    private int typeChambre; // 1: simple, 2: double, 3: triple
    private String imageUrl;
    private String nomHotel;
    private int idHotel;
    private double prix;


}

