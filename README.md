# Komatoro

Komatoro - backend-сервис для управления задачами и фокус-сессиями по методу Pomodoro.

Проект поддерживает два сценария работы:
- Pomodoro с привязкой к задаче;
- Pomodoro без задачи, в режиме таймера.

Это REST API с JWT-авторизацией, бизнес-логикой для управления сессиями и хранением данных в PostgreSQL.

## Что реализовано

- Регистрация пользователя и обновление настроек Pomodoro
- Управление задачами: создание, получение, обновление, удаление, активация и завершение
- Управление сессиями Pomodoro: старт, пауза, продолжение, продление, завершение
- Получение активной сессии и истории сессий
- Рекомендация следующего типа сессии: фокус, короткий перерыв или длинный перерыв
- Подсчет дневной статистики: количество помодоро и минуты фокуса
- Валидация входных данных, проверки прав доступа
- Глобальный обработчик исключений и собственные бизнес-исключения

## Авторизация и безопасность

- JWT-авторизация с Access token и Refresh token
- Refresh token хранится в Cookie
- Эндпоинты, работающие по Access token, реализованы как Stateless
- Для остальных запросов настроена CSRF-защита

## Реализованные технические задачи

- REST API на Spring Boot
- Слоистая структура: controller -> service -> repository -> model
- PostgreSQL + Spring Data JPA
- Миграции схемы БД через Flyway
- Разделение Entity и DTO, маппинг через MapStruct
- Сквозная логика через AOP: логирование, аудит, метрики производительности
- Документация API через Swagger/OpenAPI
- Unit и integration тесты на JUnit 5, Spring Boot Test, MockMvc, Spring Security Test и Testcontainers

## Технологии

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA (Hibernate)
- Spring Security (JWT)
- PostgreSQL
- Flyway
- MapStruct
- Lombok
- SpringDoc OpenAPI (Swagger UI)
- JUnit 5, Spring Boot Test, MockMvc, Spring Security Test, Testcontainers
- Maven
