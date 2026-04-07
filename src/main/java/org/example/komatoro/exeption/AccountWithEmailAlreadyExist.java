package org.example.komatoro.exeption;

public class AccountWithEmailAlreadyExist extends BusinessException{
    public AccountWithEmailAlreadyExist(){
        super(
                "EMAIL_ALREADY_EXIST",
                "User with this email already exist"
        );
    }
}
