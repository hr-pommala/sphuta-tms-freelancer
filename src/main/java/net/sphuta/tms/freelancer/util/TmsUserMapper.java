package net.sphuta.tms.freelancer.util;

import net.sphuta.tms.freelancer.dto.TmsUserDto;
import net.sphuta.tms.freelancer.entity.UserEntity;

import java.util.Optional;

public class TmsUserMapper {

    public static UserEntity toEntity(TmsUserDto request) {
        // When creating, id may be null — builder handles it.
        return UserEntity.builder()
                .id(request.id())
                .email(request.email())
                .passwordHash(request.passwordHash())
                .fullName(request.fullName())
                .phone(request.phone())
                .status(request.status())
                .emailVerified(request.emailVerified() != null ? request.emailVerified() : false)
                .timezone(request.timezone())
                .locale(request.locale())
                .currency(request.currency())
                .avatarUrl(request.avatarUrl())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();
    }

    public static TmsUserDto toResponse(UserEntity userEntity) {
        return new TmsUserDto(
                userEntity.getId(),
                userEntity.getEmail(),
                null, // do NOT expose passwordHash in responses
                userEntity.getFullName(),
                userEntity.getPhone(),
                userEntity.getStatus(),
                userEntity.isEmailVerified(),
                userEntity.getTimezone(),
                userEntity.getLocale(),
                userEntity.getCurrency(),
                userEntity.getAvatarUrl(),
                userEntity.getIsActive(),
                userEntity.getCreatedDt(),
                userEntity.getUpdateDt()
        );
    }

    /**
     * Updates an existing UserEntity with values from a TmsUserDto request.
     * This is used in update operations to avoid direct use of request objects inside service layer.
     */
    public static void updateEntityFromDto(TmsUserDto dto, UserEntity entity) {
        entity.setEmail(dto.email());
        Optional.ofNullable(dto.passwordHash()).ifPresent(entity::setPasswordHash);
        entity.setFullName(dto.fullName());
        entity.setPhone(dto.phone());
        entity.setStatus(dto.status());
        Optional.ofNullable(dto.emailVerified())
                .ifPresentOrElse(entity::setEmailVerified, () -> entity.setEmailVerified(entity.isEmailVerified()));
        entity.setTimezone(dto.timezone());
        entity.setLocale(dto.locale());
        entity.setCurrency(dto.currency());
        entity.setAvatarUrl(dto.avatarUrl());
        Optional.ofNullable(dto.isActive())
                .ifPresentOrElse(entity::setIsActive, () -> entity.setIsActive(entity.getIsActive()));
    }
}
