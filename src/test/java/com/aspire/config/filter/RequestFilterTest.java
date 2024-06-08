package com.aspire.config.filter;

import com.aspire.entities.User;
import com.aspire.enums.UserRoles;
import com.aspire.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class RequestFilterTest {

    @InjectMocks
    private RequestFilter requestFilter;

    @Mock
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFilter_Unauthorized_NoToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/loans");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        requestFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("User not found", response.getErrorMessage());
    }

    @Test
    public void testFilter_Unauthorized_InvalidToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/loans");
        request.addHeader("x-user-token", "invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(userService.getUserByToken("invalid-token")).thenReturn(null);

        requestFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("User not found", response.getErrorMessage());
    }

    @Test
    public void testFilter_Forbidden_UserAccessAdmin() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin");
        request.addHeader("x-user-token", "valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        User user = new User();
        user.setRole(UserRoles.USER);

        when(userService.getUserByToken("valid-token")).thenReturn(user);

        requestFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertEquals("User not allowed to access this resource", response.getErrorMessage());
    }

    @Test
    public void testFilter_Forbidden_AdminAccessUser() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/loans");
        request.addHeader("x-user-token", "valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        User user = new User();
        user.setRole(UserRoles.ADMIN);

        when(userService.getUserByToken("valid-token")).thenReturn(user);

        requestFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertEquals("User not allowed to access this resource", response.getErrorMessage());
    }

    @Test
    public void testFilter_Success_UserAccessLoan() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/loans");
        request.addHeader("x-user-token", "valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        User user = new User();
        user.setRole(UserRoles.USER);

        when(userService.getUserByToken("valid-token")).thenReturn(user);

        requestFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    public void testFilter_Success_AdminAccessAdmin() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin");
        request.addHeader("x-user-token", "valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        User user = new User();
        user.setRole(UserRoles.ADMIN);

        when(userService.getUserByToken("valid-token")).thenReturn(user);

        requestFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
}
