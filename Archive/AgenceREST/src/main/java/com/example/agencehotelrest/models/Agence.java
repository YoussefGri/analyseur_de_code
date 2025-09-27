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

    @ManyToMany(mappedBy = "agences")
    private List<Client> clients;

    @OneToMany(mappedBy = "agence")
    private List<Reservation> reservations;

    public boolean addHotelPartenaire(Hotel hotel) {
        if (hotelsPartenaires.contains(hotel)) {
            return false;
        }
        hotelsPartenaires.add(hotel);
        return true;
    }

    public boolean addClient(Client client) {
        if (clients.contains(client)) {
            return false;
        }
        clients.add(client);
        return true;
    }

    public boolean addReservation(Reservation reservation) {
        if (reservations.contains(reservation)) {
            return false;
        }
        reservations.add(reservation);
        return true;
    }
}
