package com.myfund.service;

import com.myfund.model.DTO.CreateUserDTO;
import com.myfund.model.DTO.UserDTO;
import com.myfund.model.DTO.mapper.UserMapper;
import com.myfund.model.User;
import com.myfund.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final BudgetService budgetService;

    @Autowired
    public UserService(UserRepository userRepository, BudgetService budgetService) {
        this.userRepository = userRepository;
        this.budgetService = budgetService;
    }

    public UserDTO createUser(CreateUserDTO createUserDTO) {
        User user = UserMapper.createUserDTOMapToUser(createUserDTO);
        user.setRole("USER");
        log.info("User saved successfully. Email: {}, Pass: {}, ", user.getEmail(), user.getPassword());
        userRepository.save(user);
        budgetService.createDefaultBudget(user);
        return UserMapper.userMapToUserDTO(user);
    }
}
