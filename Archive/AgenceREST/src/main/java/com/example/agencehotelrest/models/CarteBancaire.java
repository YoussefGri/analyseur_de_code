package com.example.agencehotelrest.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class CarteBancaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private long numero;
    private int cvv;
    private String dateExpiration;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
}
