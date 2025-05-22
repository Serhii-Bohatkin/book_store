package bookstore.mapper;

import bookstore.config.MapperConfig;
import bookstore.dto.cartitem.CartItemDto;
import bookstore.dto.cartitem.CartItemRequestDto;
import bookstore.dto.cartitem.UpdateCartItemDto;
import bookstore.model.Book;
import bookstore.model.CartItem;
import bookstore.model.OrderItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CartItemMapper {
    default CartItemDto toDto(CartItem cartItem) {
        return new CartItemDto(
                cartItem.getId(),
                cartItem.getBookId(),
                cartItem.getBookTitle(),
                cartItem.getQuantity()
        );
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shoppingCart", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    CartItem toModel(CartItemRequestDto requestDto);

    @AfterMapping
    default void setBook(@MappingTarget CartItem item, CartItemRequestDto requestDto) {
        item.setBook(new Book(requestDto.bookId()));
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shoppingCart", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void update(@MappingTarget CartItem cartItem, UpdateCartItemDto updateDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "price", ignore = true)
    OrderItem toOrderItem(CartItem cartItem);

    @AfterMapping
    default void setPrice(@MappingTarget OrderItem orderItem, CartItem cartItem) {
        orderItem.setPrice(cartItem.calculateCostOfCartItem());
    }
}
