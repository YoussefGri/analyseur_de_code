package com.example.agencehotelrest.repositories;

import com.example.agencehotelrest.models.CarteBancaire;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarteBancaireRepository extends JpaRepository<CarteBancaire, Integer> {
}
