package com.bike_assembly.controller;

import com.bike_assembly.domain.Producto; 
import com.bike_assembly.repository.ProductoDao; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
