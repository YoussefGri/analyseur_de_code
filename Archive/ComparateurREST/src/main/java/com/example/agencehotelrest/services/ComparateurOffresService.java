//package com.example.agencehotelrest.services;
//
//
//import com.example.agencehotelrest.dto.OffreDTO;
//import com.example.agencehotelrest.dto.OffreToCompareDTO;
//import com.example.agencehotelrest.exceptions.BadDatesException;
//import com.example.agencehotelrest.exceptions.OffreNotFoundException;
//import com.example.agencehotelrest.models.Chambre;
//import com.example.agencehotelrest.models.Offre;
//import com.example.agencehotelrest.repositories.AgenceRepository;
//import com.example.agencehotelrest.repositories.ChambreRepository;
//import com.example.agencehotelrest.repositories.HotelRepository;
//import com.example.agencehotelrest.repositories.OffreRepository;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.SimpleTimeZone;
//import java.util.stream.Collectors;
//
//import static com.example.agencehotelrest.util.DateUtil.stringToDate;
//
//@Service
//public class ComparateurOffresService {
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
//    ComparateurOffresService() {}
//
//
//    @Transactional
//    public List<OffreToCompareDTO> getOffresDetails(String ville, String dateDebut, String dateFin, int nbPersonnes, int nbEtoiles) throws OffreNotFoundException {
//        List<Offre> offres = offreRepository.findAll().stream()
//                .filter(offre -> offre.getHotel().getAdresse().getVille().equals(ville))// Filtre par ville
//                .filter(offre -> offre.getHotel().getNbEtoiles() >= nbEtoiles) // Filtre par nombre d'étoiles
//                .filter(offre -> stringToDate(offre.getDateDebutOffre()).before(new Date()) || stringToDate(offre.getDateDebutOffre()).equals(new Date()))
//                .filter(offre -> stringToDate(offre.getDateFinOffre()).after(new Date()))
//                .filter(offre -> hasEnoughAvailableRooms(offre, stringToDate(dateDebut), stringToDate(dateFin), nbPersonnes))
//                .collect(Collectors.toList());
//
//        if (offres.isEmpty()) {
//            throw new OffreNotFoundException("Pas d'offres disponibles pour vos critères.");
//        }
//
//        List<OffreToCompareDTO> offresDTO = new ArrayList<>();
//        for (Offre offre : offres) {
//            double prix = getPrixByOffreId(offre.getId(), dateDebut, dateFin);
//            offresDTO.add(new OffreToCompareDTO(offre, prix));
//        }
//
//        return offresDTO;
//    }
//
//    public double getPrixByOffreId(int idOffre, String dateDebut, String dateFin)
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
//
//
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
//
//
//}
