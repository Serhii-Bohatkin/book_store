package bookstore.service.impl;

import static bookstore.exception.EntityNotFoundException.entityNotFoundException;

import bookstore.dto.category.CategoryDto;
import bookstore.dto.category.CreateCategoryRequestDto;
import bookstore.dto.category.UpdateCategoryRequestDto;
import bookstore.exception.EntityAlreadyExistsException;
import bookstore.exception.EntityNotFoundException;
import bookstore.mapper.CategoryMapper;
import bookstore.model.Category;
import bookstore.repository.CategoryRepository;
import bookstore.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    protected static final String CATEGORY_NOT_FOUND_MESSAGE =
            "A category with id {0} does not exist";
    private static final String CATEGORY_ALREADY_EXISTS_MESSAGE =
            "A category with name {0} already exists";
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public Page<CategoryDto> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toDto);
    }

    @Override
    public CategoryDto getById(Long categoryId) {
        Category category = getCategoryOrThrow(categoryId);
        return categoryMapper.toDto(category);
    }

    @Transactional
    @Override
    public CategoryDto save(CreateCategoryRequestDto requestDto) {
        if (categoryRepository.existsByName(requestDto.name())) {
            throw new EntityAlreadyExistsException(
                    CATEGORY_ALREADY_EXISTS_MESSAGE, requestDto.name());
        }
        Category savedCategory = categoryRepository.save(categoryMapper.toModel(requestDto));
        return categoryMapper.toDto(savedCategory);
    }

    @Transactional
    @Override
    public CategoryDto update(Long categoryId, UpdateCategoryRequestDto requestDto) {
        Category category = getCategoryOrThrow(categoryId);
        categoryMapper.updateCategory(category, requestDto);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    @Override
    public void deleteById(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException(CATEGORY_NOT_FOUND_MESSAGE, categoryId);
        }
        categoryRepository.deleteById(categoryId);
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(entityNotFoundException(CATEGORY_NOT_FOUND_MESSAGE, id));
    }
}
