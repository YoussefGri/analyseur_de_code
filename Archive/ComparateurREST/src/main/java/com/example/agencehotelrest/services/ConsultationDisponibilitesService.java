//package com.example.agencehotelrest.services;
//
//import com.example.agencehotelrest.exceptions.BadCredentialsException;
//import com.example.agencehotelrest.exceptions.BadDatesException;
//import com.example.agencehotelrest.exceptions.HotelNotFoundException;
//import com.example.agencehotelrest.exceptions.OffreNotFoundException;
//import com.example.agencehotelrest.models.*;
//import com.example.agencehotelrest.repositories.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static com.example.agencehotelrest.util.DateUtil.stringToDate;
//
//@Service
//public class ConsultationDisponibilitesService {
//
//    @Autowired
//    private HotelRepository hotelRepository;
//
//    @Autowired
//    private ChambreRepository chambreRepository;
//
//    @Autowired
//    private OffreRepository offreRepository;
//
//    @Autowired
//    private ClientRepository clientRepository;
//
//    public ConsultationDisponibilitesService() {}
//
//    @Transactional
//    public boolean grantAccess(int id, String pwd) throws BadCredentialsException, HotelNotFoundException {
//        if (hotelRepository == null) {
//            throw new HotelNotFoundException("Hotel repository is null");
//        }
//
//        return hotelRepository.findAll().stream()
//                .anyMatch(hotel -> hotel.getAgencesPartenaires().stream()
//                        .anyMatch(agence -> agence.getId() == id && agence.getPassword().equals(pwd)));
//    }
///*
//    @Transactional
//    public Client ajouterClient(Client client, int idAgence) {
//
//        boolean clientAlreadyExists = clientRepository.findAll().stream()
//                .anyMatch(c -> c.getNom().equals(client.getPrenom()) && c.getPrenom().equals(client.getNom()));
//
//        if (clientAlreadyExists) {
//            return clientRepository.findAll().stream()
//                    .filter(c -> c.getNom().equals(client.getPrenom()) && c.getPrenom().equals(client.getNom()))
//                    .findFirst()
//                    .orElse(null);
//        }
//
//        return clientRepository.save(client);
//    }
//
//    @Transactional
//    public List<Integer> getOffresByAgenceId(int idAgence, String dateDebut, String dateFin, int nbPersonnes)
//            throws OffreNotFoundException {
//
//        List<Offre> offresAgence = offreRepository.findAll().stream()
//                .filter(offre -> offre.getAgence().getId() == idAgence)
//                .collect(Collectors.toList());
//
//        if (offresAgence.isEmpty()) {
//            throw new OffreNotFoundException("Aucune offre trouvée pour cette agence.");
//        }
//
//        Date dateDebutParam = stringToDate(dateDebut);
//        Date dateFinParam = stringToDate(dateFin);
//
//        List<Integer> offresDisponibles = offresAgence.stream()
//                .filter(offre -> stringToDate(offre.getDateDebutOffre()).before(new Date()))
//                .filter(offre -> stringToDate(offre.getDateFinOffre()).after(new Date()))
//                .filter(offre -> hasEnoughAvailableRooms(offre, dateDebutParam, dateFinParam, nbPersonnes))
//                .map(Offre::getId)
//                .collect(Collectors.toList());
//
//        if (offresDisponibles.isEmpty()) {
//            throw new OffreNotFoundException("Pas d'offres disponibles pour vos critères.");
//        }
//
//        return offresDisponibles;
//    }
//
// */
//
//    private boolean hasEnoughAvailableRooms(Offre offre, Date dateDebut, Date dateFin, int nbPersonnes) {
//        List<Chambre> chambresHotel = getChambresByHotel(offre.getHotel().getId());
//        List<Chambre> availableRooms = getAvailableRooms(chambresHotel, dateDebut, dateFin);
//
//        int totalCapacity = 0;
//        for (Chambre chambre : availableRooms) {
//            totalCapacity += chambre.getNbPersonnes();
//            if (totalCapacity >= nbPersonnes) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private List<Chambre> getChambresByHotel(int hotelId) {
//        return chambreRepository.findAll().stream()
//                .filter(chambre -> chambre.getHotel().getId() == hotelId)
//                .sorted((c1, c2) -> Integer.compare(c2.getNbPersonnes(), c1.getNbPersonnes()))
//                .collect(Collectors.toList());
//    }
//
//    private List<Chambre> getAvailableRooms(List<Chambre> chambres, Date dateDebut, Date dateFin) {
//        return chambres.stream()
//                .filter(chambre -> isRoomAvailable(chambre, dateDebut, dateFin))
//                .collect(Collectors.toList());
//    }
//
//    private boolean isRoomAvailable(Chambre chambre, Date dateDebut, Date dateFin) {
//        return chambre.getReservations().stream()
//                .noneMatch(reservation -> {
//                    Date debutReservation = stringToDate(reservation.getCheckin());
//                    Date finReservation = stringToDate(reservation.getCheckout());
//                    return !(dateDebut.after(finReservation) || dateFin.before(debutReservation));
//                });
//    }
//
//    public double getPrixByOffreId(int idOffre, String dateDebut, String dateFin, int nbPersonnes)
//            throws OffreNotFoundException {
//
//        Offre offre = offreRepository.findById(idOffre)
//                .orElseThrow(() -> new OffreNotFoundException("Offre non trouvée avec l'ID : " + idOffre));
//
//        validateOffreDates(offre);
//        long numberOfDays = calculateNumberOfDays(dateDebut, dateFin);
//        double pricePerNight = calculatePricePerNight(offre);
//
//        return pricePerNight * numberOfDays;
//    }
//
//    private void validateOffreDates(Offre offre) throws OffreNotFoundException {
//        Date now = new Date();
//        Date dateDebutOffre = stringToDate(offre.getDateDebutOffre());
//        Date dateFinOffre = stringToDate(offre.getDateFinOffre());
//
//        if (dateFinOffre.before(now)) {
//            throw new OffreNotFoundException("Offre expirée.");
//        }
//        if (dateDebutOffre.after(now)) {
//            throw new OffreNotFoundException("Offre pas encore valable.");
//        }
//    }
//
//    private long calculateNumberOfDays(String dateDebut, String dateFin) throws BadDatesException {
//        Date dateCheckin = stringToDate(dateDebut);
//        Date dateCheckout = stringToDate(dateFin);
//
//        long diffInDays = (dateCheckout.getTime() - dateCheckin.getTime()) / (1000 * 60 * 60 * 24);
//
//        if (diffInDays <= 0) {
//            throw new BadDatesException("La date de fin doit être après la date de début.");
//        }
//
//        return diffInDays;
//    }
//
//    private double calculatePricePerNight(Offre offre) {
//        double prixParNuit = chambreRepository.findAll().stream()
//                .filter(chambre -> chambre.getHotel().getId() == offre.getHotel().getId()
//                        && chambre.getNbPersonnes() == offre.getTypeChambre())
//                .findFirst()
//                .map(Chambre::getPrix)
//                .orElse(0.0);
//
//        return prixParNuit;
//    }
//
//    public int countChambresForOffre(int idOffre, String dateDebut, String dateFin) {
//        return offreRepository.findById(idOffre)
//                .map(offre -> {
//                    List<Chambre> chambresHotel = getChambresByHotel(offre.getHotel().getId());
//                    return (int) chambresHotel.stream()
//                            .filter(chambre -> chambre.getNbPersonnes() == offre.getTypeChambre())
//                            .filter(chambre -> isRoomAvailable(chambre,
//                                    stringToDate(dateDebut),
//                                    stringToDate(dateFin)))
//                            .count();
//                })
//                .orElse(0);
//    }
//
//    public int getTypeChambreByIdOffre(int idOffre) {
//        return offreRepository.findById(idOffre)
//                .map(Offre::getTypeChambre)
//                .orElse(0);
//    }
//
//    public String getDateExpirationOffreById(int idOffre) {
//        return offreRepository.findById(idOffre)
//                .map(Offre::getDateFinOffre)
//                .orElse("");
//    }
//
//    public List<Offre> getOffresDetails(int idAgence, String dateDebut, String dateFin, int nbPersonnes) {
//
//        List<Offre> offresAgence = offreRepository.findAll().stream()
//                .filter(offre -> offre.getAgence().getId() == idAgence)
//                .collect(Collectors.toList());
//
//        if (offresAgence.isEmpty()) {
//            throw new OffreNotFoundException("Aucune offre trouvée pour cette agence.");
//        }
//
//        Date dateDebutParam = stringToDate(dateDebut);
//        Date dateFinParam = stringToDate(dateFin);
//
//        List<Offre> offresDisponibles = offresAgence.stream()
//                .filter(offre -> stringToDate(offre.getDateDebutOffre()).before(new Date()))
//                .filter(offre -> stringToDate(offre.getDateFinOffre()).after(new Date()))
//                .filter(offre -> hasEnoughAvailableRooms(offre, dateDebutParam, dateFinParam, nbPersonnes))
//                .collect(Collectors.toList());
//
//        if (offresDisponibles.isEmpty()) {
//            throw new OffreNotFoundException("Pas d'offres disponibles pour vos critères.");
//        }
//
//        return offresDisponibles;
//
//    }
//}
