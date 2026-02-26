package com.courierperu.auth.service;

import com.courierperu.auth.entity.Address;
import com.courierperu.auth.entity.AuthUser;
import com.courierperu.auth.repository.AddressRepository;
import com.courierperu.auth.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final AuthUserRepository authUserRepository;

    public List<Address> listarMisDirecciones(String userName) {
        AuthUser user = authUserRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return addressRepository.findByUserId(user.getId());
    }

    public Address agregarDireccion(String userName, Address nuevaDireccion) {
        AuthUser user = authUserRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Vinculamos la dirección al usuario logueado
        nuevaDireccion.setUser(user);
        return addressRepository.save(nuevaDireccion);
    }

    @Transactional
    public Address editDireccion(Long id, String userName, Address datosActualizados) {
        AuthUser user = authUserRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Address direccionExistente = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada o no te pertenece"));

        // Actualizamos los datos
        direccionExistente.setAlias(datosActualizados.getAlias());
        direccionExistente.setCalle(datosActualizados.getCalle());
        direccionExistente.setDepartamento(datosActualizados.getDepartamento());
        direccionExistente.setProvincia(datosActualizados.getProvincia());
        direccionExistente.setDistrito(datosActualizados.getDistrito());
        direccionExistente.setReferencia(datosActualizados.getReferencia());

        return addressRepository.save(direccionExistente);
    }

    @Transactional
    public void eliminarDireccion(Long id, String userName) {
        AuthUser user = authUserRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Address direccionExistente = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada o no te pertenece"));

        addressRepository.delete(direccionExistente);
    }
}