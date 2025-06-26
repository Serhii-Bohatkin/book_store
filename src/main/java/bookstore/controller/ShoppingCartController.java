package bookstore.controller;

import bookstore.controller.api.ShoppingCartControllerApi;
import bookstore.dto.cartitem.CartItemDto;
import bookstore.dto.cartitem.CartItemRequestDto;
import bookstore.dto.cartitem.UpdateCartItemDto;
import bookstore.dto.shoppingcart.ShoppingCartDto;
import bookstore.model.User;
import bookstore.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class ShoppingCartController implements ShoppingCartControllerApi {
    private final ShoppingCartService shoppingCartService;

    @Override
    public ShoppingCartDto getShoppingCart(User user) {
        return shoppingCartService.getShoppingCart(user);
    }

    @Override
    public CartItemDto addBook(User user, CartItemRequestDto requestDto) {
        return shoppingCartService.addBook(user, requestDto);
    }

    @Override
    public void clearCart(User user) {
        shoppingCartService.clearCart(user);
    }

    @Override
    public CartItemDto updateCartItemQuantity(User user, Long itemId,
                                              UpdateCartItemDto updateCartItemDto) {
        return shoppingCartService.update(user, itemId, updateCartItemDto);
    }

    @Override
    public void delete(User user, Long itemId) {
        shoppingCartService.deleteCartItemById(user, itemId);
    }
}
