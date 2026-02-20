package ru.mitrohinayulya.zabotushka.service.platform;

public interface PlatformMessageService {
    void sendMessage(Long recipientId, String text);
}
