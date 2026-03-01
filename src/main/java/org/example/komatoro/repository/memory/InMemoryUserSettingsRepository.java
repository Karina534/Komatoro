//package org.example.komatoro.repository.memory;
//
//import org.example.komatoro.dto.TemporaryEntityDTO.UserSettingsDTO;
//import org.example.komatoro.repository.IUserSettingsRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.Optional;
//import java.util.UUID;
//
///**
// * In memory логика репозитория для настроек пользователя
// */
//@Repository
//public class InMemoryUserSettingsRepository implements IUserSettingsRepository {
//    private final InMemoryStore store;
//
//    public InMemoryUserSettingsRepository(InMemoryStore store) {
//        this.store = store;
//    }
//
//    @Override
//    public Optional<UserSettingsDTO> findByUserId(UUID userId) {
//        return store.userSettings.values().stream().filter(settings -> userId.equals(settings.getUserId()))
//                .findFirst();
//    }
//
//    @Override
//    public UserSettingsDTO save(UserSettingsDTO settings) {
//        UUID id = UUID.randomUUID();
//        UserSettingsDTO settingsDTO = new UserSettingsDTO(
//                id,
//                settings.getUserId(),
//                settings.getPomodoroMinutes() == null ? 25 : settings.getPomodoroMinutes(),
//                settings.getLongBreakMinutes() == null ? 15 : settings.getLongBreakMinutes(),
//                settings.getShortBreakMinutes() == null ? 5 : settings.getShortBreakMinutes(),
//                settings.getLongBreakInterval() == null ? 4 : settings.getLongBreakInterval()
//        );
//        store.userSettings.put(id, settingsDTO);
//        return settingsDTO;
//    }
//
//    @Override
//    public UserSettingsDTO update(UserSettingsDTO settings) {
//        UserSettingsDTO settingsDTO = store.userSettings.get(settings.getId());
//        if (settingsDTO == null){
//            throw new IllegalArgumentException(String.format("No such settings with id %s", settings.getId()));
//        }
//
//        UserSettingsDTO toStore = new UserSettingsDTO(
//                settings.getId(),
//                settingsDTO.getUserId(),
//                settings.getPomodoroMinutes() == null ? settingsDTO.getPomodoroMinutes() : settings.getPomodoroMinutes(),
//                settings.getLongBreakMinutes() == null ? settingsDTO.getLongBreakMinutes() : settings.getLongBreakMinutes(),
//                settings.getShortBreakMinutes() == null ? settingsDTO.getShortBreakMinutes() : settings.getShortBreakMinutes(),
//                settings.getLongBreakInterval() == null ? settingsDTO.getLongBreakInterval() : settings.getLongBreakInterval()
//        );
//
//        store.userSettings.put(settingsDTO.getId(), toStore);
//        return toStore;
//    }
//
//    @Override
//    public void deleteById(UUID id) {
//        UserSettingsDTO settingsDTO = store.userSettings.get(id);
//        if (settingsDTO == null){
//            throw new IllegalArgumentException(String.format("No such settings with id %s", id));
//        }
//
//        store.userSettings.remove(id);
//    }
//}
