package com.bike_assembly.repository;

import com.bike_assembly.domain.OtpVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificacionDao extends JpaRepository<OtpVerificacion, Long> {

    Optional<OtpVerificacion> findByIdUsuario(Long idUsuario);
}
