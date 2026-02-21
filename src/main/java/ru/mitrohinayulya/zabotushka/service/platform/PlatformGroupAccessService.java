package ru.mitrohinayulya.zabotushka.service.platform;

public interface PlatformGroupAccessService {
    boolean isMemberOfChat(long chatId, long userId);
    void removeMemberFromChat(long chatId, long userId);
    void checkAndRemoveIfNotQualified(long chatId, long userId, long greenwayId);
}
