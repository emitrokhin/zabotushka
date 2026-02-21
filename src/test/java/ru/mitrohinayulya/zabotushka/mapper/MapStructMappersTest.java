package ru.mitrohinayulya.zabotushka.mapper;

import org.mapstruct.factory.Mappers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mitrohinayulya.zabotushka.entity.Platform;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MapStructMappersTest {

    private final AuthorizedTelegramUserMapper authorizedTelegramUserMapper =
            Mappers.getMapper(AuthorizedTelegramUserMapper.class);
    private final AuthorizedMaxUserMapper authorizedMaxUserMapper =
            Mappers.getMapper(AuthorizedMaxUserMapper.class);
    private final UserGroupMembershipMapper userGroupMembershipMapper =
            Mappers.getMapper(UserGroupMembershipMapper.class);

    @Test
    @DisplayName("AuthorizedTelegramUserMapper maps all expected fields and leaves id empty")
    void authorizedTelegramUserMapper_ShouldMapExpectedFields() {
        var creationDate = LocalDateTime.of(2026, 2, 21, 12, 0);

        var entity = authorizedTelegramUserMapper.toEntity(123L, 456L, "21.02.2026", creationDate);

        assertThat(entity.id).as("id should be null").isNull();
        assertThat(entity.telegramId).as("telegramId should be mapped").isEqualTo(123L);
        assertThat(entity.greenwayId).as("greenwayId should be mapped").isEqualTo(456L);
        assertThat(entity.regDate).as("regDate should be mapped").isEqualTo("21.02.2026");
        assertThat(entity.creationDate).as("creationDate should be mapped").isEqualTo(creationDate);
    }

    @Test
    @DisplayName("AuthorizedMaxUserMapper maps all expected fields and leaves id empty")
    void authorizedMaxUserMapper_ShouldMapExpectedFields() {
        var creationDate = LocalDateTime.of(2026, 2, 21, 12, 30);

        var entity = authorizedMaxUserMapper.toEntity(987L, 654L, "22.02.2026", creationDate);

        assertThat(entity.id).as("id should be null").isNull();
        assertThat(entity.maxId).as("maxId should be mapped").isEqualTo(987L);
        assertThat(entity.greenwayId).as("greenwayId should be mapped").isEqualTo(654L);
        assertThat(entity.regDate).as("regDate should be mapped").isEqualTo("22.02.2026");
        assertThat(entity.creationDate).as("creationDate should be mapped").isEqualTo(creationDate);
    }

    @Test
    @DisplayName("UserGroupMembershipMapper maps expected fields and keeps lastCheckedAt empty")
    void userGroupMembershipMapper_ShouldMapExpectedFields() {
        var joinedAt = LocalDateTime.of(2026, 2, 21, 13, 0);

        var entity = userGroupMembershipMapper.toEntity(111L, -100L, Platform.TELEGRAM, joinedAt);

        assertThat(entity.id).as("id should be null").isNull();
        assertThat(entity.platformUserId).as("platformUserId should be mapped").isEqualTo(111L);
        assertThat(entity.chatId).as("chatId should be mapped").isEqualTo(-100L);
        assertThat(entity.platform).as("platform should be mapped").isEqualTo(Platform.TELEGRAM);
        assertThat(entity.joinedAt).as("joinedAt should be mapped").isEqualTo(joinedAt);
        assertThat(entity.lastCheckedAt).as("lastCheckedAt should be null").isNull();
    }
}
