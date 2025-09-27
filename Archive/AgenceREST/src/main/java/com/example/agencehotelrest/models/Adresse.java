package com.example.agencehotelrest.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class Adresse {

    @Id
    @GeneratedValue
    private Long id;

    private String pays;

    private String ville;

    private String nomRue;

    private int numeroRue;


    @Override
    public String toString() {
        return numeroRue + " " + nomRue + ", " + ville + ", " + pays;
    }

}