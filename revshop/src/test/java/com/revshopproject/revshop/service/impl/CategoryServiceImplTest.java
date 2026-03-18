package com.revshopproject.revshop.service.impl;

import com.revshopproject.revshop.dto.CategoryRequestDTO;
import com.revshopproject.revshop.entity.Category;
import com.revshopproject.revshop.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private CategoryRequestDTO categoryRequestDTO;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setCategoryId(1L);
        testCategory.setName("Electronics");

        categoryRequestDTO = new CategoryRequestDTO();
        categoryRequestDTO.setName("Electronics");
    }

    @Test
    void testGetAllCategories() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(testCategory));

        List<Category> categories = categoryService.getAllCategories();

        assertNotNull(categories);
        assertEquals(1, categories.size());
        assertEquals("Electronics", categories.get(0).getName());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void testAddCategory() {
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        Category savedCategory = categoryService.addCategory(categoryRequestDTO);

        assertNotNull(savedCategory);
        assertEquals("Electronics", savedCategory.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testGetCategoryById_Found() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        Category category = categoryService.getCategoryById(1L);

        assertNotNull(category);
        assertEquals(1L, category.getCategoryId());
        assertEquals("Electronics", category.getName());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void testGetCategoryById_NotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.getCategoryById(99L);
        });

        assertTrue(exception.getMessage().contains("not found"));
        verify(categoryRepository, times(1)).findById(99L);
    }
}
