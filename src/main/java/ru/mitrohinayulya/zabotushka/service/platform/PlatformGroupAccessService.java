package ru.mitrohinayulya.zabotushka.service.platform;

public interface PlatformGroupAccessService {
    boolean isMemberOfChat(Long chatId, Long userId);
    void removeMemberFromChat(Long chatId, Long userId);
    void checkAndRemoveIfNotQualified(Long chatId, Long userId, Long greenwayId);
}
