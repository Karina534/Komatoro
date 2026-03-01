//package org.example.komatoro.tests;
//
//import org.example.komatoro.dto.request.tomatoSession.StartTomatoSessionDTORequest;
//import org.example.komatoro.dto.TemporaryEntityDTO.TomatoSessionDTO;
//import org.example.komatoro.model.TomatoType;
//import org.example.komatoro.repository.ITomatoSessionRepository;
//import org.example.komatoro.repository.memory.InMemoryStore;
//import org.example.komatoro.repository.memory.InMemoryTomatoSessionRepository;
//import org.example.komatoro.service.TomatoSessionService;
//
//import java.util.Optional;
//import java.util.UUID;
//
//public class TomatoSessionTest {
//    public static void main(String[] args) {
//        InMemoryStore store = new InMemoryStore();
//        ITomatoSessionRepository repository = new InMemoryTomatoSessionRepository(store);
//        TomatoSessionService service = new TomatoSessionService(repository);
//
//        StartTomatoSessionDTORequest startSession = new StartTomatoSessionDTORequest(
//                null,
//                TomatoType.TIMER,
//                25
//        );
//
//        service.startSession(UUID.fromString("ec919175-8ace-48aa-9cd1-a63376a7edcf"), startSession);
//
//        System.out.println("----------------");
//
//        Optional<TomatoSessionDTO> sessionDTO = service.getCurrentRunningSession(UUID.fromString("ec919175-8ace-48aa-9cd1-a63376a7edcf"));
//
//        service.extendSession(sessionDTO.get().id(), sessionDTO.get().userId(), 1);
//    }
//}
