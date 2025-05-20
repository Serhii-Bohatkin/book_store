package bookstore.mapper;

import bookstore.config.MapperConfig;
import bookstore.dto.orderitem.OrderItemDto;
import bookstore.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface OrderItemMapper {
    @Mapping(target = "orderItemId", source = "id")
    @Mapping(target = "bookId", source = "book.id")
    OrderItemDto toDto(OrderItem orderItem);
}
