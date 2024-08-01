package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.PasswordChangeRequestDTO;
import com.myfund.models.PasswordChangeRequest;

public class PasswordChangeRequestMapper {

    public static PasswordChangeRequestDTO toPasswordChangeRequestDTO(PasswordChangeRequest passwordChangeRequest) {
        PasswordChangeRequestDTO passwordChangeRequestDTO = new PasswordChangeRequestDTO();
        passwordChangeRequestDTO.setEmail(passwordChangeRequest.getEmail());
        return passwordChangeRequestDTO;

    }

    public static PasswordChangeRequest toPasswordChangeRequest(PasswordChangeRequestDTO passwordChangeRequestDTO) {
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setEmail(passwordChangeRequestDTO.getEmail());
        return passwordChangeRequest;
    }
}
