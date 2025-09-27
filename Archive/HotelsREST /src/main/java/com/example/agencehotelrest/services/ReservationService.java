package com.example.agencehotelrest.services;


import com.example.agencehotelrest.dto.ReservationDTO;
import com.example.agencehotelrest.exceptions.BadDatesException;
import com.example.agencehotelrest.exceptions.NoRoomsFoundException;
import com.example.agencehotelrest.exceptions.OffreNotFoundException;
import com.example.agencehotelrest.models.*;
import com.example.agencehotelrest.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.agencehotelrest.util.DateUtil.stringToDate;


@Service
public class ReservationService {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private AgenceRepository agenceRepository;
    @Autowired
    private OffreRepository offreRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private ChambreRepository chambreRepository;

    public ReservationService() {}

    public Boolean grantAccess(int id, String pwd) {
        return agenceRepository.findById(id)
                .map(agence -> agence.getPassword().equals(pwd))
                .orElse(false);
    }

    public Boolean existsClient(String nom, String prenom, int idHotel) {
        return clientRepository.findAll().stream()
                .anyMatch(c -> c.getNom().equals(nom) &&
                        c.getPrenom().equals(prenom) &&
                        c.getHotels().stream()
                                .anyMatch(hotel -> hotel.getId() == idHotel));
    }

    public Client ajouterClient(String nom, String prenom, String email, int idHotel) {
        Optional<Hotel> hotelOptional = hotelRepository.findById(idHotel);
        if (hotelOptional.isEmpty()) {
            throw new RuntimeException("Hotel not found");
        }

        Hotel hotel = hotelOptional.get();

        // Check if the client already exists in the hotel
        boolean clientExists = existsClient(nom, prenom, idHotel);
        if (clientExists) {
            return clientRepository.findAll().stream()
                    .filter(c -> c.getNom().equals(nom) && c.getPrenom().equals(prenom))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Client not found"));
        }

        // Create a new client if not exists
        Client client = new Client();
        client.setNom(nom);
        client.setPrenom(prenom);
        client.setEmail(email);

        hotel.addClient(client);  // Associe le client à l'hôtel
        client.addHotel(hotel);   // Ajoute l'hôtel au client

        clientRepository.save(client);
        hotelRepository.save(hotel);

        return client;
    }

    @Transactional
    public boolean isReservationPossible(int idOffre, int nbPersonnes, String dateDebut, String dateFin) {
        Offre offre = getValidOffre(idOffre);
        validateOffreDates(offre);
        Date dateDebutParsed = stringToDate(dateDebut);
        Date dateFinParsed = stringToDate(dateFin);

        List<Chambre> chambresLibres = getChambresDisponibles(offre.getHotel().getId(), offre.getTypeChambre(), dateDebutParsed, dateFinParsed);
        int capacite = chambresLibres.stream().mapToInt(Chambre::getNbPersonnes).sum();

        return capacite >= nbPersonnes;
    }

    @Transactional
    public ReservationDTO effectuerReservation(int idOffre,
                                               int idHotel,
                                               int idAgence,
                                               String nomClient,
                                               String prenomClient,
                                               String emailClient,
                                               String dateDebut,
                                               String dateFin,
                                               int nbPersonnesInt)
            throws NoRoomsFoundException {

        if (!isReservationPossible(idOffre, nbPersonnesInt, dateDebut, dateFin)) {
            throw new NoRoomsFoundException("Pas de chambres disponibles ou offre non valable.");
        }

        Client client = clientRepository.findAll().stream()
                .filter(c -> c.getNom().equals(nomClient) && c.getPrenom().equals(prenomClient) &&
                        c.getHotels().stream().anyMatch(hotel -> hotel.getId() == idHotel))
                .findFirst()
                .orElseGet(() -> ajouterClient(nomClient, prenomClient, emailClient, idAgence));

        Offre offre = getValidOffre(idOffre);
        Date dateCheckin = stringToDate(dateDebut);
        Date dateCheckout = stringToDate(dateFin);

        double prix = calculateTotalPrice(offre, dateCheckin, dateCheckout);
        List<Chambre> chambresAReserver = selectChambresForReservation(
                idHotel,
                idOffre,
                dateCheckin,
                dateCheckout,
                nbPersonnesInt
        );

        Reservation res = createReservation(idAgence, offre, client, dateDebut, dateFin, prix, chambresAReserver);
        int reservationId = res.getId();

        ReservationDTO reservationDTO = new ReservationDTO(reservationId, nomClient, prenomClient, emailClient, dateDebut, dateFin, prix);

        return reservationDTO;
    }

    private Offre getValidOffre(int idOffre) throws OffreNotFoundException {
        return offreRepository.findById(idOffre)
                .orElseThrow(() -> new OffreNotFoundException("Offre inexistante"));
    }

    private void validateOffreDates(Offre offre) throws OffreNotFoundException {
        Date now = new Date();
        Date dateDebutOffre = stringToDate(offre.getDateDebutOffre());
        Date dateFinOffre = stringToDate(offre.getDateFinOffre());

        if (dateFinOffre.before(now) || dateDebutOffre.after(now)) {
            throw new OffreNotFoundException("Offre expirée ou pas encore valable.");
        }
    }

    private List<Chambre> getChambresDisponibles(int hotelId, int typeChambre, Date dateDebut, Date dateFin) {
        return chambreRepository.findAll().stream()
                .filter(chambre -> chambre.getHotel().getId() == hotelId)
                .filter(chambre -> chambre.getNbPersonnes() == typeChambre)
                .filter(chambre -> isRoomAvailable(chambre, dateDebut, dateFin))
                .collect(Collectors.toList());
    }

    private boolean isRoomAvailable(Chambre chambre, Date dateDebut, Date dateFin) {
        return chambre.getReservations().stream()
                .allMatch(reservation ->
                        dateDebut.after(stringToDate(reservation.getCheckout())) ||
                                dateFin.before(stringToDate(reservation.getCheckin())));
    }

    private double calculateTotalPrice(Offre offre, Date dateCheckin, Date dateCheckout) {
        double prixParNuit = getPricePerNight(offre);
        long numberOfDays = (dateCheckout.getTime() - dateCheckin.getTime()) / (1000 * 60 * 60 * 24);
        double pourcentageReduction = offre.getPourcentageReduction();

        return prixParNuit * numberOfDays * (1 - (pourcentageReduction / 100));
    }

    private double getPricePerNight(Offre offre) throws BadDatesException {
        double prixParNuit = chambreRepository.findAll().stream()
                .filter(chambre -> chambre.getNbPersonnes() == offre.getTypeChambre())
                .map(Chambre::getPrix)
                .findFirst()
                .orElse(0.0);

        if (prixParNuit == 0.0) {
            throw new BadDatesException("Prix par nuit non trouvé.");
        }
        return prixParNuit;
    }

    private List<Chambre> selectChambresForReservation(int hotelId, int offreId, Date dateCheckin, Date dateCheckout, int nbPersonnes)
            throws OffreNotFoundException, NoRoomsFoundException {

        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new OffreNotFoundException("Offre inexistante"));

        List<Chambre> chambresDisponibles = getChambresDisponibles(hotelId, offre.getTypeChambre(), dateCheckin, dateCheckout);

        List<Chambre> chambresAReserver = new ArrayList<>();
        int personnesRestantes = nbPersonnes;

        chambresDisponibles.sort((c1, c2) -> Integer.compare(c2.getNbPersonnes(), c1.getNbPersonnes()));

        for (Chambre chambre : chambresDisponibles) {
            if (personnesRestantes <= 0) break;

            if (chambre.getNbPersonnes() <= personnesRestantes) {
                chambresAReserver.add(chambre);
                personnesRestantes -= chambre.getNbPersonnes();
            }
        }

        if (personnesRestantes > 0) {
            throw new NoRoomsFoundException("Pas assez de chambres disponibles.");
        }

        return chambresAReserver;
    }

    private Reservation createReservation(int idAgence, Offre offre, Client client, String dateDebut, String dateFin, double prix, List<Chambre> chambres) {

        if (chambres.isEmpty()) {
            throw new NoRoomsFoundException("Pas de chambres disponibles.");
        }

        Reservation res = new Reservation();
        res.setOffre(offre);
        res.setClient(client);
        res.setCheckin(dateDebut);
        res.setCheckout(dateFin);
        res.setTotal(prix);
        res.setAgence(agenceRepository.findById(idAgence).orElseThrow(() -> new RuntimeException("Agence non trouvée")));


        chambres.forEach(chambre -> res.addChambre(chambre));
        reservationRepository.save(res);
        return res;
    }

}

