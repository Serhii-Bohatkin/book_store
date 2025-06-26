package bookstore.controller.api;

import bookstore.dto.book.BookDtoWithoutCategoryIds;
import bookstore.dto.category.CategoryDto;
import bookstore.dto.category.CreateCategoryRequestDto;
import bookstore.dto.category.UpdateCategoryRequestDto;
import bookstore.dto.page.PageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping("/categories")
@Tag(name = "Category management", description = "Endpoints for managing categories")
public interface CategoryControllerApi {
    @Operation(summary = "Create a category by id", description = "Create a category by id")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CategoryDto createCategory(@RequestBody @Valid CreateCategoryRequestDto requestDto);

    @Operation(summary = "Get all categories",
            description = "Get a list of all available categories. "
                    + "Pagination: add a ? followed by the query {page}={value}&{size}={value} "
                    + "For example: /categories?page=0&size=10 "
                    + "Sorting: add & followed by {sort}={field} or {sort}={field, DESC}")
    @GetMapping
    PageDto<CategoryDto> getAll(Pageable pageable);

    @Operation(summary = "Get a category by id", description = "Get a category by id")
    @GetMapping("/{categoryId}")
    CategoryDto getCategoryById(@PathVariable @Min(1) Long categoryId);

    @Operation(summary = "Update category", description = "Update category by id")
    @PatchMapping("/{categoryId}")
    CategoryDto updateCategory(@PathVariable @Min(1) Long categoryId,
                               @RequestBody @Valid UpdateCategoryRequestDto requestDto);

    @Operation(summary = "Delete category", description = "Delete a category by id")
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCategory(@PathVariable @Min(1) Long categoryId);

    @Operation(summary = "Find books by category", description = "Find all books by category id")
    @GetMapping("/{categoryId}/books")
    PageDto<BookDtoWithoutCategoryIds> getBooksByCategoryId(
            @PathVariable @Min(1) Long categoryId, Pageable pageable);
}
