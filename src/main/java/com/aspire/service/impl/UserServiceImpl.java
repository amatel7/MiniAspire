package com.aspire.service.impl;

import com.aspire.entities.User;
import com.aspire.repo.UserRepository;
import com.aspire.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User getUserByToken(String token) {
        return userRepository.findByToken(token).orElse(null);
    }
}
