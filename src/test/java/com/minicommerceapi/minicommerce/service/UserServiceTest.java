package com.minicommerceapi.minicommerce.service;

import com.minicommerceapi.minicommerce.domain.User;
import com.minicommerceapi.minicommerce.dto.UserDtos;
import com.minicommerceapi.minicommerce.exception.ConflictException;
import com.minicommerceapi.minicommerce.exception.NotFoundException;
import com.minicommerceapi.minicommerce.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        // Assume BaseEntity provides setId
        try {
            var setId = user.getClass().getSuperclass().getDeclaredMethod("setId", Long.class);
            setId.setAccessible(true);
            setId.invoke(user, 1L);
        } catch (Exception ignored) {}
    }

    @Test
    void create_shouldCreateUser_whenEmailNotExists() {
        UserDtos.CreateUserRequest req = new UserDtos.CreateUserRequest("John Doe", "john@example.com");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        var response = userService.create(req);
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.email()).isEqualTo("john@example.com");
    }

    @Test
    void create_shouldThrowConflictException_whenEmailExists() {
        UserDtos.CreateUserRequest req = new UserDtos.CreateUserRequest("John Doe", "john@example.com");
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        assertThatThrownBy(() -> userService.create(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void list_shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        var result = userService.list();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("john@example.com");
    }

    @Test
    void get_shouldReturnUser_whenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        var result = userService.get(1L);
        assertThat(result.email()).isEqualTo("john@example.com");
    }

    @Test
    void get_shouldThrowNotFoundException_whenNotExists() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.get(2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void update_shouldUpdateUser_whenEmailNotExistsOrSame() {
        UserDtos.UpdateUserRequest req = new UserDtos.UpdateUserRequest("Jane Doe", "jane@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        var result = userService.update(1L, req);
        assertThat(result.name()).isEqualTo("Jane Doe");
        assertThat(result.email()).isEqualTo("jane@example.com");
    }

    @Test
    void update_shouldThrowConflictException_whenEmailExists() {
        UserDtos.UpdateUserRequest req = new UserDtos.UpdateUserRequest("Jane Doe", "jane@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);
        user.setEmail("john@example.com");
        assertThatThrownBy(() -> userService.update(1L, req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void update_shouldThrowNotFoundException_whenUserNotFound() {
        UserDtos.UpdateUserRequest req = new UserDtos.UpdateUserRequest("Jane Doe", "jane@example.com");
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.update(2L, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void delete_shouldDeleteUser_whenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);
        userService.delete(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_shouldThrowNotFoundException_whenNotExists() {
        when(userRepository.existsById(2L)).thenReturn(false);
        assertThatThrownBy(() -> userService.delete(2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
