package com.example.agencehotelrest.repositories;

import com.example.agencehotelrest.models.Chambre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ChambreRepository extends JpaRepository<Chambre, Integer> {

}
