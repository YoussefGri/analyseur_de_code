package com.example.agencehotelrest.controllers;

import com.example.agencehotelrest.dto.OffreComparateur;
import com.example.agencehotelrest.dto.OffreDTO;
import com.example.agencehotelrest.dto.ReservationDTO;
import com.example.agencehotelrest.exceptions.BadDatesException;
import com.example.agencehotelrest.exceptions.OffreNotFoundException;
import com.example.agencehotelrest.models.Offre;
import com.example.agencehotelrest.services.ComparateurOffresService;
import com.example.agencehotelrest.services.ConsultationDisponibilitesService;
import com.example.agencehotelrest.services.ReservationService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/hotels")
public class HotelController {

    @Autowired
    private ConsultationDisponibilitesService consultationDisponibilitesService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ComparateurOffresService comparateurOffresService;


    @GetMapping("/{idHotel}/acces")
    ResponseEntity<Boolean> grantAccess(
            @PathVariable int idHotel,
            @RequestParam int idAgence,
            @RequestParam String password) {
        try {
            boolean accessGranted = consultationDisponibilitesService.grantAccess(idHotel, idAgence, password);
            return new ResponseEntity<>(accessGranted, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("pb acces : "+e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/offres")
    public ResponseEntity<Boolean> grantAccessNoHotelId(
            @RequestParam int idAgence,
            @RequestParam String password) {
        try {
            boolean accessGranted = consultationDisponibilitesService.grantAccessNoHotelId(idAgence, password);
            return new ResponseEntity<>(accessGranted, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("pb acces : "+e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/{idHotel}/offres")  // Changement ici pour accepter l'id de l'hôtel
    public ResponseEntity<String> getOffresByHotelId(
            @PathVariable int idHotel,  // Utilisation de @PathVariable pour récupérer l'ID de l'hôtel
            @RequestParam int idAgence,
            //@RequestParam String password,
            @RequestParam String dateDebut,
            @RequestParam String dateFin,
            @RequestParam int nbPersonnes) throws OffreNotFoundException {


        // Récupérer les offres de l'hôtel spécifié en fonction des dates et du nombre de personnes
        List<Offre> offres = new ArrayList<>();
        try{
            offres = consultationDisponibilitesService.getOffresDetails(idAgence, idHotel, dateDebut, dateFin, nbPersonnes);
        }
        catch (OffreNotFoundException e){
            return new ResponseEntity<>("Aucune offre n'a été trouvée pour cet hôtel.", HttpStatus.BAD_REQUEST);
        }

        List<OffreDTO> offreDTOs = new ArrayList<>();
        for (Offre offre : offres) {
            double prix = consultationDisponibilitesService.getPrixByOffreId(offre.getId(), dateDebut, dateFin, nbPersonnes);
            OffreDTO offreDTO = new OffreDTO(offre);
            offreDTO.setPrix(prix);
            offreDTOs.add(offreDTO);
        }

        // Retourner les offres sous forme de JSON
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String formattedOffers = objectMapper.writeValueAsString(offreDTOs);
            return new ResponseEntity<>(formattedOffers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{idHotel}/comparateur")
    public ResponseEntity<List<OffreComparateur>> comparerOffres(
            @PathVariable int idHotel,
            @RequestParam int idAgence,
            @RequestParam String ville,
            @RequestParam String dateDebut,
            @RequestParam String dateFin,
            @RequestParam int nbPersonnes) {

        List<OffreComparateur> offres = new ArrayList<>();

        try {
            offres = comparateurOffresService.getOffresDetails(idHotel, idAgence, ville, dateDebut, dateFin, nbPersonnes);
            return new ResponseEntity<>(offres, HttpStatus.OK);
        } catch (OffreNotFoundException e) {
            // Retourne le message de l'exception dans le body de la réponse
            System.err.println("pb comparateur : "+e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(offres, HttpStatus.BAD_REQUEST);
        } catch (BadDatesException e) {
            return new ResponseEntity<>(offres, HttpStatus.BAD_REQUEST);
        }
    }




    @PostMapping("/{idHotel}/reservations")
    public ResponseEntity<ReservationDTO> makeReservation(
            @PathVariable int idHotel,
            @RequestParam int idAgence,
            //@RequestParam String password,
            @RequestParam int idOffre,
            @RequestParam String dateDebut,
            @RequestParam String dateFin,
            @RequestParam String nomClient,
            @RequestParam String prenomClient,
            @RequestParam String emailClient,
            @RequestParam int nbPersonnes
    ) {


        ReservationDTO reservationDTO = new ReservationDTO();

        try {
            reservationDTO = reservationService.effectuerReservation(idOffre, idHotel, idAgence, nomClient, prenomClient, emailClient, dateDebut, dateFin, nbPersonnes);
        }
        catch (Exception e) {
            System.err.println("pb reservation : "+e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (reservationDTO != null) {
            return new ResponseEntity<>(reservationDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }



}
