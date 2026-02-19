package ru.mitrohinayulya.zabotushka.dto.max;

import java.util.List;

/// Запрос для получения информации об участнике чата в Max
public record MaxGetChatMemberRequest(
        List<Long> members
) {
    public static MaxGetChatMemberRequest ofMemberList(List<Long> members) {
        return new MaxGetChatMemberRequest(members);
    }
}
