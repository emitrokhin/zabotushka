package ru.mitrohinayulya.zabotushka.service.platform;

public interface PlatformMessageService {
    void sendMessage(long recipientId, String text);
}
