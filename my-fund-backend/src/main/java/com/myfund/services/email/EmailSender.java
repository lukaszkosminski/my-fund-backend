package com.myfund.services.email;

import com.myfund.models.DTOs.UserDTO;
import com.myfund.models.User;

import java.io.IOException;

public interface EmailSender {
    void sendWelcomeEmail(User user) throws IOException;

    void sendPasswordResetEmail(User user, String resetToken) throws IOException;

}
