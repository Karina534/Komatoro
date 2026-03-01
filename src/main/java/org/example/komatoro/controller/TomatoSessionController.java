package org.example.komatoro.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.example.komatoro.dto.request.tomatoSession.*;
import org.example.komatoro.dto.response.tomatoSession.TomatoSessionDTOResponse;
import org.example.komatoro.dto.response.tomatoSession.TomatoSessionRecommendationDTOResponse;
import org.example.komatoro.model.UserSettings;
import org.example.komatoro.security.CustomUserDetails;
import org.example.komatoro.service.ITomatoSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/sessions")
public class TomatoSessionController {
    private final ITomatoSessionService service;

    @Autowired
    public TomatoSessionController(ITomatoSessionService service) {
        this.service = service;
    }

    @Operation(
            summary = "Start a new tomato session for a user",
            description = "Starts a new tomato session for user. If user has an active session, interrupts it and " +
                    "starts a new one"
    )
    @PostMapping("/start")
    public ResponseEntity<TomatoSessionDTOResponse> startSession(
            @Valid @RequestBody StartTomatoSessionDTORequest tomatoSessionDTO,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        TomatoSessionDTOResponse createdSession = service.startTomatoSession(userDetails, tomatoSessionDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdSession);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TomatoSessionDTOResponse>> getAllUserSessions(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        List<TomatoSessionDTOResponse> allSessions = service.getAllUserSessions(userDetails);
        return ResponseEntity.ok(allSessions);
    }

    @Operation(summary = "Get current active tomato session for a user otherwise empty response")
    @GetMapping("/active")
    public ResponseEntity<TomatoSessionDTOResponse> getCurrentActiveSessionByUser(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        Optional<TomatoSessionDTOResponse> sessionDTO = service.getCurrentRunningSession(userDetails);

        if (sessionDTO.isPresent()){
            return ResponseEntity.ok(sessionDTO.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<TomatoSessionDTOResponse> getSession(
            @PathVariable("sessionId") @NotNull Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        TomatoSessionDTOResponse response = service.getSession(userDetails, sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/pause")
    public ResponseEntity<TomatoSessionDTOResponse> pauseSession(
            @PathVariable("sessionId") @NotNull Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        TomatoSessionDTOResponse response = service.pauseTomatoSession(userDetails, sessionId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/resume")
    public ResponseEntity<TomatoSessionDTOResponse> resumeSession(
            @PathVariable("sessionId") @NotNull Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        TomatoSessionDTOResponse response = service.resumeTomatoSession(userDetails, sessionId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Extend a duration of tomato session by 1 minute")
    @PatchMapping("/{sessionId}/extend")
    public ResponseEntity<TomatoSessionDTOResponse> extendTomatoSession(
            @PathVariable("sessionId") @NotNull Long sessionId,
            @RequestBody @Valid ExtendTomatoSessionDTORequest extendTomato,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        TomatoSessionDTOResponse sessionDTO = service.extendTomatoSession(userDetails, sessionId, extendTomato);
        return ResponseEntity.ok(sessionDTO);
    }

    @PostMapping("/{sessionId}/finish")
    public ResponseEntity<Void> finishSession(
            @PathVariable("sessionId") @NotNull Long sessionId,
            @RequestBody @Valid FinishTomatoSessionDTORequest finishTomato,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        service.finishTomatoSession(userDetails, finishTomato);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable("sessionId") @NotNull Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        service.deleteTomatoSession(userDetails, sessionId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recommendation")
    public ResponseEntity<TomatoSessionRecommendationDTOResponse> getSessionRecommendation(
            @AuthenticationPrincipal UserDetails userDetails
            ){
        TomatoSessionRecommendationDTOResponse recommendation = service.recommendTomatoSession(userDetails);
        return ResponseEntity.ok(recommendation);
    }
}
