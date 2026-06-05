package com.example.domus.domain.usercase;

import com.example.domus.domain.model.ButtonVisibility;
import com.example.domus.domain.model.UserType;

public class GetButtonVisibilityUseCase {

    public ButtonVisibility execute(UserType userType) {
        if (userType == UserType.MORADOR) {
            return ButtonVisibility.forMorador();
        } else {
            return ButtonVisibility.forAdmin();
        }
    }
}