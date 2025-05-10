package bookstore.mapper;

import bookstore.config.MapperConfig;
import bookstore.dto.page.PageDto;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(config = MapperConfig.class)
public interface PageMapper {
    default <T> PageDto<T> toDto(Page<T> page) {
        if (page == null) {
            return null;
        }
        return new PageDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
