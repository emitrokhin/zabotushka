package ru.mitrohinayulya.zabotushka.config;

import java.util.Arrays;
import java.util.Optional;

/// Qualification requirements for VK communities bound to platform group IDs.
public enum VkChatGroupRequirements {
    GOLD_CLUB(3L, ChatGroupRequirements.GOLD_CLUB);

    private final long chatId;
    private final ChatGroupRequirements requirements;

    VkChatGroupRequirements(long chatId, ChatGroupRequirements requirements) {
        this.chatId = chatId;
        this.requirements = requirements;
    }

    public long getChatId() {
        return chatId;
    }

    public String getGroupName() {
        return requirements.getGroupName();
    }

    public ChatGroupRequirements getRequirements() {
        return requirements;
    }

    public static Optional<VkChatGroupRequirements> findByChatId(long chatId) {
        return Arrays.stream(values())
                .filter(group -> group.getChatId() == chatId)
                .findFirst();
    }

    public static String resolveGroupName(long chatId) {
        return findByChatId(chatId)
                .map(VkChatGroupRequirements::getGroupName)
                .orElse("клуб");
    }
}
