package com.example.agencehotelrest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OffreComparateurDTO {

    private String nomHotel;
    private String nomAgence;
    private String adresse;
    private int nbEtoiles;
    private int nbLitsDisponibles;
    private double prix;
    private double pourcentageReduction;


}
