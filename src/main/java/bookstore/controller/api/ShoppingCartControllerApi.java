package bookstore.controller.api;

import bookstore.dto.cartitem.CartItemDto;
import bookstore.dto.cartitem.CartItemRequestDto;
import bookstore.dto.cartitem.UpdateCartItemDto;
import bookstore.dto.shoppingcart.ShoppingCartDto;
import bookstore.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping("/cart")
@Tag(name = "Shopping cart management", description = "Endpoints for managing shopping carts")
public interface ShoppingCartControllerApi {
    @Operation(summary = "Get shopping cart", description = "Get the current user's cart")
    @GetMapping
    ShoppingCartDto getShoppingCart(@AuthenticationPrincipal User user);

    @Operation(summary = "Add a book to the current user's cart",
            description = "Add a book to the current user's cart")
    @PostMapping
    CartItemDto addBook(@AuthenticationPrincipal User user,
                        @RequestBody @Valid CartItemRequestDto requestDto);

    @Operation(summary = "Clear current user's cart", description = "Clear current user's cart")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void clearCart(@AuthenticationPrincipal User user);

    @Operation(summary = "Update info about quantity books",
            description = "Update info about quantity books by item id")
    @PatchMapping("/items/{itemId}")
    CartItemDto updateCartItemQuantity(
            @AuthenticationPrincipal User user,
            @PathVariable @Min(1) Long itemId,
            @RequestBody @Valid UpdateCartItemDto updateCartItemDto
    );

    @Operation(summary = "Delete a cart item from shopping cart",
            description = "Delete a cart item from shopping cart by id")
    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@AuthenticationPrincipal User user, @PathVariable @Min(1) Long itemId);
}
