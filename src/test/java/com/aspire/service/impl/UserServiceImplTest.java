package com.aspire.service.impl;

import com.aspire.entities.User;
import com.aspire.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetUserByToken_UserExists() {
        User user = new User();
        user.setId(1L);
        user.setToken("valid-token");

        when(userRepository.findByToken("valid-token")).thenReturn(Optional.of(user));

        User result = userService.getUserByToken("valid-token");

        assertEquals(user, result);
    }

    @Test
    public void testGetUserByToken_UserDoesNotExist() {
        when(userRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        User result = userService.getUserByToken("invalid-token");

        assertNull(result);
    }

    @Test
    public void testGetUserByToken_NullToken() {
        User result = userService.getUserByToken(null);

        assertNull(result);
    }
}
