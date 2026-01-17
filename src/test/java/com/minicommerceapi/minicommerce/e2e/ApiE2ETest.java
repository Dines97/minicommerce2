package com.minicommerceapi.minicommerce.e2e;

import com.minicommerceapi.minicommerce.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiE2ETest {

    @LocalServerPort
    int port;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class RestTemplateConfig {
        @org.springframework.context.annotation.Bean
        public org.springframework.web.client.RestTemplate restTemplate() {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            restTemplate.setErrorHandler(new org.springframework.web.client.ResponseErrorHandler() {
                @Override
                public boolean hasError(org.springframework.http.client.ClientHttpResponse response) throws java.io.IOException {
                    return false;
                }
                public void handleError(org.springframework.http.client.ClientHttpResponse response) throws java.io.IOException {
                    // No-op
                }
            });
            return restTemplate;
        }
    }

    @Autowired
    RestTemplate rest;

    @Test
    void scenario_userLifecycle_create_get_delete() {
        var created = rest.postForObject(url("/api/users"),
                new UserDtos.CreateUserRequest("Mehmet", "mehmet@ornek.com"),
                UserDtos.UserResponse.class);

        assertNotNull(created);
        assertNotNull(created.id());

        var fetched = rest.getForObject(url("/api/users/" + created.id()), UserDtos.UserResponse.class);
        assertEquals("Mehmet", fetched.name());

        rest.delete(url("/api/users/" + created.id()));

        ResponseEntity<String> afterDelete = rest.getForEntity(url("/api/users/" + created.id()), String.class);
        assertEquals(HttpStatus.NOT_FOUND, afterDelete.getStatusCode());
    }

    @Test
    void scenario_categoryDelete_withProducts_returnsConflict() {
        long catId = rest.postForObject(url("/api/categories"),
                new CategoryDtos.CreateCategoryRequest("Elektronik"),
                CategoryDtos.CategoryResponse.class).id();

        rest.postForObject(url("/api/products"),
                new ProductDtos.CreateProductRequest("Kulaklik", "SKU-TR-99", new BigDecimal("250.00"), 5, catId),
                ProductDtos.ProductResponse.class);

        ResponseEntity<String> del = rest.exchange(url("/api/categories/" + catId), HttpMethod.DELETE, null, String.class);
        assertEquals(HttpStatus.CONFLICT, del.getStatusCode());
    }

    @Test
    void scenario_orderCreation_decreasesStock() {
        var userRequest = new UserDtos.CreateUserRequest("Zeynep", "zeynep@ornek.com");
        ResponseEntity<UserDtos.UserResponse> userEntity = rest.postForEntity(url("/api/users"), userRequest, UserDtos.UserResponse.class);
        if (userEntity.getBody() == null || userEntity.getBody().id() == null) {
            System.err.println("User creation failed. Status: " + userEntity.getStatusCode() + ", Body: " + userEntity.getBody());
        }
        assertNotNull(userEntity.getBody(), "User creation response is null. Status: " + userEntity.getStatusCode());
        assertNotNull(userEntity.getBody().id(), "User creation returned null id. Status: " + userEntity.getStatusCode() + ", Body: " + userEntity.getBody());
        long userId = userEntity.getBody().id();

        var catResponse = rest.postForObject(url("/api/categories"),
                new CategoryDtos.CreateCategoryRequest("Ofis"),
                CategoryDtos.CategoryResponse.class);
        assertNotNull(catResponse, "Category creation response is null");
        assertNotNull(catResponse.id(), "Category creation returned null id. Response: " + catResponse);
        long catId = catResponse.id();

        var productResponse = rest.postForObject(url("/api/products"),
                new ProductDtos.CreateProductRequest("Defter", "SKU-TR-10", new BigDecimal("30.00"), 10, catId),
                ProductDtos.ProductResponse.class);
        assertNotNull(productResponse, "Product creation response is null");
        assertNotNull(productResponse.id(), "Product creation returned null id. Response: " + productResponse);
        long productId = productResponse.id();

        var before = rest.getForObject(url("/api/products/" + productId), ProductDtos.ProductResponse.class);
        assertNotNull(before, "Product fetch before order is null");
        assertEquals(10, before.stock());

        var order = rest.postForObject(url("/api/orders"),
                new OrderDtos.CreateOrderRequest(userId, List.of(new OrderDtos.CreateOrderItem(productId, 2))),
                OrderDtos.OrderResponse.class);
        assertNotNull(order, "Order creation response is null");
        assertEquals(1, order.items().size());

        var after = rest.getForObject(url("/api/products/" + productId), ProductDtos.ProductResponse.class);
        assertNotNull(after, "Product fetch after order is null");
        assertEquals(8, after.stock());
    }

    @Test
    void scenario_reviewPatch_updatesRatingAndComment() {
        long userId = rest.postForObject(url("/api/users"),
                new UserDtos.CreateUserRequest("Ahmet", "ahmet@ornek.com"),
                UserDtos.UserResponse.class).id();

        long catId = rest.postForObject(url("/api/categories"),
                new CategoryDtos.CreateCategoryRequest("Ev"),
                CategoryDtos.CategoryResponse.class).id();

        long productId = rest.postForObject(url("/api/products"),
                new ProductDtos.CreateProductRequest("Masa Lambasi", "SKU-TR-55", new BigDecimal("120.00"), 3, catId),
                ProductDtos.ProductResponse.class).id();

        long reviewId = rest.postForObject(url("/api/reviews"),
                new ReviewDtos.CreateReviewRequest(userId, productId, 3, "idare eder"),
                ReviewDtos.ReviewResponse.class).id();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> patchReq = new HttpEntity<>("{\"rating\":5,\"comment\":\"cok iyi\"}", headers);

        ResponseEntity<ReviewDtos.ReviewResponse> patched = rest.exchange(url("/api/reviews/" + reviewId),
                HttpMethod.PATCH, patchReq, ReviewDtos.ReviewResponse.class);

        assertEquals(HttpStatus.OK, patched.getStatusCode());
        assertNotNull(patched.getBody());
        assertEquals(5, patched.getBody().rating());
        assertEquals("cok iyi", patched.getBody().comment());
    }

    @Test
    void scenario_updateOrderStatus_createdToPaid() {
        long userId = rest.postForObject(url("/api/users"),
                new UserDtos.CreateUserRequest("Elif", "elif@ornek.com"),
                UserDtos.UserResponse.class).id();

        long catId = rest.postForObject(url("/api/categories"),
                new CategoryDtos.CreateCategoryRequest("Kirtasiye"),
                CategoryDtos.CategoryResponse.class).id();

        long productId = rest.postForObject(url("/api/products"),
                new ProductDtos.CreateProductRequest("Kalem", "SKU-TR-77", new BigDecimal("12.00"), 20, catId),
                ProductDtos.ProductResponse.class).id();

        var order = rest.postForObject(url("/api/orders"),
                new OrderDtos.CreateOrderRequest(userId, List.of(new OrderDtos.CreateOrderItem(productId, 1))),
                OrderDtos.OrderResponse.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> patchReq = new HttpEntity<>("{\"status\":\"PAID\"}", headers);

        ResponseEntity<OrderDtos.OrderResponse> patched = rest.exchange(url("/api/orders/" + order.id()),
                HttpMethod.PATCH, patchReq, OrderDtos.OrderResponse.class);

        assertEquals(HttpStatus.OK, patched.getStatusCode());
        assertNotNull(patched.getBody());
        assertEquals("PAID", patched.getBody().status());
    }

    @Test
    void scenario_validationError_returns400() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> req = new HttpEntity<>("{\"name\":\"\",\"email\":\"yanlis\"}", headers);

        ResponseEntity<Map> res = rest.postForEntity(url("/api/users"), req, Map.class);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }
}
