package com.courierperu.auth.controller;

import com.courierperu.auth.entity.Address;
import com.courierperu.auth.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/auth/direcciones")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // ✨ Extrae el usuario del Token JWT automáticamente
    @GetMapping
    public ResponseEntity<List<Address>> misDirecciones(Principal principal) {
        return ResponseEntity.ok(addressService.listarMisDirecciones(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<Address> agregarDireccion(@RequestBody Address address, Principal principal) {
        return ResponseEntity.ok(addressService.agregarDireccion(principal.getName(), address));
    }
}