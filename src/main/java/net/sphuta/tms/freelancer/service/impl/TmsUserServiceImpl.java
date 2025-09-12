package net.sphuta.tms.freelancer.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsUserDto;
import net.sphuta.tms.freelancer.exception.ConflictException;
import net.sphuta.tms.freelancer.exception.NotFoundException;
import net.sphuta.tms.freelancer.repository.TmsUserRepository;
import net.sphuta.tms.freelancer.service.TmsUserService;
import net.sphuta.tms.freelancer.util.TmsUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TmsUserServiceImpl implements TmsUserService {

    @Autowired
    private TmsUserRepository tmsUserRepository;

    @Override
    public TmsUserDto createUser(TmsUserDto request) {
        log.info("Creating new user with email {}", request.email());

        tmsUserRepository.findByEmail(request.email()).ifPresent(u -> {
            log.warn("Attempted to create user with existing email {}", request.email());
            throw new ConflictException("Email already exists");
        });

        var saved = tmsUserRepository.save(TmsUserMapper.toEntity(request));
        log.debug("User saved with ID {}", saved.getId());
        return TmsUserMapper.toResponse(saved);
    }

    @Override
    public TmsUserDto getUserById(int id) {
        log.info("Fetching user with ID {}", id);

        return tmsUserRepository.findById(id)
                .map(TmsUserMapper::toResponse)
                .orElseThrow(() -> {
                    log.error("User not found with ID {}", id);
                    return new NotFoundException("User not existed with this id: " + id);
                });
    }

    @Override
    public List<TmsUserDto> getAllUsers() {
        log.info("Fetching all users");
        var users = tmsUserRepository.findAll()
                .stream()
                .map(TmsUserMapper::toResponse)
                .collect(Collectors.toList());
        log.debug("Total users fetched: {}", users.size());
        return users;
    }

    @Override
    public TmsUserDto updateUser(int id, TmsUserDto request) {
        log.info("Updating user with ID {}", id);

        var user = tmsUserRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID {}", id);
                    return new NotFoundException("User not existed with this id: " + id);
                });

        // Update full set of fields using mapper
        TmsUserMapper.updateEntityFromDto(request, user);
        user.setUpdateDt(LocalDateTime.now());

        var updated = tmsUserRepository.save(user);
        log.debug("User updated successfully with ID {}", updated.getId());

        return TmsUserMapper.toResponse(updated);
    }

    @Override
    public void deleteUser(int id) {
        log.info("Deleting user with ID {}", id);

        var user = tmsUserRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID {}", id);
                    return new NotFoundException("User not existed with this id: " + id);
                });

        tmsUserRepository.delete(user);
        log.debug("User deleted successfully with ID {}", id);
    }
}
