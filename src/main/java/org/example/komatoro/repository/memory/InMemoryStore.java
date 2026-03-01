//package org.example.komatoro.repository.memory;
//
//import io.swagger.v3.oas.annotations.media.Schema;
//import org.example.komatoro.dto.TemporaryEntityDTO.*;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Repository;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * Хранилище данных, пока нет базы данных
// */
//@Component
//@Scope("singleton")
//public class InMemoryStore {
//    public Map<UUID, UserDTO> users = new HashMap<>();
//    public Map<UUID, TaskDTO> tasks = new HashMap<>();
//    public Map<UUID, TomatoSessionDTO> tomatoSessions = new HashMap<>();
//    public Map<UUID, UserDailyStatsDTO> dailyStats = new HashMap<>();
//    public Map<UUID, UserSettingsDTO> userSettings = new HashMap<>();
//}
