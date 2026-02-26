package ru.mitrohinayulya.zabotushka.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedVkUser;

import java.time.LocalDateTime;

@Mapper(config = MapStructConfig.class)
public interface AuthorizedVkUserMapper {

    @Mapping(target = "id", ignore = true)
    AuthorizedVkUser toEntity(long vkId, long greenwayId, String regDate, LocalDateTime creationDate);
}
