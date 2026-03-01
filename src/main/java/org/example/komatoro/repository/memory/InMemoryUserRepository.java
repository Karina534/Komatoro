//package org.example.komatoro.repository.memory;
//
//import lombok.extern.slf4j.Slf4j;
//import org.example.komatoro.dto.TemporaryEntityDTO.UserDTO;
//import org.example.komatoro.repository.IUserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
///**
// * Реализация in-memory репозитория для пользователей
// */
//@Slf4j
//@Repository
//public class InMemoryUserRepository implements IUserRepository {
//    private final InMemoryStore store;
//
//    @Autowired
//    public InMemoryUserRepository(InMemoryStore store) {
//        this.store = store;
//    }
//
//
//    @Override
//    public Optional<UserDTO> findById(UUID id) {
//        return Optional.ofNullable(store.users.get(id));
//    }
//
//    @Override
//    public Optional<UserDTO> findByUsername(String username) {
//        return store.users.values().stream().filter(userDTO -> userDTO.username().equals(username)).findFirst();
//    }
//
//    @Override
//    public Optional<UserDTO> findByEmail(String email) {
//        return store.users.values().stream().filter(userDTO -> userDTO.email().equals(email)).findFirst();
//    }
//
//    @Override
//    public UserDTO save(UserDTO userDTO) {
//        store.users.put(userDTO.id(), userDTO);
//        return userDTO;
//    }
//
//    @Override
//    public UserDTO update(UserDTO userDTO) {
//        store.users.put(userDTO.id(), userDTO);
//        return userDTO;
//    }
//
//    @Override
//    public List<UserDTO> findAll() {
//        return store.users.values().stream().toList();
//    }
//
//    @Override
//    public boolean existsById(UUID userId) {
//        return store.users.containsKey(userId);
//    }
//}
