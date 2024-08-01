package com.myfund.models.DTOs.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.myfund.models.DTOs.CreateUserDTO;
import com.myfund.models.DTOs.UserDTO;
import com.myfund.models.User;
import org.junit.jupiter.api.Test;

public class UserMapperTest {

    @Test
    void testCreateUserDTOMapToUser() {
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setEmail("test@example.com");
        createUserDTO.setPassword("password123");
        createUserDTO.setUsername("testuser");

        User user = UserMapper.createUserDTOMapToUser(createUserDTO);

        assertNotNull(user, "The user should not be null");
        assertEquals(createUserDTO.getEmail(), user.getEmail(), "The email should match");
        assertEquals(createUserDTO.getPassword(), user.getPassword(), "The password should match");
        assertEquals(createUserDTO.getUsername(), user.getUsername(), "The username should match");
    }

    @Test
    void testUserMapToUserDTO() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setUsername("user123");

        UserDTO userDTO = UserMapper.userMapToUserDTO(user);

        assertNotNull(userDTO, "The userDTO should not be null");
        assertEquals(user.getEmail(), userDTO.getEmail(), "The email should match");
        assertEquals(user.getUsername(), userDTO.getUsername(), "The username should match");
    }

    @Test
    void testCreateUserDTOMapToUser_NullValues() {
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setEmail(null);
        createUserDTO.setPassword(null);
        createUserDTO.setUsername(null);

        User user = UserMapper.createUserDTOMapToUser(createUserDTO);

        assertNotNull(user, "The user should not be null");
        assertEquals(createUserDTO.getEmail(), user.getEmail(), "The email should match");
        assertEquals(createUserDTO.getPassword(), user.getPassword(), "The password should match");
        assertEquals(createUserDTO.getUsername(), user.getUsername(), "The username should match");
    }
}