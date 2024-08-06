package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.PasswordChangeRequestDTO;
import com.myfund.models.PasswordChangeRequest;

public class PasswordChangeRequestMapper {

    public static PasswordChangeRequest toModel(PasswordChangeRequestDTO passwordChangeRequestDTO) {
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setEmail(passwordChangeRequestDTO.getEmail());
        return passwordChangeRequest;
    }
}
