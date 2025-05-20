package bookstore.dto.shoppingcart;

import bookstore.dto.cartitem.CartItemDto;
import java.util.Set;

public record ShoppingCartDto(
         Long shoppingCartId,
         Long userId,
         Set<CartItemDto> cartItems
) {
}
