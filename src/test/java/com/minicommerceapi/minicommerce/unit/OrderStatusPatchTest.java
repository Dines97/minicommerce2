package com.minicommerceapi.minicommerce.unit;

import com.minicommerceapi.minicommerce.domain.Order;
import com.minicommerceapi.minicommerce.domain.OrderStatus;
import com.minicommerceapi.minicommerce.dto.OrderDtos;
import com.minicommerceapi.minicommerce.exception.ConflictException;
import com.minicommerceapi.minicommerce.repo.OrderRepository;
import com.minicommerceapi.minicommerce.repo.ProductRepository;
import com.minicommerceapi.minicommerce.repo.UserRepository;
import com.minicommerceapi.minicommerce.service.OrderService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderStatusPatchTest {

    @Test
    void patchStatus_terminalOrder_throwsConflict() {
        OrderRepository or = mock(OrderRepository.class);
        UserRepository ur = mock(UserRepository.class);
        ProductRepository pr = mock(ProductRepository.class);

        Order o = new Order();
        o.setStatus(OrderStatus.PAID);
        when(or.findById(1L)).thenReturn(Optional.of(o));

        OrderService svc = new OrderService(or, ur, pr);
        assertThrows(ConflictException.class, () -> svc.patchStatus(1L, new OrderDtos.PatchOrderRequest("CANCELLED")));
    }
}
