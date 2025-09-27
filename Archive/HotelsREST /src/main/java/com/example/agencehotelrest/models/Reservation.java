package com.example.agencehotelrest.models;

import lombok.Data;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservation")
@Data
public class Reservation {
    @Id
    @GeneratedValue
    private int id;

    private String checkin;

    private String checkout;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "reservation_chambre",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "chambre_id")
    )
    private List<Chambre> chambres = new ArrayList<>();

    private double total;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "offre_id", nullable = false)
    private Offre offre;

    @ManyToOne
    @JoinColumn(name = "agence_id", nullable = false)
    private Agence agence;

    public boolean addChambre(Chambre chambre) {
        if (chambres.contains(chambre)) {
            return false;
        }
        chambres.add(chambre);
        return true;
    }
}
