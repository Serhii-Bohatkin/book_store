package bookstore.mapper;

import bookstore.config.MapperConfig;
import bookstore.dto.shoppingcart.ShoppingCartDto;
import bookstore.model.ShoppingCart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class, uses = CartItemMapper.class)
public interface ShoppingCartMapper {
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "shoppingCartId", source = "id")
    ShoppingCartDto toDto(ShoppingCart shoppingCart);
}
