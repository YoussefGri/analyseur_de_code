package com.example.agencehotelrest.repositories;

import com.example.agencehotelrest.models.Offre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OffreRepository extends JpaRepository<Offre, Integer> {

}
