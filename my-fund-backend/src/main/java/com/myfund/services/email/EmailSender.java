package com.myfund.services.email;

import com.myfund.models.DTOs.UserDTO;

import java.io.IOException;

public interface EmailSender {
    void sendWelcomeEmail(UserDTO userDTO) throws IOException;
}
