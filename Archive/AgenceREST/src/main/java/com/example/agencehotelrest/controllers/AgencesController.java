package com.example.agencehotelrest.controllers;

import com.example.agencehotelrest.dto.OffreComparateur;
import com.example.agencehotelrest.dto.OffreDTO;
import com.example.agencehotelrest.dto.ReservationDTO;
import com.example.agencehotelrest.models.Client;
import com.example.agencehotelrest.models.Hotel;
import com.example.agencehotelrest.repositories.AgenceRepository;
import com.example.agencehotelrest.repositories.HotelRepository;
import com.example.agencehotelrest.services.AgenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/agences")
public class AgencesController {

    private static final Logger logger = LoggerFactory.getLogger(AgencesController.class);

    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private AgenceRepository agenceRepository;
    @Autowired
    private AgenceService agenceService;

    @GetMapping("/offres")
    public ResponseEntity<String> getOffresByAgenceId(
            @RequestParam int idAgence,
            @RequestParam String password,
            @RequestParam String ville,
            @RequestParam String dateDebut,
            @RequestParam String dateFin,
            @RequestParam int nbPersonnes) {

        // Vérification des paramètres
        if (ville == null || dateDebut == null || dateFin == null || password == null) {
            logger.warn("Paramètres manquants pour la recherche d'offres.");
            return new ResponseEntity<>("Les paramètres requis sont manquants.", HttpStatus.BAD_REQUEST);
        }

        // Récupérer les hôtels partenaires
        List<Hotel> hotels = agenceRepository.findById(idAgence).get().getHotelsPartenaires().stream()
                .filter(hotel -> hotel.getAdresse().getVille().equalsIgnoreCase(ville))
                .toList();

        if (hotels.isEmpty()) {
            logger.warn("Aucun hôtel partenaire trouvé dans la ville : {}", ville);
            return new ResponseEntity<>("Aucun hôtel partenaire trouvé pour cette ville.", HttpStatus.BAD_REQUEST);
        }

        List<OffreDTO> offreDTOs = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        for (Hotel hotel : hotels) {
            String apiUrl = String.format("http://localhost:8081/hotels/%d/offres", hotel.getId());
            try {

                String apiUrlAccess = String.format("http://localhost:8081/hotels/%d/acces", hotel.getId());

                UriComponentsBuilder accessBuilder = UriComponentsBuilder.fromHttpUrl(apiUrlAccess)
                        .queryParam("idAgence", idAgence)
                        .queryParam("password", password);

                ResponseEntity<Boolean> accessResponse = restTemplate.getForEntity(accessBuilder.toUriString(), Boolean.class);

                if (accessResponse.getStatusCode() != HttpStatus.OK || !accessResponse.getBody()) {
                    return new ResponseEntity<>("Accès refusé, identifiant ou mot de passe incorrect.", HttpStatus.UNAUTHORIZED);
                }

                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl)
                        .queryParam("idAgence", idAgence)
//                        .queryParam("password", password)
                        .queryParam("dateDebut", dateDebut)
                        .queryParam("dateFin", dateFin)
                        .queryParam("nbPersonnes", nbPersonnes);

                ResponseEntity<OffreDTO[]> response = restTemplate.getForEntity(builder.toUriString(), OffreDTO[].class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    for (OffreDTO offre : response.getBody()) {
                        offreDTOs.add(offre);
                    }
                }
            } catch (HttpClientErrorException.NotFound e) {
                logger.error("API introuvable pour l'hôtel ID {}: {}", hotel.getId(), e.getMessage());
            } catch (Exception e) {
                logger.error("Erreur lors de l'appel à l'API de l'hôtel ID {}: {}", hotel.getId(), e.getMessage());
            }
        }

        if (offreDTOs.isEmpty()) {
            return new ResponseEntity<>("Aucune offre trouvée pour vos critères.", HttpStatus.BAD_REQUEST);
        }

        try {
            String formattedOffers = new ObjectMapper().writeValueAsString(offreDTOs);
            return new ResponseEntity<>(formattedOffers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            logger.error("Erreur lors de la conversion des offres en JSON: {}", e.getMessage());
            return new ResponseEntity<>("Erreur interne du serveur.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{idAgence}/comparateur")
    public ResponseEntity<List<OffreComparateur>> getOffresPourComparateur(
            @PathVariable int idAgence,
            @RequestParam String ville,
            @RequestParam String dateDebut,
            @RequestParam String dateFin,
            @RequestParam int nbPersonnes,
            @RequestParam int nbEtoiles) {

        // Récupérer les hôtels partenaires de l'agence qui répondent aux critères

        List<Hotel> hotels = agenceRepository.findById(idAgence).get().getHotelsPartenaires().stream()
                .filter(hotel -> hotel.getAdresse().getVille().equalsIgnoreCase(ville))
                .filter(hotel -> hotel.getNbEtoiles() >= nbEtoiles)
                .toList();


        if (hotels.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<OffreComparateur> offreComparateur = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        for (Hotel hotel : hotels) {
            String apiUrl = String.format("http://localhost:8081/hotels/%d/comparateur", hotel.getId());
            try {
                // Construire les paramètres de la requête
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl)
                        .queryParam("idAgence", idAgence)
                        .queryParam("ville", ville)
                        .queryParam("dateDebut", dateDebut)
                        .queryParam("dateFin", dateFin)
                        .queryParam("nbPersonnes", nbPersonnes);

                // Effectuer l'appel à l'API partenaire
                ResponseEntity<OffreComparateur[]> response = restTemplate.getForEntity(builder.toUriString(), OffreComparateur[].class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    for (OffreComparateur offre : response.getBody()) {
                        offreComparateur.add(offre);
                    }
                }
            } catch (Exception e) { // pour l'exception levee par getForEntity
                // Logger les erreurs pour un hôtel spécifique (normalement ce message ne devrait jamais être affiché)
                System.err.println("Erreur lors de l'appel à l'API de l'hôtel ID " + hotel.getId() + ": " + e.getMessage());
                e.printStackTrace();
                return new ResponseEntity<>(offreComparateur,HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<>(offreComparateur, HttpStatus.OK);
    }

    @PostMapping("/effectuerReservation")
    public ResponseEntity<String> effectuerReservation(
            @RequestParam String idOffre,
            @RequestParam(required = false) int idHotel,
            @RequestParam int idAgence,
            @RequestParam String password,
            @RequestParam String nomClient,
            @RequestParam String prenomClient,
            @RequestParam String dateDebut,
            @RequestParam String dateFin,
            @RequestParam String emailClient,
            @RequestParam String numeroCarte,
            @RequestParam String dateExpiration,
            @RequestParam int cryptogramme,
            @RequestParam int nbPersonnes) {

        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = String.format("http://localhost:8081/hotels/%d/reservations", idHotel);
        ReservationDTO reservationDTO;

        try {

            String apiUrlAccess = String.format("http://localhost:8081/hotels/%d/acces", idHotel);

            UriComponentsBuilder accessBuilder = UriComponentsBuilder.fromHttpUrl(apiUrlAccess)
                    .queryParam("idAgence", idAgence)
                    .queryParam("password", password);

            ResponseEntity<Boolean> accessResponse = restTemplate.getForEntity(accessBuilder.toUriString(), Boolean.class);

            if (accessResponse.getStatusCode() != HttpStatus.OK || !accessResponse.getBody()) {
                return new ResponseEntity<>("Accès refusé, identifiant ou mot de passe incorrect.", HttpStatus.UNAUTHORIZED);
            }

            int idOffreInt = Integer.parseInt(idOffre);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("idAgence", idAgence)
                    //.queryParam("password", password)
                    .queryParam("idOffre", idOffreInt)
                    .queryParam("dateDebut", dateDebut)
                    .queryParam("dateFin", dateFin)
                    .queryParam("nomClient", nomClient)
                    .queryParam("prenomClient", prenomClient)
                    .queryParam("emailClient", emailClient)
                    .queryParam("nbPersonnes", nbPersonnes);

            ResponseEntity<ReservationDTO> response = restTemplate.postForEntity(builder.toUriString(), null, ReservationDTO.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                reservationDTO = response.getBody();
                double total = reservationDTO.getTotal();
                long numeroCarteLong = Long.parseLong(numeroCarte);

                Client client = agenceService.ajouterClient(nomClient, prenomClient, emailClient, cryptogramme, numeroCarteLong, dateExpiration, idAgence);
                agenceService.ajouterReservation(idOffre, idHotel, idAgence, dateDebut, dateFin, nbPersonnes, total, client.getId());

                return new ResponseEntity<>(new ObjectMapper().writeValueAsString(reservationDTO), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("La réservation a échoué, vérifiez les infromations fournies.", HttpStatus.BAD_REQUEST);
            }
        } catch (HttpClientErrorException.BadRequest e) {
            logger.error("Erreur de requête : {}", e.getMessage());
            return new ResponseEntity<>("Erreur dans la requête à l'API.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Erreur interne : {}", e.getMessage());
            return new ResponseEntity<>("Erreur interne du serveur.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
