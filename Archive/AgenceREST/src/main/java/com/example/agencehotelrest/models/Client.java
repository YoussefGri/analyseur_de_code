package com.example.agencehotelrest.models;

import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import jakarta.persistence.*;
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
            name = "agence_client",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "agence_id")
    )
    private List<Agence> agences;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CarteBancaire> cartesBancaires;

}
