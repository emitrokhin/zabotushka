package ru.mitrohinayulya.zabotushka.dto.max;

/// Отписывает бота от получения обновлений через Webhook. После вызова этого метода бот перестаёт
/// получать уведомления о новых событиях, и становится доступна доставка уведомлений через API с длительным опросом
public record MaxDeleteSubscriptionResponse(
        boolean success,
        String message
) {
}
