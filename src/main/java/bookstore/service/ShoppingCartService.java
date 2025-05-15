package bookstore.service;

import bookstore.dto.cartitem.CartItemRequestDto;
import bookstore.dto.cartitem.CartItemResponseDto;
import bookstore.dto.cartitem.UpdateCartItemDto;
import bookstore.dto.shoppingcart.ShoppingCartDto;
import bookstore.model.User;
import jakarta.validation.Valid;

public interface ShoppingCartService {
    ShoppingCartDto getShoppingCart(User user);

    CartItemResponseDto addBook(User user, @Valid CartItemRequestDto requestDto);

    CartItemResponseDto update(User user, Long cartItemId, UpdateCartItemDto updateDto);

    void deleteCartItemById(User user, Long itemId);
}
