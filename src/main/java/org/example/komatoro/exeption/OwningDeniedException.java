package org.example.komatoro.exeption;

import org.springframework.http.HttpStatus;

public class OwningDeniedException extends BusinessException{
    public OwningDeniedException(){
        super(
                "FORBIDDEN_RESOURCE",
                "User try to get not his own resource",
                HttpStatus.FORBIDDEN
        );
    }
}
