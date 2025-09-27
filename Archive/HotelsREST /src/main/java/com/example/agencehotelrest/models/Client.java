package com.example.agencehotelrest.models;

import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Client {

    @Id
    @GeneratedValue
    private int id;

    private String nom;

    private String prenom;

    private String email;


    @ManyToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "client_hotel",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "hotel_id")
    )
    private List<Hotel> hotels = new ArrayList<>(); // les hotels où le client a déjà réservé

    public boolean addHotel(Hotel hotel) {
        if (hotels.contains(hotel)) {
            return false;
        }
        hotels.add(hotel);
        return true;
    }


}
