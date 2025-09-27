package com.example.agencehotelrest.data;

import com.example.agencehotelrest.models.*;
import com.example.agencehotelrest.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    private Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initDatabase(
            AdresseRepository adresseRepo,
            AgenceRepository agenceRepo,
            HotelRepository hotelRepo) {

        return args -> {
            // Create an address
            Adresse adresse = new Adresse();
            adresse.setPays("France");
            adresse.setVille("Paris");
            adresse.setNomRue("Champs-Élysées");
            adresse.setNumeroRue(101);
            adresseRepo.save(adresse);

            Adresse adresse2 = new Adresse();
            adresse2.setPays("France");
            adresse2.setVille("Marseille");
            adresse2.setNomRue("Rue de la République");
            adresse2.setNumeroRue(50);
            adresseRepo.save(adresse2);

            // Create an agency
            Agence agence = new Agence();
            agence.setNom("Agence Parisienne");
            agence.setPassword("password123");
            agenceRepo.save(agence);

            Agence agence2 = new Agence();
            agence2.setNom("Agence Marseillaise");
            agence2.setPassword("password456");
            agenceRepo.save(agence2);

            // Create a hotel
            Hotel hotel = new Hotel();
            hotel.setNom("Hôtel de Paris");
            hotel.setAdresse(adresse);
            hotel.setNbEtoiles(4);
            hotel.setNbLitsDisponibles(50);
            hotelRepo.save(hotel);


            // Link the hotel with the agency as a partner
            hotel.setAgencesPartenaires(List.of(agence,agence2));
            hotelRepo.save(hotel);

            logger.info("Database initialized with sample data.");
        };
    }
}
