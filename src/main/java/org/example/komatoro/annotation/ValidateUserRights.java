package org.example.komatoro.annotation;

import org.example.komatoro.model.BaseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для валидации, что ресурс (Task или TomatoSession) принадлежит пользователю перед выполнением
 * методов контроллера. Требует указания класса UserDetails для извлечения ID пользователя и класса ресурса
 * для проверки прав доступа.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ValidateUserRights {
    Class<UserDetails> userDetails();
    Class<? extends BaseEntity> resourceClass();
}
