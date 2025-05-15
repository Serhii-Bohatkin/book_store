package bookstore.controller;

import bookstore.dto.book.BookDtoWithoutCategoryIds;
import bookstore.dto.category.CategoryDto;
import bookstore.dto.category.CreateCategoryRequestDto;
import bookstore.dto.category.UpdateCategoryRequestDto;
import bookstore.dto.page.PageDto;
import bookstore.mapper.PageMapper;
import bookstore.service.BookService;
import bookstore.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Category management", description = "Endpoints for managing categories")
@Validated
public class CategoryController {
    private final CategoryService categoryService;
    private final PageMapper pageMapper;
    private final BookService bookService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Create a category by id", description = "Create a category by id")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody @Valid CreateCategoryRequestDto requestDto) {
        return categoryService.save(requestDto);
    }

    @Operation(summary = "Get all categories",
            description = "Get a list of all available categories. "
                    + "Pagination: add a ? followed by the query {page}={value}&{size}={value} "
                    + "For example: /categories?page=0&size=10 "
                    + "Sorting: add & followed by {sort}={field} or {sort}={field, DESC}")
    @GetMapping
    public PageDto<CategoryDto> getAll(Pageable pageable) {
        return pageMapper.toDto(categoryService.findAll(pageable));
    }

    @Operation(summary = "Get a category by id", description = "Get a category by id")
    @GetMapping("/{categoryId}")
    public CategoryDto getCategoryById(@PathVariable @Min(1) Long categoryId) {
        return categoryService.getById(categoryId);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update category", description = "Update category by id")
    @PatchMapping("/{categoryId}")
    public CategoryDto updateCategory(@PathVariable @Min(1) Long categoryId,
                                      @RequestBody @Valid UpdateCategoryRequestDto requestDto) {
        return categoryService.update(categoryId, requestDto);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete category", description = "Delete a category by id")
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable @Min(1) Long categoryId) {
        categoryService.deleteById(categoryId);
    }

    @Operation(summary = "Find books by category", description = "Find all books by category id")
    @GetMapping("/{categoryId}/books")
    public PageDto<BookDtoWithoutCategoryIds> getBooksByCategoryId(
            @PathVariable @Min(1) Long categoryId, Pageable pageable) {
        return pageMapper.toDto(bookService.findByCategoryId(categoryId, pageable));
    }
}
