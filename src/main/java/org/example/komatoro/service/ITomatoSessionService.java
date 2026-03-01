package org.example.komatoro.service;

import org.example.komatoro.dto.request.tomatoSession.*;
import org.example.komatoro.dto.TemporaryEntityDTO.TomatoSessionDTO;
import org.example.komatoro.dto.response.tomatoSession.TomatoSessionDTOResponse;
import org.example.komatoro.dto.response.tomatoSession.TomatoSessionRecommendationDTOResponse;
import org.example.komatoro.model.TomatoSession;
import org.example.komatoro.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ITomatoSessionService {
    TomatoSessionDTOResponse startTomatoSession(UserDetails userDetails, StartTomatoSessionDTORequest sessionDTO);
    TomatoSessionDTOResponse pauseTomatoSession(UserDetails userDetails, Long sessionId);
    TomatoSessionDTOResponse resumeTomatoSession(UserDetails userDetails, Long sessionId);
    TomatoSessionDTOResponse extendTomatoSession(UserDetails userDetails, Long sessionId, ExtendTomatoSessionDTORequest tomatoSession);
    TomatoSessionDTOResponse finishTomatoSession(UserDetails userDetails, FinishTomatoSessionDTORequest tomatoSession);
    Optional<TomatoSessionDTOResponse> getCurrentRunningSession(UserDetails userDetails);
    TomatoSessionDTOResponse getSession(UserDetails userDetails, Long sessionId);
    void deleteTomatoSession(UserDetails userDetails, Long sessionId);
    boolean isAnyRunningTomatoSession(Long userId);
    List<TomatoSessionDTOResponse> getAllUserSessions(UserDetails userDetails);
    TomatoSessionRecommendationDTOResponse recommendTomatoSession(UserDetails userDetails);
}
