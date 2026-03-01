//package org.example.komatoro.repository.memory;
//
//import org.example.komatoro.dto.TemporaryEntityDTO.TaskDTO;
//import org.example.komatoro.repository.ITaskRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Repository;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Repository
//public class InMemoryTaskRepository implements ITaskRepository {
//
//    private final InMemoryStore store;
//
//    @Autowired
//    public InMemoryTaskRepository(InMemoryStore store) {
//        this.store = store;
//    }
//
//    @Override
//    public Optional<TaskDTO> findById(UUID id) {
//        return Optional.ofNullable(store.tasks.get(id));
//    }
//
//    @Override
//    public Optional<TaskDTO> findByTitle(String title) {
//        return store.tasks.values().stream()
//                .filter(taskDTO -> taskDTO.title() != null && taskDTO.title().equals(title))
//                .findFirst();
//    }
//
//    @Override
//    public List<TaskDTO> findByUserId(UUID id) {
//        return store.tasks.values().stream()
//                .filter(taskDTO -> taskDTO.userId() != null && taskDTO.userId().equals(id))
//                .toList();
//    }
//
//    @Override
//    public TaskDTO save(TaskDTO taskDTO) {
//        UUID id = taskDTO.id() == null ? UUID.randomUUID() : taskDTO.id();
//        boolean isActive = true;
//        Instant createdAt = Instant.now();
//
//        TaskDTO toStore = new TaskDTO(
//                id,
//                taskDTO.userId(),
//                taskDTO.title(),
//                taskDTO.description(),
//                isActive,
//                createdAt
//        );
//
//        store.tasks.put(id, toStore);
//        return toStore;
//    }
//
//    @Override
//    public TaskDTO update(TaskDTO taskDTO) {
//        if (taskDTO.id() == null){
//            throw new IllegalArgumentException("task id is required for update");
//        }
//
//        TaskDTO task = store.tasks.get(taskDTO.id());
//
//        TaskDTO toStore = new TaskDTO(
//                task.id(),
//                taskDTO.userId(),
//                taskDTO.title() == null ? task.title() : taskDTO.title(),
//                taskDTO.description() == null ? task.description() : taskDTO.description(),
//                taskDTO.isActive() == null ? task.isActive() : taskDTO.isActive(),
//                task.createdAt()
//        );
//
//        store.tasks.put(task.id(), toStore);
//        return toStore;
//    }
//
//    @Override
//    public void deleteById(UUID id) {
//        if (!store.tasks.containsKey(id)){
//            throw new IllegalArgumentException("Task with id " + id + " does not exist");
//        }
//        store.tasks.remove(id);
//    }
//
//    @Override
//    public List<TaskDTO> findAll() {
//        return store.tasks.values().stream().toList();
//    }
//}
