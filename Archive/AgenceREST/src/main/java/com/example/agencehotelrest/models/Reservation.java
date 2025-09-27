package com.example.agencehotelrest.models;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@Table(name = "reservation")
@Data
public class Reservation {
    @Id
    @GeneratedValue
    private int id;

    private String checkin;

    private String checkout;

    private double total;

    private int idOffre;

    // La relation ManyToOne vers Hotel est suffisante
    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)  // On garde la relation sans la colonne idHotel séparée
    private Hotel hotel;  // Association à un seul hôtel

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "agence_id", nullable = false)
    private Agence agence;
}
