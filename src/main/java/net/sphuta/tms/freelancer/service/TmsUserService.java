package net.sphuta.tms.freelancer.service;

import net.sphuta.tms.freelancer.dto.TmsUserDto;

import java.util.List;

public interface TmsUserService {

    TmsUserDto createUser(TmsUserDto request);

    TmsUserDto getUserById(int id);

    List<TmsUserDto> getAllUsers();

    TmsUserDto updateUser(int id, TmsUserDto request);

    void deleteUser(int id);
}
