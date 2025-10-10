package ru.mitrohinayulya.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This object represents a Telegram user or bot.
 *
 * @see <a href="https://core.telegram.org/bots/api#user">User</a>
 */
public record User(
        /**
         * Unique identifier for this user or bot.
         */
        @JsonProperty("id")
        Long id,

        /**
         * True, if this user is a bot.
         */
        @JsonProperty("is_bot")
        Boolean isBot,

        /**
         * User's or bot's first name.
         */
        @JsonProperty("first_name")
        String firstName,

        /**
         * Optional. User's or bot's last name.
         */
        @JsonProperty("last_name")
        String lastName,

        /**
         * Optional. User's or bot's username.
         */
        @JsonProperty("username")
        String username,

        /**
         * Optional. IETF language tag of the user's language.
         */
        @JsonProperty("language_code")
        String languageCode,

        /**
         * Optional. True, if this user is a Telegram Premium user.
         */
        @JsonProperty("is_premium")
        Boolean isPremium,

        /**
         * Optional. True, if this user added the bot to the attachment menu.
         */
        @JsonProperty("added_to_attachment_menu")
        Boolean addedToAttachmentMenu,

        /**
         * Optional. True, if the bot can be invited to groups.
         * Returned only in getMe.
         */
        @JsonProperty("can_join_groups")
        Boolean canJoinGroups,

        /**
         * Optional. True, if privacy mode is disabled for the bot.
         * Returned only in getMe.
         */
        @JsonProperty("can_read_all_group_messages")
        Boolean canReadAllGroupMessages,

        /**
         * Optional. True, if the bot supports inline queries.
         * Returned only in getMe.
         */
        @JsonProperty("supports_inline_queries")
        Boolean supportsInlineQueries,

        /**
         * Optional. True, if the bot can be connected to a Telegram Business account to receive its messages.
         * Returned only in getMe.
         */
        @JsonProperty("can_connect_to_business")
        Boolean canConnectToBusiness,

        /**
         * Optional. True, if the bot has a main Web App.
         * Returned only in getMe.
         */
        @JsonProperty("has_main_web_app")
        Boolean hasMainWebApp
) { }
