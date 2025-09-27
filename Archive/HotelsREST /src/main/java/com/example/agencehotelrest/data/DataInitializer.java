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
            ChambreRepository chambreRepo,
            OffreRepository offreRepo,
            CarteBancaireRepository carteBancaireRepo) {

        return args -> {
            // Create an address
            Adresse adresse = new Adresse();
            adresse.setPays("France");
            adresse.setVille("Paris");
            adresse.setNomRue("Champs-Élysées");
            adresse.setNumeroRue(101);
            adresseRepo.save(adresse);

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

            // Create rooms for the hotel
            Chambre chambre1 = new Chambre();
            chambre1.setNbPersonnes(2);
            chambre1.setPrix(150.0);
            chambre1.setHotel(hotel);
            chambreRepo.save(chambre1);

            Chambre chambre2 = new Chambre();
            chambre2.setNbPersonnes(3);
            chambre2.setPrix(200.0);
            chambre2.setHotel(hotel);
            chambreRepo.save(chambre2);

            Chambre chambre3 = new Chambre();
            chambre3.setNbPersonnes(1);
            chambre3.setPrix(100.0);
            chambre3.setHotel(hotel);
            chambreRepo.save(chambre3);

            hotel.setChambres(List.of(chambre1, chambre2, chambre3));
            hotelRepo.save(hotel);

            // Create a client
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
            clientRepo.save(client);

            // Create an offer
            Offre offre = new Offre();
            offre.setDateDebutOffre("2024-11-01");
            offre.setDateFinOffre("2024-12-01");
            offre.setTypeChambre(2); // Double
            offre.setPourcentageReduction(10.0);
            offre.setHotel(hotel);
            offre.setAgence(agence);
            offreRepo.save(offre);

            Offre offre2 = new Offre();
            offre2.setDateDebutOffre("2024-11-01");
            offre2.setDateFinOffre("2024-12-01");
            offre2.setTypeChambre(2); // Double
            offre2.setPourcentageReduction(20.0);
            offre2.setHotel(hotel);
            offre2.setAgence(agence2);
            offreRepo.save(offre2);

            Offre offre3 = new Offre();
            offre3.setDateDebutOffre("2024-11-01");
            offre3.setDateFinOffre("2024-12-01");
            offre3.setTypeChambre(1); // Double`
            offre3.setPourcentageReduction(10.0);
            offre3.setHotel(hotel);
            offre3.setAgence(agence);
            offreRepo.save(offre3);

            // Associate hotels with client
            hotel.setClients(List.of(client));
            hotelRepo.save(hotel);
            client.setHotels(List.of(hotel));
            clientRepo.save(client);

            // Link the hotel with the agency as a partner
            hotel.setAgencesPartenaires(List.of(agence, agence2));
            hotelRepo.save(hotel);
            agence.setHotelsPartenaires(List.of(hotel));
            agenceRepo.save(agence);
            agence2.setHotelsPartenaires(List.of(hotel));
            agenceRepo.save(agence2);

            logger.info("Database initialized with sample data.");
        };
    }
}





