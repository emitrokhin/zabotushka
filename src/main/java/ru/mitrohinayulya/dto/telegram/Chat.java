package ru.mitrohinayulya.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This object represents a chat.
 *
 * @see <a href="https://core.telegram.org/bots/api#chat">Chat</a>
 */
public record Chat(
        /**
         * Unique identifier for this chat.
         * This number may have more than 32 significant bits.
         */
        @JsonProperty("id")
        Long id,

        /**
         * Type of the chat, can be either "private", "group", "supergroup" or "channel".
         */
        @JsonProperty("type")
        String type,

        /**
         * Optional. Title, for supergroups, channels and group chats.
         */
        @JsonProperty("title")
        String title,

        /**
         * Optional. Username, for private chats, supergroups and channels if available.
         */
        @JsonProperty("username")
        String username,

        /**
         * Optional. First name of the other party in a private chat.
         */
        @JsonProperty("first_name")
        String firstName,

        /**
         * Optional. Last name of the other party in a private chat.
         */
        @JsonProperty("last_name")
        String lastName,

        /**
         * Optional. True, if the supergroup chat is a forum (has topics enabled).
         */
        @JsonProperty("is_forum")
        Boolean isForum,

        /**
         * Optional. True, if the chat is the direct messages chat of a channel.
         */
        @JsonProperty("is_direct_messages")
        Boolean isDirectMessages
) { }
