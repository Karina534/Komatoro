package org.example.komatoro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KomatoroApplication {

    public static void main(String[] args) {
        SpringApplication.run(KomatoroApplication.class, args);
    }

}

//TODO: Добавить логирование
//TODO: Добавить пагинацию для получения всех задач
//TODO: Добавить комбинированные dto, если будет нужен пользователь со статистикой и/или настройками
