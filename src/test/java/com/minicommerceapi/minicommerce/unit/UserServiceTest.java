package com.minicommerceapi.minicommerce.unit;

import com.minicommerceapi.minicommerce.dto.UserDtos;
import com.minicommerceapi.minicommerce.exception.ConflictException;
import com.minicommerceapi.minicommerce.exception.NotFoundException;
import com.minicommerceapi.minicommerce.repo.UserRepository;
import com.minicommerceapi.minicommerce.service.UserService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Test
    void create_normalizesEmail_andPersists() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        when(repo.existsByEmail("a@b.com")).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> {
            var u = (com.minicommerceapi.minicommerce.domain.User) inv.getArgument(0);
            Field f = com.minicommerceapi.minicommerce.domain.BaseEntity.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(u, 1L);
            return u;
        });

        UserService svc = new UserService(repo);
        var res = svc.create(new UserDtos.CreateUserRequest(" Ali ", " A@B.COM "));

        assertEquals(1L, res.id());
        assertEquals("Ali", res.name());
        assertEquals("a@b.com", res.email());
    }

    @Test
    void create_duplicateEmail_throwsConflict() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.existsByEmail("x@y.com")).thenReturn(true);

        UserService svc = new UserService(repo);
        assertThrows(ConflictException.class,
                () -> svc.create(new UserDtos.CreateUserRequest("X", "x@y.com")));
    }

    @Test
    void get_missingUser_throwsNotFound() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.findById(99L)).thenReturn(Optional.empty());

        UserService svc = new UserService(repo);
        assertThrows(NotFoundException.class, () -> svc.get(99L));
    }
}
