package com.bike_assembly.repository;

import com.bike_assembly.domain.Organizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizacionDao extends JpaRepository<Organizacion, Long> {
}