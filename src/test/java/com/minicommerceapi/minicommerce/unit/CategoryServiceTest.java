package com.minicommerceapi.minicommerce.unit;

import com.minicommerceapi.minicommerce.exception.ConflictException;
import com.minicommerceapi.minicommerce.repo.CategoryRepository;
import com.minicommerceapi.minicommerce.repo.ProductRepository;
import com.minicommerceapi.minicommerce.service.CategoryService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryServiceTest {

    @Test
    void delete_categoryWithProducts_throwsConflict() {
        CategoryRepository catRepo = mock(CategoryRepository.class);
        ProductRepository prodRepo = mock(ProductRepository.class);

        when(catRepo.existsById(10L)).thenReturn(true);
        when(prodRepo.existsByCategoryId(10L)).thenReturn(true);

        CategoryService svc = new CategoryService(catRepo, prodRepo);
        assertThrows(ConflictException.class, () -> svc.delete(10L));
    }
}
