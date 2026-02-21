package ru.mitrohinayulya.zabotushka.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedTelegramUser;

import java.time.LocalDateTime;

@Mapper(config = MapStructConfig.class)
public interface AuthorizedTelegramUserMapper {

    @Mapping(target = "id", ignore = true)
    AuthorizedTelegramUser toEntity(long telegramId, long greenwayId, String regDate, LocalDateTime creationDate);
}
