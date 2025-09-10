package net.sphuta.tms.freelancer.service;

import net.sphuta.tms.freelancer.dto.TmsUserDto;

import java.util.List;

public interface TmsUserService {
    TmsUserDto createUser(TmsUserDto request);
    TmsUserDto getUserById(Integer id);
    List<TmsUserDto> getAllUsers();
    TmsUserDto updateUser(Integer id, TmsUserDto request);
    TmsUserDto partialUpdateUser(Integer id, TmsUserDto request);
    void deleteUser(Integer id);
}
