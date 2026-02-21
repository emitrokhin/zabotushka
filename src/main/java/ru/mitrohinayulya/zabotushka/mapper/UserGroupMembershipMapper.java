package ru.mitrohinayulya.zabotushka.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.entity.UserGroupMembership;

import java.time.LocalDateTime;

@Mapper(config = MapStructConfig.class)
public interface UserGroupMembershipMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "platformUserId", source = "userId")
    @Mapping(target = "lastCheckedAt", ignore = true)
    UserGroupMembership toEntity(long userId, long chatId, Platform platform, LocalDateTime joinedAt);
}
