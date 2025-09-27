package com.example.agencehotelrest.services;

import com.example.agencehotelrest.exceptions.BadCredentialsException;
import com.example.agencehotelrest.exceptions.BadDatesException;
import com.example.agencehotelrest.exceptions.HotelNotFoundException;
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
public class ConsultationDisponibilitesService {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private ChambreRepository chambreRepository;

    @Autowired
    private OffreRepository offreRepository;


    public ConsultationDisponibilitesService() {}

    @Transactional
    public boolean grantAccess(int idHotel, int id, String pwd) throws HotelNotFoundException {
        Hotel hotel = hotelRepository.findById(idHotel)
                .orElse(null);

        if ((hotel != null) && hotel.getAgencesPartenaires().stream().anyMatch(agence -> agence.getId() == id && agence.getPassword().equals(pwd))) {
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public boolean grantAccessNoHotelId(int id, String pwd) {
        List<Hotel> hotels = hotelRepository.findAll();
        for (Hotel hotel : hotels) {
            if (hotel.getAgencesPartenaires().stream().anyMatch(agence -> agence.getId() == id && agence.getPassword().equals(pwd))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasEnoughAvailableRooms(Offre offre, Date dateDebut, Date dateFin, int nbPersonnes) {
        //List<Chambre> chambresHotel = getChambresByHotel(offre.getHotel().getId());
        List<Chambre> chambresHotel = getChambresByHotel(offre.getHotel().getId(), offre.getTypeChambre());
        List<Chambre> availableRooms = getAvailableRooms(chambresHotel, dateDebut, dateFin);

        int totalCapacity = 0;
        for (Chambre chambre : availableRooms) {
            totalCapacity += chambre.getNbPersonnes();
            if (totalCapacity >= nbPersonnes) {
                return true;
            }
        }
        return false;
    }

    private List<Chambre> getChambresByHotel(int hotelId, int typeChambre) {
        return chambreRepository.findAll().stream()
                .filter(chambre -> chambre.getHotel().getId() == hotelId)
                .filter(chambre -> chambre.getNbPersonnes() == typeChambre)
                //.sorted((c1, c2) -> Integer.compare(c2.getNbPersonnes(), c1.getNbPersonnes()))
                .collect(Collectors.toList());
    }

    private List<Chambre> getAvailableRooms(List<Chambre> chambres, Date dateDebut, Date dateFin) {

        List<Chambre> availableRooms = new ArrayList<>();

        if (chambres.isEmpty()) {
            return availableRooms;
        }
        else {
            for (Chambre chambre : chambres) {
                if (isRoomAvailable(chambre, dateDebut, dateFin)) {
                    availableRooms.add(chambre);
                }
            }
        }

        return availableRooms;

    }

    private boolean isRoomAvailable(Chambre chambre, Date dateDebut, Date dateFin) {
        return chambre.getReservations().stream()
                .noneMatch(reservation -> {
                    Date debutReservation = stringToDate(reservation.getCheckin());
                    Date finReservation = stringToDate(reservation.getCheckout());
                    return !(dateDebut.after(finReservation) || dateFin.before(debutReservation));
                });
    }

    public double getPrixByOffreId(int idOffre, String dateDebut, String dateFin, int nbPersonnes)
            throws OffreNotFoundException {

        Offre offre = offreRepository.findById(idOffre)
                .orElseThrow(() -> new OffreNotFoundException("Offre non trouvée avec l'ID : " + idOffre));

        validateOffreDates(offre);
        long numberOfDays = calculateNumberOfDays(dateDebut, dateFin);
        double pricePerNight = calculatePricePerNight(offre);
        double pourcentageReduction = offre.getPourcentageReduction();

        return pricePerNight * numberOfDays * (1 - (pourcentageReduction / 100));
    }

    private void validateOffreDates(Offre offre) throws OffreNotFoundException {
        Date now = new Date();
        Date dateDebutOffre = stringToDate(offre.getDateDebutOffre());
        Date dateFinOffre = stringToDate(offre.getDateFinOffre());

        if (dateFinOffre.before(now)) {
            throw new OffreNotFoundException("Offre expirée.");
        }
        if (dateDebutOffre.after(now)) {
            throw new OffreNotFoundException("Offre pas encore valable.");
        }
    }

    private long calculateNumberOfDays(String dateDebut, String dateFin) throws BadDatesException {
        Date dateCheckin = stringToDate(dateDebut);
        Date dateCheckout = stringToDate(dateFin);

        long diffInDays = (dateCheckout.getTime() - dateCheckin.getTime()) / (1000 * 60 * 60 * 24);

        if (diffInDays <= 0) {
            throw new BadDatesException("La date de fin doit être après la date de début.");
        }

        return diffInDays;
    }

    private double calculatePricePerNight(Offre offre) {
        double prixParNuit = chambreRepository.findAll().stream()
                .filter(chambre -> chambre.getHotel().getId() == offre.getHotel().getId()
                        && chambre.getNbPersonnes() == offre.getTypeChambre())
                .findFirst()
                .map(Chambre::getPrix)
                .orElse(0.0);

        return prixParNuit;
    }

    public int countChambresForOffre(int idOffre, String dateDebut, String dateFin) {
        return offreRepository.findById(idOffre)
                .map(offre -> {
                    List<Chambre> chambresHotel = getChambresByHotel(offre.getHotel().getId(), offre.getTypeChambre());
                    return (int) chambresHotel.stream()
                            .filter(chambre -> chambre.getNbPersonnes() == offre.getTypeChambre())
                            .filter(chambre -> isRoomAvailable(chambre,
                                    stringToDate(dateDebut),
                                    stringToDate(dateFin)))
                            .count();
                })
                .orElse(0);
    }

    public int getTypeChambreByIdOffre(int idOffre) {
        return offreRepository.findById(idOffre)
                .map(Offre::getTypeChambre)
                .orElse(0);
    }

    public String getDateExpirationOffreById(int idOffre) {
        return offreRepository.findById(idOffre)
                .map(Offre::getDateFinOffre)
                .orElse("");
    }

    private int calculateTotalAvailableBeds(Offre offre, Date dateDebut, Date dateFin) {
        // List<Chambre> chambresHotel = getChambresByHotel(offre.getHotel().getId());
        List<Chambre> chambresHotel = getChambresByHotel(offre.getHotel().getId(), offre.getTypeChambre());
        List<Chambre> availableRooms = getAvailableRooms(chambresHotel, dateDebut, dateFin);

        int totalCapacity = 0;
        for (Chambre chambre : availableRooms) {
            totalCapacity += chambre.getNbPersonnes();
        }
        return totalCapacity;
    }


    public List<Offre> getOffresDetails(int idAgence, int idHotel, String dateDebut, String dateFin, int nbPersonnes) {

        List<Offre> offresAgence = hotelRepository.findById(idHotel)
                .map(Hotel::getOffres)
                .orElse(new ArrayList<>());

        offresAgence = offresAgence.stream()
                .filter(offre -> offre.getAgence().getId() == idAgence)
                .collect(Collectors.toList());

        if (offresAgence.isEmpty()) {
            throw new OffreNotFoundException("Aucune offre trouvée pour cette agence.");
        }

        Date dateDebutParam = stringToDate(dateDebut);
        Date dateFinParam = stringToDate(dateFin);

        List<Offre> offres = offresAgence.stream()
                .filter(offre -> stringToDate(offre.getDateDebutOffre()).before(dateDebutParam) || stringToDate(offre.getDateDebutOffre()).equals(dateDebutParam))
                .filter(offre -> stringToDate(offre.getDateFinOffre()).after(dateFinParam) || stringToDate(offre.getDateFinOffre()).equals(dateFinParam))
                //.filter(offre -> hasEnoughAvailableRooms(offre, dateDebutParam, dateFinParam, nbPersonnes))
                .collect(Collectors.toList());

        if (offres.isEmpty()) {
            throw new OffreNotFoundException("Pas d'offres disponibles pour vos critères.");
        }

        offres.sort((o1, o2) -> Integer.compare(o2.getTypeChambre(), o1.getTypeChambre()));

        List<Offre> offresDisponibles = new ArrayList<>();
        int personnesRestantes = nbPersonnes;

        System.err.println("nb offres trouvées : "+offres.size());

        for (Offre offre : offres) {
            // Vérifier si l'offre a des chambres disponibles
            int litsDisponibles = calculateTotalAvailableBeds(offre, dateDebutParam, dateFinParam);
            System.err.println("nb lits dispo : "+litsDisponibles);
            if (litsDisponibles > 0) {
                if (offre.getTypeChambre() <= personnesRestantes) {
                    offresDisponibles.add(offre);
                    personnesRestantes -= offre.getTypeChambre();
                }

                if (personnesRestantes <= 0) {
                    break;
                }
            }
        }

        if (personnesRestantes > 0) {
            throw new OffreNotFoundException("Pas assez de chambres disponibles pour le nombre de personnes demandé.");
        }

                return offresDisponibles;

    }


}
