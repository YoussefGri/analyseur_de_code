package com.example.agencehotelrest.services;

import com.example.agencehotelrest.models.*;
import com.example.agencehotelrest.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class AgenceService {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private AgenceRepository agenceRepository;
    @Autowired
    private CarteBancaireRepository carteBancaireRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private HotelRepository hotelRepository;


    public AgenceService() {}


    public Boolean existsClient(String nom, String prenom, int idAgence) {
        // Vérifie si un client ayant le nom et prénom spécifiés est associé à une agence ayant l'ID donné
        return clientRepository.findAll().stream()
                .anyMatch(c -> c.getNom().equals(nom) &&
                        c.getPrenom().equals(prenom) &&
                        c.getAgences().stream()
                                .anyMatch(agence -> agence.getId() == idAgence));
    }

    public Client ajouterClient(String nom, String prenom, String email, int cryptogramme, Long numeroCarte, String dateExpiration, int idAgence) {

        // Fetch the agency by id
        Optional<Agence> agenceOptional = agenceRepository.findById(idAgence);
        if (agenceOptional.isEmpty()) {
            throw new RuntimeException("Agency not found");
        }
        Agence agence = agenceOptional.get();

        // Check if the client already exists in the agency
        boolean clientExists = existsClient(nom, prenom, idAgence);

        if (clientExists) {
            return agence.getClients().stream()
                    .filter(c -> c.getNom().equals(nom) && c.getPrenom().equals(prenom))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Client not found"));
        }

        // Create and initialize a new Client
        Client client = new Client();
        client.setNom(nom);
        client.setPrenom(prenom);
        client.setEmail(email);
        clientRepository.save(client);  // Persist the Client

        // Create a new CarteBancaire and associate it with the Client
        CarteBancaire carteBancaire = new CarteBancaire();
        carteBancaire.setNumero(numeroCarte);
        carteBancaire.setCvv(cryptogramme);
        carteBancaire.setDateExpiration(dateExpiration);
        carteBancaire.setClient(client);
        carteBancaireRepository.save(carteBancaire);  // Persist the CarteBancaire

        // Add the CarteBancaire to the Client's list of cards
        client.setCartesBancaires(new ArrayList<>(List.of(carteBancaire)));

        // Add client to the agency's clients list
        agence.addClient(client);

        // Save the Client and the associated CarteBancaire(s)
        clientRepository.save(client);  // Persist the client and its cards
        agenceRepository.save(agence);  // Persist the agency with the newly added client


        return client;
    }

    public Reservation ajouterReservation(String idOffre, int idHotel, int idAgence, String dateDebut, String dateFin, int nbPersonnes, double prix ,int idClient) {

        // Vérification de l'existence du client
        Optional<Client> clientOptional = clientRepository.findById(idClient);
        if (clientOptional.isEmpty()) {
            throw new RuntimeException("Client non trouvé");
        }
        Client client = clientOptional.get();

        // Vérification de l'existence de l'agence
        Optional<Agence> agenceOptional = agenceRepository.findById(idAgence);
        if (agenceOptional.isEmpty()) {
            throw new RuntimeException("Agence non trouvée");
        }
        Agence agence = agenceOptional.get();

        // Vérification de l'existence de l'hôtel
        Optional<Hotel> hotelOptional = hotelRepository.findById(idHotel);
        if (hotelOptional.isEmpty()) {
            throw new RuntimeException("Hôtel non trouvé");
        }
        Hotel hotel = hotelOptional.get();

        int idOffreInt = Integer.parseInt(idOffre);

        Reservation reservation = new Reservation();
        reservation.setIdOffre(idOffreInt); // Utilisation de l'OffreDTO
        reservation.setHotel(hotel);
        reservation.setAgence(agence);
        reservation.setClient(client);
        reservation.setCheckin(dateDebut);
        reservation.setCheckout(dateFin);
        reservation.setTotal(prix);
       // reservation.setNbPersonnes(nbPersonnes);

        // Ajout de la réservation dans la base de données de l'agence
        agence.getReservations().add(reservation);
        // client.getReservations().add(reservation);

        // Sauvegarde de la réservation, de l'agence et du client
        reservationRepository.save(reservation);
        agenceRepository.save(agence);
        clientRepository.save(client);

        // Retourner la réservation
        return reservation;
    }



}

