package bookstore.controller;

import bookstore.controller.api.CategoryControllerApi;
import bookstore.dto.book.BookDtoWithoutCategoryIds;
import bookstore.dto.category.CategoryDto;
import bookstore.dto.category.CreateCategoryRequestDto;
import bookstore.dto.category.UpdateCategoryRequestDto;
import bookstore.dto.page.PageDto;
import bookstore.mapper.PageMapper;
import bookstore.service.BookService;
import bookstore.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class CategoryController implements CategoryControllerApi {
    private final CategoryService categoryService;
    private final PageMapper pageMapper;
    private final BookService bookService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Override
    public CategoryDto createCategory(CreateCategoryRequestDto requestDto) {
        return categoryService.save(requestDto);
    }

    @Override
    public PageDto<CategoryDto> getAll(Pageable pageable) {
        return pageMapper.toDto(categoryService.findAll(pageable));
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        return categoryService.getById(categoryId);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Override
    public CategoryDto updateCategory(Long categoryId, UpdateCategoryRequestDto requestDto) {
        return categoryService.update(categoryId, requestDto);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Override
    public void deleteCategory(Long categoryId) {
        categoryService.deleteById(categoryId);
    }

    @Override
    public PageDto<BookDtoWithoutCategoryIds> getBooksByCategoryId(Long categoryId,
                                                                   Pageable pageable) {
        return pageMapper.toDto(bookService.findByCategoryId(categoryId, pageable));
    }
}
