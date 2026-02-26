package com.courierperu.auth.repository;

import com.courierperu.auth.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    // Busca todas las direcciones asociadas a un ID de usuario específico
    List<Address> findByUserId(Long userId);
    Optional<Address> findByIdAndUserId(Long id, Long userId);
}