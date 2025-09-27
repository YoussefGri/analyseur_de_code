package com.example.agencehotelrest.controllers;


import com.example.agencehotelrest.dto.OffreComparateurDTO;
import com.example.agencehotelrest.models.Agence;
import com.example.agencehotelrest.repositories.AgenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:8082")
@RestController
@RequestMapping("/compare")
public class ComparateurController {

    @Autowired
    private AgenceRepository agenceRepository;


    @PostMapping("/offres")
    public ResponseEntity<?> getOffresByVille(
            @RequestParam String ville,
            @RequestParam String dateDebut,
            @RequestParam String dateFin,
            @RequestParam int nbPersonnes,
            @RequestParam int nbEtoiles) {

        List<Agence> agences = agenceRepository.findAll();

        // Vérification : aucune agence trouvée
        if (agences.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"Aucune agence trouvée.\"}");
        }

        RestTemplate restTemplate = new RestTemplate();
        List<OffreComparateurDTO> offresCombinees = new ArrayList<>();

        for (Agence agence : agences) {
            String apiUrl = String.format("http://localhost:8080/agences/%d/comparateur", agence.getId());

            try {
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl)
                        .queryParam("ville", ville)
                        .queryParam("dateDebut", dateDebut)
                        .queryParam("dateFin", dateFin)
                        .queryParam("nbPersonnes", nbPersonnes)
                        .queryParam("nbEtoiles", nbEtoiles);

                ResponseEntity<OffreComparateurDTO[]> response =
                        restTemplate.getForEntity(builder.toUriString(), OffreComparateurDTO[].class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    offresCombinees.addAll(List.of(response.getBody()));
                }
            } catch (Exception e) {
                // En cas d'erreur dans l'appel d'une agence spécifique, continuer avec les autres agences
                 System.err.println("Erreur lors de l'appel à l'agence : " + agence.getId() + " - " + e.getMessage());


            }
        }

        if (offresCombinees.isEmpty()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Aucune offre ne correspond aux critères spécifiés.\"}");

        }

        return ResponseEntity.ok(offresCombinees);
    }



}
