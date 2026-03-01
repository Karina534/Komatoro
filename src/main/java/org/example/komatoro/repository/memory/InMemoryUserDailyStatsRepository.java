//package org.example.komatoro.repository.memory;
//
//import org.example.komatoro.dto.TemporaryEntityDTO.UserDailyStatsDTO;
//import org.example.komatoro.repository.IUserDailyStatsRepository;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
///**
// * In memory хранилище сущностей статистики пользователей
// */
//@Repository
//public class InMemoryUserDailyStatsRepository implements IUserDailyStatsRepository {
//    private final InMemoryStore store;
//
//    public InMemoryUserDailyStatsRepository(InMemoryStore store) {
//        this.store = store;
//    }
//
//    @Override
//    public List<UserDailyStatsDTO> findDailyStatsByUserId(UUID userId) {
//
//        return store.dailyStats.values().stream()
//                .filter(dailyStatsDTO -> dailyStatsDTO.userId().equals(userId)).toList();
//    }
//
//    @Override
//    public Optional<UserDailyStatsDTO> findDailyStatsByUserIdAndDate(UUID userId, LocalDate date) {
//        return store.dailyStats.values().stream()
//                .filter(dailyStatsDTO -> dailyStatsDTO.userId().equals(userId) && dailyStatsDTO.date().equals(date))
//                .findFirst();
//    }
//
//    @Override
//    public List<UserDailyStatsDTO> findDailyStatsByUserIdBetweenDates(UUID userId, LocalDate startDate, LocalDate endDate) {
//        return store.dailyStats.values().stream()
//                .filter(dailyStatsDTO -> dailyStatsDTO.userId().equals(userId) &&
//                        dailyStatsDTO.date().isAfter(startDate) &&
//                        dailyStatsDTO.date().isBefore(endDate)).toList();
//    }
//
//    @Override
//    public UserDailyStatsDTO save(UserDailyStatsDTO dailyStats) {
//        if (dailyStats.userId() == null){
//            throw new RuntimeException("User Id is required");
//        }
//
//        if (dailyStats.pomodoroCount() == null || dailyStats.pomodoroCount() < 0){
//            throw new IllegalArgumentException("Pomodoro count must be at least 0");
//        }
//
//        if (dailyStats.focusMinutes() == null || dailyStats.focusMinutes() < 0){
//            throw new IllegalArgumentException("Focus minutes must be at least 0");
//        }
//
//        UUID dailyStatsId = dailyStats.id() == null ? UUID.randomUUID() : dailyStats.id();
//        LocalDate date = LocalDate.now();
//
//        UserDailyStatsDTO toStore = new UserDailyStatsDTO(
//                dailyStatsId,
//                dailyStats.userId(),
//                date,
//                dailyStats.pomodoroCount(),
//                dailyStats.focusMinutes()
//        );
//
//        store.dailyStats.put(dailyStatsId, toStore);
//        return toStore;
//    }
//
//    @Override
//    public UserDailyStatsDTO update(UserDailyStatsDTO dailyStats) {
//        if (dailyStats.id() == null){
//            throw new IllegalArgumentException("Daily stats id is required for update");
//        }
//
//        UserDailyStatsDTO dailyStatsDTO = store.dailyStats.get(dailyStats.id());
//
//        UserDailyStatsDTO toStore = new UserDailyStatsDTO(
//                dailyStatsDTO.id(),
//                dailyStatsDTO.userId(),
//                dailyStatsDTO.date(),
//                dailyStats.pomodoroCount() == null ? dailyStatsDTO.pomodoroCount() : dailyStats.pomodoroCount(),
//                dailyStats.focusMinutes() == null ? dailyStatsDTO.focusMinutes() : dailyStats.focusMinutes()
//        );
//
//        store.dailyStats.put(dailyStatsDTO.id(), toStore);
//        return toStore;
//    }
//
//    @Override
//    public void deleteById(UUID id) {
//        UserDailyStatsDTO dailyStatsDTO = store.dailyStats.get(id);
//
//        if (dailyStatsDTO == null){
//            throw new IllegalArgumentException(String.format("No such daily stats by id %s", id));
//        }
//
//        store.dailyStats.remove(id);
//    }
//}
