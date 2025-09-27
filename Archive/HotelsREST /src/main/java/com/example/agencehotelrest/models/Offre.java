package com.example.agencehotelrest.models;


import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;


@Entity
@Data
@NoArgsConstructor
public class Offre {
    @Id
    @GeneratedValue
    private int id;

    private String dateDebutOffre;

    private String dateFinOffre;

    private int typeChambre; // 1: simple, 2: double, 3: triple

    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "agence_id", nullable = false)
    private Agence agence;

    private double pourcentageReduction;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;


}
