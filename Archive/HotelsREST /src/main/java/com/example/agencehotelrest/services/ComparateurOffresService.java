package com.example.agencehotelrest.services;


import com.example.agencehotelrest.dto.OffreComparateur;
import com.example.agencehotelrest.exceptions.BadDatesException;
import com.example.agencehotelrest.exceptions.OffreNotFoundException;
import com.example.agencehotelrest.models.Chambre;
import com.example.agencehotelrest.models.Offre;
import com.example.agencehotelrest.repositories.ChambreRepository;
import com.example.agencehotelrest.repositories.HotelRepository;
import com.example.agencehotelrest.repositories.OffreRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.agencehotelrest.util.DateUtil.stringToDate;

@Service
public class ComparateurOffresService {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private ChambreRepository chambreRepository;

    @Autowired
    private OffreRepository offreRepository;

    ComparateurOffresService() {}


    @Transactional
    public List<OffreComparateur> getOffresDetails(int idHotel, int idAgence, String ville, String dateDebut, String dateFin, int nbPersonnes) throws OffreNotFoundException {
        Date dateDebutres = stringToDate(dateDebut);
        Date dateFinres = stringToDate(dateFin);

        // Filtrer les offres par critères
        List<Offre> offres = offreRepository.findAll().stream()
                .filter(offre -> offre.getHotel().getId() == idHotel)
                .filter(offre -> offre.getAgence().getId() == idAgence)
                .filter(offre -> offre.getHotel().getAdresse().getVille().equals(ville))
                .filter(offre -> stringToDate(offre.getDateDebutOffre()).before(dateDebutres) || stringToDate(offre.getDateDebutOffre()).equals(dateDebutres))
                .filter(offre -> stringToDate(offre.getDateFinOffre()).after(dateFinres) || stringToDate(offre.getDateFinOffre()).equals(dateFinres))
                .collect(Collectors.toList());

        if (offres.isEmpty()) {
            throw new OffreNotFoundException("Pas d'offres disponibles pour vos critères.");
        }

        // Trier les offres par la capacité des chambres (ordre décroissant)
        offres.sort((o1, o2) -> Integer.compare(o2.getTypeChambre(), o1.getTypeChambre()));

        List<OffreComparateur> offresComparateur = new ArrayList<>();
        int personnesRestantes = nbPersonnes;

        System.err.println("nb offres trouvées : "+offres.size());

        for (Offre offre : offres) {
            // Vérifier si l'offre a des chambres disponibles
            int litsDisponibles = calculateTotalAvailableBeds(offre, dateDebutres, dateFinres);
            System.err.println("nb lits dispo : "+litsDisponibles);
            if (litsDisponibles > 0) {
                OffreComparateur offreComparateur = new OffreComparateur();

                // Remplir les détails de l'offre
                offreComparateur.setNomHotel(offre.getHotel().getNom());
                String adresseHotel = offre.getHotel().getAdresse().getNumeroRue() + " " + offre.getHotel().getAdresse().getNomRue() + ", " + offre.getHotel().getAdresse().getVille() + ", " + offre.getHotel().getAdresse().getPays();
                offreComparateur.setAdresse(adresseHotel);
                offreComparateur.setNbEtoiles(offre.getHotel().getNbEtoiles());
                offreComparateur.setPrix(getPrixByOffreId(offre.getId(), dateDebut, dateFin));
                offreComparateur.setPourcentageReduction(offre.getPourcentageReduction());
                offreComparateur.setNbLitsDisponibles(litsDisponibles);

                String nomAgence = offre.getHotel().getAgencesPartenaires().stream()
                        .filter(agence -> agence.getId() == idAgence)
                        .findFirst()
                        .map(agence -> agence.getNom())
                        .orElse("Agence non trouvée");
                offreComparateur.setNomAgence(nomAgence);

                offresComparateur.add(offreComparateur);

                // Déduire les lits de cette offre
                personnesRestantes -= litsDisponibles;
                if (personnesRestantes <= 0) {
                    break; // Besoin satisfait
                }
            }
        }

        if (personnesRestantes > 0) {
            throw new OffreNotFoundException("Pas assez de lits disponibles pour satisfaire la demande.");
        }

        return offresComparateur;
    }


    // Méthode pour calculer le nombre total de lits disponibles pour une offre
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


    public double getPrixByOffreId(int idOffre, String dateDebut, String dateFin)
            throws OffreNotFoundException {

        Offre offre = offreRepository.findById(idOffre)
                .orElseThrow(() -> new OffreNotFoundException("Offre non trouvée avec l'ID : " + idOffre));

        //validateOffreDates(offre);

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
                .sorted((c1, c2) -> Integer.compare(c2.getNbPersonnes(), c1.getNbPersonnes()))
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



}
