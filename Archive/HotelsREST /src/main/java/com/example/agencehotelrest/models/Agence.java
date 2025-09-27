package com.example.agencehotelrest.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Agence {
    @Id
    @GeneratedValue
    private int id;

    private String password;

    private String nom;

    @ManyToMany
    @JoinTable(
            name = "agence_hotel",
            joinColumns = @JoinColumn(name = "agence_id"),
            inverseJoinColumns = @JoinColumn(name = "hotel_id")
    )
    private List<Hotel> hotelsPartenaires = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "adresse_id", referencedColumnName = "id")
    private Adresse adresse;

    public boolean addHotelPartenaire(Hotel hotel) {
        if (hotelsPartenaires.contains(hotel)) {
            return false;
        }
        hotelsPartenaires.add(hotel);
        return true;
    }
}
