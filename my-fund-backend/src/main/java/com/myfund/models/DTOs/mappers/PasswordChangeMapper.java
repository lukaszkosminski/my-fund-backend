package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.PasswordChangeDTO;
import com.myfund.models.PasswordChange;

public class PasswordChangeMapper {

    public static PasswordChange toModel(PasswordChangeDTO passwordChangeDTO) {
        PasswordChange passwordChange = new PasswordChange();
        passwordChange.setNewPassword(passwordChangeDTO.getNewPassword());
        passwordChange.setEmail(passwordChangeDTO.getEmail());
        passwordChange.setToken(passwordChangeDTO.getToken());
        return passwordChange;
    }

}
