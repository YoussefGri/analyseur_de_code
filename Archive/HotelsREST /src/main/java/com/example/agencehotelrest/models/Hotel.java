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
public class Hotel {
    @Id
    @GeneratedValue
    private int id;

    private String nom;

    @OneToOne
    @JoinColumn(name = "adresse_id", referencedColumnName = "id")
    private Adresse adresse;

    private int nbEtoiles;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Chambre> chambres = new ArrayList<>();

    private int nbLitsDisponibles;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL)
    private List<Offre> offres;

    @ManyToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "hotel_agence",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "agence_id")
    )
    private List<Agence> agencesPartenaires = new ArrayList<>();

    @ManyToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "hotel_client",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "client_id")
    )
    private List<Client> clients = new ArrayList<>();

    public boolean addClient(Client client) {
        if (clients.contains(client)) {
            return false;
        }
        clients.add(client);
        return true;
    }

}
