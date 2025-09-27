package com.example.agencehotelrest.dto;

import com.example.agencehotelrest.models.Reservation;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReservationDTO {
    int id;
    String nom;
    String prenom;
    String dateDebut;
    String dateFin;
    double total;

    public ReservationDTO(int reservationId, String nomClient, String prenomClient, String dateDebut, String dateFin, double prix) {

        this.id = reservationId;
        this.nom = nomClient;
        this.prenom = prenomClient;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.total = prix;
    }

}
