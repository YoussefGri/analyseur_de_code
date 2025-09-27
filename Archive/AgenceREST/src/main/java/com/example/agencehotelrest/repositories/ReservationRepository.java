package com.example.agencehotelrest.repositories;

import com.example.agencehotelrest.models.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;


@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

}
