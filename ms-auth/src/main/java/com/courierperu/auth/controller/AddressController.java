package com.courierperu.auth.controller;

import com.courierperu.auth.entity.Address;
import com.courierperu.auth.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/direcciones")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // ✨ Ahora recibimos el usuario por el Header "X-User-Name"
    @GetMapping
    public ResponseEntity<List<Address>> misDirecciones(
            @RequestHeader("X-User-Name") String username) {
        return ResponseEntity.ok(addressService.listarMisDirecciones(username));
    }

    @PostMapping
    public ResponseEntity<Address> agregarDireccion(
            @RequestBody Address address,
            @RequestHeader("X-User-Name") String username) {
        return ResponseEntity.ok(addressService.agregarDireccion(username, address));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Address> editDireccion(
            @PathVariable Long id,
            @RequestBody Address address,
            @RequestHeader("X-User-Name") String username) {
        return ResponseEntity.ok(addressService.editDireccion(id, username, address));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDireccion(
            @PathVariable Long id,
            @RequestHeader("X-User-Name") String username) {
        addressService.eliminarDireccion(id, username);
        return ResponseEntity.noContent().build();
    }
}