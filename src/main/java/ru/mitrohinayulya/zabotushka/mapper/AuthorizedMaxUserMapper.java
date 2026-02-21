package ru.mitrohinayulya.zabotushka.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedMaxUser;

import java.time.LocalDateTime;

@Mapper(config = MapStructConfig.class)
public interface AuthorizedMaxUserMapper {

    @Mapping(target = "id", ignore = true)
    AuthorizedMaxUser toEntity(long maxId, long greenwayId, String regDate, LocalDateTime creationDate);
}
