package org.example.komatoro.dto.response.tomatoSession;

import org.example.komatoro.model.TomatoType;

/**
 * DTO для ответа по рекомендуемому типу сессии
 * @param userId
 * @param type
 */
public record TomatoSessionRecommendationDTOResponse (
        Long userId,
        TomatoType type
){
    public TomatoSessionRecommendationDTOResponse(Long userId, TomatoType type) {
        this.userId = userId;
        this.type = type;
    }
}
