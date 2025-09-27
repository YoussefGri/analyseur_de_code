package com.example.agencehotelrest.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class Agence {
    @Id
    @GeneratedValue
    private int id;

    private String password;

    private String nom;

}
