package org.example.komatoro.dto.response.tomatoSession;

import org.example.komatoro.model.TomatoType;

/**
 * DTO для ответа по рекомендуемому типу сессии
 * @param type
 */
public record TomatoSessionRecommendationDTOResponse (
        TomatoType type
){
    public TomatoSessionRecommendationDTOResponse(TomatoType type) {
        this.type = type;
    }
}
