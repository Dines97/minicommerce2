package com.minicommerceapi.minicommerce.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minicommerceapi.minicommerce.dto.CategoryDtos;
import com.minicommerceapi.minicommerce.dto.ProductDtos;
import com.minicommerceapi.minicommerce.dto.ReviewDtos;
import com.minicommerceapi.minicommerce.dto.UserDtos;
import com.minicommerceapi.minicommerce.repo.CategoryRepository;
import com.minicommerceapi.minicommerce.repo.ProductRepository;
import com.minicommerceapi.minicommerce.repo.ReviewRepository;
import com.minicommerceapi.minicommerce.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
@Transactional
class ReviewIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        reviewRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void testCreateReview_Success() throws Exception {
        // Setup: Create user, category, and product
        UserDtos.UserResponse user = createUser("John Doe", "john@example.com");
        CategoryDtos.CategoryResponse category = createCategory("Electronics");
        ProductDtos.ProductResponse product = createProduct("Laptop", "SKU-001", new BigDecimal("999.99"), 10, category.id());

        // Create review
        ReviewDtos.CreateReviewRequest request = new ReviewDtos.CreateReviewRequest(
                user.id(),
                product.id(),
                5,
                "Excellent product!"
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(user.id()))
                .andExpect(jsonPath("$.productId").value(product.id()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Excellent product!"));
    }

    @Test
    void testCreateReview_InvalidRating_TooLow() throws Exception {
        UserDtos.UserResponse user = createUser("Alice", "alice@example.com");
        CategoryDtos.CategoryResponse category = createCategory("Books");
        ProductDtos.ProductResponse product = createProduct("Book", "SKU-BOOK-001", new BigDecimal("19.99"), 50, category.id());

        ReviewDtos.CreateReviewRequest request = new ReviewDtos.CreateReviewRequest(
                user.id(),
                product.id(),
                0, // Invalid: less than 1
                "Bad rating"
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateReview_InvalidRating_TooHigh() throws Exception {
        UserDtos.UserResponse user = createUser("Bob", "bob@example.com");
        CategoryDtos.CategoryResponse category = createCategory("Games");
        ProductDtos.ProductResponse product = createProduct("Game", "SKU-GAME-001", new BigDecimal("59.99"), 20, category.id());

        ReviewDtos.CreateReviewRequest request = new ReviewDtos.CreateReviewRequest(
                user.id(),
                product.id(),
                6, // Invalid: greater than 5
                "Rating too high"
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateReview_CommentTooLong() throws Exception {
        UserDtos.UserResponse user = createUser("Charlie", "charlie@example.com");
        CategoryDtos.CategoryResponse category = createCategory("Toys");
        ProductDtos.ProductResponse product = createProduct("Toy", "SKU-TOY-001", new BigDecimal("29.99"), 30, category.id());

        // Create a comment longer than 600 characters
        String longComment = "a".repeat(601);

        ReviewDtos.CreateReviewRequest request = new ReviewDtos.CreateReviewRequest(
                user.id(),
                product.id(),
                4,
                longComment
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateReview_NullUserId() throws Exception {
        CategoryDtos.CategoryResponse category = createCategory("Sports");
        ProductDtos.ProductResponse product = createProduct("Ball", "SKU-BALL-001", new BigDecimal("15.99"), 100, category.id());

        ReviewDtos.CreateReviewRequest request = new ReviewDtos.CreateReviewRequest(
                null, // Invalid: null userId
                product.id(),
                3,
                "Comment"
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetReview_Success() throws Exception {
        // Setup and create review
        UserDtos.UserResponse user = createUser("Diana", "diana@example.com");
        CategoryDtos.CategoryResponse category = createCategory("Home");
        ProductDtos.ProductResponse product = createProduct("Chair", "SKU-CHAIR-001", new BigDecimal("149.99"), 15, category.id());

        ReviewDtos.ReviewResponse createdReview = createReview(user.id(), product.id(), 4, "Good chair");

        // Get the review
        mockMvc.perform(get("/api/reviews/" + createdReview.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdReview.id()))
                .andExpect(jsonPath("$.userId").value(user.id()))
                .andExpect(jsonPath("$.productId").value(product.id()))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Good chair"));
    }

    @Test
    void testGetReview_NotFound() throws Exception {
        mockMvc.perform(get("/api/reviews/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testListReviews_All() throws Exception {
        // Create multiple reviews
        UserDtos.UserResponse user1 = createUser("User1", "user1@example.com");
        UserDtos.UserResponse user2 = createUser("User2", "user2@example.com");
        CategoryDtos.CategoryResponse category = createCategory("Category1");
        ProductDtos.ProductResponse product1 = createProduct("Product1", "SKU-P1", new BigDecimal("10.00"), 5, category.id());
        ProductDtos.ProductResponse product2 = createProduct("Product2", "SKU-P2", new BigDecimal("20.00"), 5, category.id());

        createReview(user1.id(), product1.id(), 5, "Great!");
        createReview(user2.id(), product2.id(), 3, "Average");

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testListReviews_FilteredByProductId() throws Exception {
        // Create reviews for different products
        UserDtos.UserResponse user = createUser("Reviewer", "reviewer@example.com");
        CategoryDtos.CategoryResponse category = createCategory("Tech");
        ProductDtos.ProductResponse product1 = createProduct("Product1", "SKU-PROD1", new BigDecimal("50.00"), 10, category.id());
        ProductDtos.ProductResponse product2 = createProduct("Product2", "SKU-PROD2", new BigDecimal("60.00"), 10, category.id());

        createReview(user.id(), product1.id(), 5, "Review for product 1");
        createReview(user.id(), product1.id(), 4, "Another review for product 1");
        createReview(user.id(), product2.id(), 3, "Review for product 2");

        // Filter by product1
        mockMvc.perform(get("/api/reviews")
                        .param("productId", product1.id().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].productId", everyItem(is(product1.id().intValue()))));
    }

    @Test
    void testPatchReview_Success() throws Exception {
        // Create review
        UserDtos.UserResponse user = createUser("Emma", "emma@example.com");
        CategoryDtos.CategoryResponse category = createCategory("Fashion");
        ProductDtos.ProductResponse product = createProduct("Shirt", "SKU-SHIRT-001", new BigDecimal("39.99"), 25, category.id());

        ReviewDtos.ReviewResponse createdReview = createReview(user.id(), product.id(), 3, "Initial comment");

        // Patch the review
        ReviewDtos.PatchReviewRequest patchRequest = new ReviewDtos.PatchReviewRequest(
                5,
                "Updated comment - much better!"
        );

        mockMvc.perform(patch("/api/reviews/" + createdReview.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdReview.id()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Updated comment - much better!"));
    }

    @Test
    void testPatchReview_OnlyRating() throws Exception {
        UserDtos.UserResponse user = createUser("Frank", "frank@example.com");
        CategoryDtos.CategoryResponse category = createCategory("Food");
        ProductDtos.ProductResponse product = createProduct("Snack", "SKU-SNACK-001", new BigDecimal("5.99"), 100, category.id());

        ReviewDtos.ReviewResponse createdReview = createReview(user.id(), product.id(), 2, "Not great");

        // Patch only rating
        ReviewDtos.PatchReviewRequest patchRequest = new ReviewDtos.PatchReviewRequest(
                4,
                null
        );

        mockMvc.perform(patch("/api/reviews/" + createdReview.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Not great")); // Comment unchanged
    }

    @Test
    void testPatchReview_InvalidRating() throws Exception {
        UserDtos.UserResponse user = createUser("Grace", "grace@example.com");
        CategoryDtos.CategoryResponse category = createCategory("Beauty");
        ProductDtos.ProductResponse product = createProduct("Lipstick", "SKU-LIP-001", new BigDecimal("12.99"), 50, category.id());

        ReviewDtos.ReviewResponse createdReview = createReview(user.id(), product.id(), 4, "Nice color");

        // Try to patch with invalid rating
        ReviewDtos.PatchReviewRequest patchRequest = new ReviewDtos.PatchReviewRequest(
                7, // Invalid: greater than 5
                "Trying to update"
        );

        mockMvc.perform(patch("/api/reviews/" + createdReview.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteReview_Success() throws Exception {
        // Create review
        UserDtos.UserResponse user = createUser("Henry", "henry@example.com");
        CategoryDtos.CategoryResponse category = createCategory("Garden");
        ProductDtos.ProductResponse product = createProduct("Plant", "SKU-PLANT-001", new BigDecimal("25.00"), 20, category.id());

        ReviewDtos.ReviewResponse createdReview = createReview(user.id(), product.id(), 5, "Beautiful plant");

        // Delete the review
        mockMvc.perform(delete("/api/reviews/" + createdReview.id()))
                .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(get("/api/reviews/" + createdReview.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteReview_NotFound() throws Exception {
        mockMvc.perform(delete("/api/reviews/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testMultipleReviewsForSameProduct() throws Exception {
        // Test that multiple users can review the same product
        UserDtos.UserResponse user1 = createUser("User A", "usera@example.com");
        UserDtos.UserResponse user2 = createUser("User B", "userb@example.com");
        UserDtos.UserResponse user3 = createUser("User C", "userc@example.com");

        CategoryDtos.CategoryResponse category = createCategory("Popular");
        ProductDtos.ProductResponse product = createProduct("Popular Item", "SKU-POP-001", new BigDecimal("99.99"), 100, category.id());

        createReview(user1.id(), product.id(), 5, "Loved it!");
        createReview(user2.id(), product.id(), 4, "Pretty good");
        createReview(user3.id(), product.id(), 3, "It's okay");

        mockMvc.perform(get("/api/reviews")
                        .param("productId", product.id().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].productId", everyItem(is(product.id().intValue()))));
    }

    // Helper methods

    private UserDtos.UserResponse createUser(String name, String email) throws Exception {
        UserDtos.CreateUserRequest userReq = new UserDtos.CreateUserRequest(name, email);
        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), UserDtos.UserResponse.class);
    }

    private CategoryDtos.CategoryResponse createCategory(String name) throws Exception {
        CategoryDtos.CreateCategoryRequest categoryReq = new CategoryDtos.CreateCategoryRequest(name);
        MvcResult result = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryReq)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), CategoryDtos.CategoryResponse.class);
    }

    private ProductDtos.ProductResponse createProduct(String name, String sku, BigDecimal price, int stock, Long categoryId) throws Exception {
        ProductDtos.CreateProductRequest productReq = new ProductDtos.CreateProductRequest(name, sku, price, stock, categoryId);
        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productReq)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), ProductDtos.ProductResponse.class);
    }

    private ReviewDtos.ReviewResponse createReview(Long userId, Long productId, int rating, String comment) throws Exception {
        ReviewDtos.CreateReviewRequest reviewReq = new ReviewDtos.CreateReviewRequest(userId, productId, rating, comment);
        MvcResult result = mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), ReviewDtos.ReviewResponse.class);
    }
}
