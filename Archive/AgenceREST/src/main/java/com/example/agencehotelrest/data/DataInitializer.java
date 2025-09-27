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
            HotelRepository hotelRepo,
            ClientRepository clientRepo,

            CarteBancaireRepository carteBancaireRepo) {

        return args -> {

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

            Adresse adresse3 = new Adresse();
            adresse3.setPays("France");
            adresse3.setVille("Lyon");
            adresse3.setNomRue("Rue de la République");
            adresse3.setNumeroRue(50);
            adresseRepo.save(adresse3);

            Agence agence = new Agence();
            agence.setNom("Agence Parisienne");
            agence.setPassword("password123");
            agence.setAdresse(adresse2);
            agenceRepo.save(agence);

            Agence agence2 = new Agence();
            agence2.setNom("Agence Marseillaise");
            agence2.setPassword("password456");
            agence2.setAdresse(adresse3);
            agenceRepo.save(agence2);

            Hotel hotel = new Hotel();
            hotel.setNom("Hôtel de Paris");
            hotel.setAdresse(adresse);
            hotel.setNbEtoiles(4);
            hotel.setNbLitsDisponibles(50);
            hotelRepo.save(hotel);


            Client client = new Client();
            client.setNom("Dupont");
            client.setPrenom("Jean");
            client.setEmail("jean.dupont@example.com");
            clientRepo.save(client);

            // Create multiple CarteBancaire instances and associate them with the client
            CarteBancaire carteBancaire1 = new CarteBancaire();
            carteBancaire1.setNumero(1234567890123456L);
            carteBancaire1.setCvv(123);
            carteBancaire1.setDateExpiration("12/24");
            carteBancaire1.setClient(client);
            carteBancaireRepo.save(carteBancaire1);

            CarteBancaire carteBancaire2 = new CarteBancaire();
            carteBancaire2.setNumero(9876543210123456L);
            carteBancaire2.setCvv(456);
            carteBancaire2.setDateExpiration("10/25");
            carteBancaire2.setClient(client);
            carteBancaireRepo.save(carteBancaire2);

            // Link cards to the client
            client.setCartesBancaires(List.of(carteBancaire1, carteBancaire2));
            clientRepo.save(client);


            // Associate agency with client
            agence.setClients(List.of(client));
            agenceRepo.save(agence);
            client.setAgences(List.of(agence));
            clientRepo.save(client);

            // Link the hotel with the agency as a partner
            agence.addHotelPartenaire(hotel);
            agenceRepo.save(agence);

            agence2.addHotelPartenaire(hotel);
            agenceRepo.save(agence2);

            logger.info("Database initialized with sample data.");
        };
    }
}
