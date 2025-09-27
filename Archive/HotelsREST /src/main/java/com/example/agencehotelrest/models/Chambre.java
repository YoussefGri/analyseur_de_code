package com.example.agencehotelrest.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@NoArgsConstructor
public class Chambre {

    @Id
    @GeneratedValue
    private int id;

    private int nbPersonnes;

    private double prix;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToMany(mappedBy = "chambres")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Reservation> reservations = new ArrayList<>();

}
