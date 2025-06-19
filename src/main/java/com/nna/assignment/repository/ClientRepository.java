package com.nna.assignment.repository;

import com.nna.assignment.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByEmail(String email);

    Optional<Client> findByEmailAndIdNot(String email, Long id);
}
