package com.example.agencehotelrest.repositories;


import com.example.agencehotelrest.models.Agence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AgenceRepository extends JpaRepository<Agence, Integer> {

}
