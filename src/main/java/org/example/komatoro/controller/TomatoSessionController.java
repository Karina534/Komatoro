package org.example.komatoro.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.example.komatoro.dto.request.tomatoSession.*;
import org.example.komatoro.dto.response.tomatoSession.TomatoSessionDTOResponse;
import org.example.komatoro.dto.response.tomatoSession.TomatoSessionRecommendationDTOResponse;
import org.example.komatoro.service.ITomatoSessionService;
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
@RequestMapping("/api/v1/sessions")
public class TomatoSessionController {
    private final ITomatoSessionService service;

    public TomatoSessionController(ITomatoSessionService service) {
        this.service = service;
    }

    @Operation(
            summary = "Start a new tomato session for a user",
            description = "Starts a new tomato session for user. If user has an active session, interrupts it and " +
                    "starts a new one"
    )
    @PostMapping("/start")
    public ResponseEntity<?> startTomatoSession(
            @Valid @RequestBody StartTomatoSessionDTORequest startTomatoSessionDTORequest,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        TomatoSessionDTOResponse createdSession = service.startTomatoSession(userDetails, startTomatoSessionDTORequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdSession);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUserTomatoSessions(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        List<TomatoSessionDTOResponse> allSessions = service.getAllUserSessions(userDetails);
        return ResponseEntity.ok(allSessions);
    }

    @Operation(summary = "Get current active tomato session for a user otherwise empty response")
    @GetMapping("/active")
    public ResponseEntity<?> getCurrentActiveSessionByUser(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        Optional<TomatoSessionDTOResponse> currentRunningSession = service.getCurrentRunningSession(userDetails);

        if (currentRunningSession.isPresent()){
            return ResponseEntity.ok(currentRunningSession.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getSessionById(
            @PathVariable("sessionId") @NotNull Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        TomatoSessionDTOResponse response = service.getSession(userDetails, sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/pause")
    public ResponseEntity<?> pauseSession(
            @PathVariable("sessionId") @NotNull Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        TomatoSessionDTOResponse response = service.pauseTomatoSession(userDetails, sessionId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/resume")
    public ResponseEntity<?> resumeSession(
            @PathVariable("sessionId") @NotNull Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        TomatoSessionDTOResponse response = service.resumeTomatoSession(userDetails, sessionId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Extend a duration of tomato session by 1 minute")
    @PatchMapping("/{sessionId}/extend")
    public ResponseEntity<?> extendTomatoSession(
            @PathVariable("sessionId") @NotNull Long sessionId,
            @RequestBody @Valid ExtendTomatoSessionDTORequest extendTomatoSessionDTO,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        TomatoSessionDTOResponse sessionDTO = service.extendTomatoSession(userDetails, sessionId,
                extendTomatoSessionDTO);
        return ResponseEntity.ok(sessionDTO);
    }

    @PostMapping("/{sessionId}/finish")
    public ResponseEntity<?> finishSession(
            @PathVariable("sessionId") @NotNull Long sessionId,
            @RequestBody @Valid FinishTomatoSessionDTORequest finishTomato,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        service.finishTomatoSession(userDetails, sessionId, finishTomato);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> deleteSession(
            @PathVariable("sessionId") @NotNull Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        service.deleteTomatoSession(userDetails, sessionId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recommendation")
    public ResponseEntity<?> getSessionTypeRecommendation(
            @AuthenticationPrincipal UserDetails userDetails
            ){
        TomatoSessionRecommendationDTOResponse recommendation = service.recommendTomatoSessionType(userDetails);
        return ResponseEntity.ok(recommendation);
    }
}
