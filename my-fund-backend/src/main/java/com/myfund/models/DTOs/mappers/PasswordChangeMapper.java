package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.PasswordChangeDTO;
import com.myfund.models.PasswordChange;

public class PasswordChangeMapper {

    public static PasswordChange toPasswordChange(PasswordChangeDTO passwordChangeDTO) {
        PasswordChange passwordChange = new PasswordChange();
        passwordChange.setNewPassword(passwordChangeDTO.getNewPassword());
        passwordChange.setEmail(passwordChangeDTO.getEmail());
        passwordChange.setToken(passwordChangeDTO.getToken());
        return passwordChange;
    }

    public static PasswordChangeDTO toPasswordChangeDTO(PasswordChange passwordChange) {
        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setNewPassword(passwordChange.getNewPassword());
        passwordChangeDTO.setEmail(passwordChange.getEmail());
        passwordChangeDTO.setToken(passwordChange.getToken());
        return passwordChangeDTO;
    }

}
