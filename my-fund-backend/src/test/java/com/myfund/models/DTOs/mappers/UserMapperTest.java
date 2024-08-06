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

        CreateUserDTO createUserDTO = CreateUserDTO
                .builder()
                .email("test@example.com")
                .password("password123")
                .username("testuser")
                .build();

        User user = UserMapper.toModel(createUserDTO);

        assertNotNull(user, "The user should not be null");
        assertEquals(createUserDTO.getEmail(), user.getEmail(), "The email should match");
        assertEquals(createUserDTO.getPassword(), user.getPassword(), "The password should match");
        assertEquals(createUserDTO.getUsername(), user.getUsername(), "The username should match");
    }

    @Test
    void testUserMapToUserDTO() {
        User user =User.builder().email("test@test.pl").username("testUser").build();

        UserDTO userDTO = UserMapper.toDTO(user);

        assertNotNull(userDTO, "The userDTO should not be null");
        assertEquals(user.getEmail(), userDTO.getEmail(), "The email should match");
        assertEquals(user.getUsername(), userDTO.getUsername(), "The username should match");
    }

    @Test
    void testCreateUserDTOMapToUser_NullValues() {
        CreateUserDTO createUserDTO = CreateUserDTO
                .builder()
                .email(null)
                .password(null)
                .username(null)
                .build();

        User user = UserMapper.toModel(createUserDTO);

        assertNotNull(user, "The user should not be null");
        assertEquals(createUserDTO.getEmail(), user.getEmail(), "The email should match");
        assertEquals(createUserDTO.getPassword(), user.getPassword(), "The password should match");
        assertEquals(createUserDTO.getUsername(), user.getUsername(), "The username should match");
    }
}