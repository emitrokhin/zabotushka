package ru.mitrohinayulya.zabotushka.membership.infrastructure.config;

import jakarta.enterprise.context.ApplicationScoped;
import ru.mitrohinayulya.zabotushka.config.MaxChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.config.TelegramChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.membership.domain.GroupRequirements;
import ru.mitrohinayulya.zabotushka.membership.domain.GroupRequirementsRegistry;
import ru.mitrohinayulya.zabotushka.shared.domain.Platform;
import ru.mitrohinayulya.zabotushka.shared.domain.QualificationLevel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/// Transitional implementation of {@link GroupRequirementsRegistry} backed by the
/// existing {@code TelegramChatGroupRequirements} and {@code MaxChatGroupRequirements} enums.
///
/// <h2>Migration path</h2>
/// This implementation lets all domain and application code migrate to
/// {@code GroupRequirementsRegistry} immediately, without touching the enum config yet.
/// In a follow-up step, replace this class with a {@code YamlGroupRequirementsRegistry}
/// or {@code DatabaseGroupRequirementsRegistry} and delete the config enums.
///
/// <h2>Adding VK</h2>
/// Add a {@code VkChatGroupRequirements} enum (or config entry) and handle
/// {@code Platform.VK} in the switch below. No other class changes.
@ApplicationScoped
public class EnumBackedGroupRequirementsRegistry implements GroupRequirementsRegistry {

    @Override
    public Optional<GroupRequirements> findByChatId(long chatId, Platform platform) {
        return switch (platform) {
            case TELEGRAM -> TelegramChatGroupRequirements.findByChatId(chatId)
                    .map(e -> toGroupRequirements(e.getRequirements()));
            case MAX -> MaxChatGroupRequirements.findByChatId(chatId)
                    .map(e -> toGroupRequirements(e.getRequirements()));
            case VK -> Optional.empty(); // Implement when VK is added
        };
    }

    @Override
    public List<Long> chatIdsForPlatform(Platform platform) {
        return switch (platform) {
            case TELEGRAM -> Arrays.stream(TelegramChatGroupRequirements.values())
                    .map(TelegramChatGroupRequirements::getChatId).toList();
            case MAX -> Arrays.stream(MaxChatGroupRequirements.values())
                    .map(MaxChatGroupRequirements::getChatId).toList();
            case VK -> List.of();
        };
    }

    @Override
    public String resolveGroupName(long chatId, Platform platform) {
        return switch (platform) {
            case TELEGRAM -> TelegramChatGroupRequirements.resolveGroupName(chatId);
            case MAX -> MaxChatGroupRequirements.resolveGroupName(chatId);
            case VK -> "клуб";
        };
    }

    private GroupRequirements toGroupRequirements(ru.mitrohinayulya.zabotushka.config.ChatGroupRequirements req) {
        var levels = req.getAllowedQualifications().stream()
                .map(old -> QualificationLevel.valueOf(old.name()))
                .toList();
        return new GroupRequirements(req.getGroupName(), levels);
    }
}
