//package org.example.komatoro.repository.memory;
//
//import org.example.komatoro.dto.TemporaryEntityDTO.TomatoSessionDTO;
//import org.example.komatoro.model.TomatoStatus;
//import org.example.komatoro.repository.ITomatoSessionRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Repository
//public class InMemoryTomatoSessionRepository implements ITomatoSessionRepository {
//    private final InMemoryStore store;
//
//    public InMemoryTomatoSessionRepository(InMemoryStore store) {
//        this.store = store;
//    }
//
//    @Override
//    public Optional<TomatoSessionDTO> findById(UUID id) {
//        return Optional.ofNullable(store.tomatoSessions.get(id));
//    }
//
//    @Override
//    public List<TomatoSessionDTO> findByUserId(UUID id) {
//        return store.tomatoSessions.values().stream()
//                .filter(tomatoSessionDTO -> tomatoSessionDTO.userId() != null && tomatoSessionDTO.userId().equals(id))
//                .toList();
//    }
//
//    @Override
//    public List<TomatoSessionDTO> findByTaskId(UUID id) {
//        return store.tomatoSessions.values().stream()
//                .filter(tomatoSessionDTO -> tomatoSessionDTO.taskId() != null && tomatoSessionDTO.taskId().equals(id))
//                .toList();
//    }
//
//    @Override
//    public TomatoSessionDTO save(TomatoSessionDTO tomatoSessionDTO) {
//        UUID id = tomatoSessionDTO.id() == null ? UUID.randomUUID() : tomatoSessionDTO.id();
//
//        TomatoSessionDTO toStore = new TomatoSessionDTO(
//                id,
//                tomatoSessionDTO.userId(),
//                tomatoSessionDTO.taskId(),
//                tomatoSessionDTO.type(),
//                tomatoSessionDTO.createdAt(),
//                tomatoSessionDTO.startTime(),
//                tomatoSessionDTO.endTime(),
//                tomatoSessionDTO.intendedMinutes(),
//                tomatoSessionDTO.status(),
//                tomatoSessionDTO.totalActiveMinutes(),
//                tomatoSessionDTO.lastResumeTime()
//        );
//
//        store.tomatoSessions.put(id, toStore);
//        return toStore;
//    }
//
//    @Override
//    public TomatoSessionDTO update(TomatoSessionDTO tomatoSessionDTO) {
//        if (tomatoSessionDTO.id() == null){
//            throw new IllegalArgumentException("tomato session id is required for update");
//        }
//
//        store.tomatoSessions.put(tomatoSessionDTO.id(), tomatoSessionDTO);
//        return tomatoSessionDTO;
//    }
//
//    @Override
//    public List<TomatoSessionDTO> findAll() {
//        return store.tomatoSessions.values().stream().toList();
//    }
//
//    @Override
//    public Optional<TomatoSessionDTO> findRunningSessionByUserId(UUID userId) {
//        return store.tomatoSessions.values().stream().filter(tomatoSessionDTO ->
//                tomatoSessionDTO.userId().equals(userId) &&
//                        tomatoSessionDTO.status() == TomatoStatus.RUNNING).findFirst();
//    }
//
//    @Override
//    public void delete(UUID sessionId) {
//        store.tomatoSessions.remove(sessionId);
//    }
//}
