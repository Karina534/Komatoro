//package org.example.komatoro.aspect;
//
//import com.fasterxml.jackson.databind.ser.Serializers;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.Pointcut;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.example.komatoro.annotation.ValidateUserRights;
//import org.example.komatoro.exeption.UserNotFoundException;
//import org.example.komatoro.model.BaseEntity;
//import org.example.komatoro.repository.ITaskRepository;
//import org.example.komatoro.repository.ITomatoSessionRepository;
//import org.example.komatoro.repository.IUserRepository;
//import org.example.komatoro.service.ITomatoSessionService;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import java.util.UUID;
//
///**
// * Аспект для валидации существования пользователя перед выполнением методов сервиса.
// * Перехватывает методы, аннотированные @ValidateUserRightsExist, проверяет существование пользователя с указанным ID и
// * прерывает выполнение с ошибкой UserNotFoundException, если пользователь не найден.
// */
//@Aspect
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class UserRightsValidationAspect {
//
//    private final IUserRepository userRepository;
//    private final ITaskRepository taskRepository;
//    private final ITomatoSessionRepository tomatoSessionRepository;
//
//    @Pointcut("@annotation(validateUserRights)")
//    public void needUserRightsValidationPointcut(ValidateUserRights validateUserRights) {}
//
//    @Before("needUserRightsValidationPointcut(validateUserRights)")
//    public Object needUserRightsValidation(ProceedingJoinPoint pjp, ValidateUserRights validateUserRights) throws Throwable {
//        // Получаем параметры из аннотации
//        Class<? extends UserDetails> userDetails = validateUserRights.userDetails();
//        Class<? extends BaseEntity> resourceClass = validateUserRights.resourceClass();
//
//        // Получаем текущего пользователя
//        UserDetails currentUser = extractCurrentUser(pjp, userDetails);
//
//        // Извлекаем значение id ресурса
//        Long resourceId = extractResourceId(pjp, resourceClass);
//
//        // Проверка
//        if (!userRepository.existsById(userId)){
//            throw new UserNotFoundException(userId);
//        }
//
//        return pjp.proceed();
//    }
//
//    private Long extractResourceId(ProceedingJoinPoint pjp, Class<? extends BaseEntity> resourceClass) {
//        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
//        String[] paramNames = methodSignature.getParameterNames();
//        Object[] args = pjp.getArgs();
//
//        for (int i = 0; i < paramNames.length; i++){
//            if (paramNames[i].toLowerCase().contains("id") && args[i] instanceof Long){
//                return (Long) args[i];
//            }
//        }
//        throw new IllegalArgumentException("Resource ID parameter not found in method arguments");
//    }
//
//    private UserDetails extractCurrentUser(ProceedingJoinPoint pjp, Class<? extends UserDetails> userDetails) {
//        for (Object arg : pjp.getArgs()){
//            if (userDetails.isInstance(arg)){
//                return (UserDetails) arg;
//            }
//        }
//        throw new IllegalArgumentException("UserDetails parameter not found in method arguments");
//    }
//
//    private Long extractParam(ProceedingJoinPoint pjp, String paramName){
//        MethodSignature signature = (MethodSignature) pjp.getSignature();
//        String[] paramNames = signature.getParameterNames();
//        Object[] args = pjp.getArgs();
//
//        for (int i = 0; i < paramNames.length; i++){
//            if (paramNames[i].equals(paramName) && args[i] instanceof UUID){
//                return (Long) args[i];
//            }
//        }
//
//        throw new IllegalArgumentException(
//                "Parameter " + paramName + " not found or not UUID"
//        );
//    }
//}
