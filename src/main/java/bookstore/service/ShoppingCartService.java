package bookstore.service;

import bookstore.dto.cartitem.CartItemDto;
import bookstore.dto.cartitem.CartItemRequestDto;
import bookstore.dto.cartitem.UpdateCartItemDto;
import bookstore.dto.shoppingcart.ShoppingCartDto;
import bookstore.model.User;

public interface ShoppingCartService {
    ShoppingCartDto getShoppingCart(User user);

    CartItemDto addBook(User user, CartItemRequestDto requestDto);

    CartItemDto update(User user, Long cartItemId, UpdateCartItemDto updateDto);

    void deleteCartItemById(User user, Long itemId);

    void clearCart(User user);
}
