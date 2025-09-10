package net.sphuta.tms.freelancer.util;

import net.sphuta.tms.freelancer.dto.TmsUserDto;
import net.sphuta.tms.freelancer.entity.UserEntity;

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
                userEntity.getCreatedAt(),
                userEntity.getUpdatedAt()
        );
    }
}
