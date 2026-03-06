package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SetChatMemberTagRequest(
        @JsonProperty("chat_id") long chatId,
        @JsonProperty("user_id") long userId,
        @JsonProperty("tag") String tag
) {
    public static SetChatMemberTagRequest of(long chatId, long userId, String tag) {
        return new SetChatMemberTagRequest(chatId, userId, tag);
    }
}
