package bookstore.service.impl;

import static bookstore.exception.EntityNotFoundException.entityNotFoundException;
import static bookstore.service.impl.BookServiceImpl.BOOK_NOT_FOUND_MESSAGE;

import bookstore.dto.cartitem.CartItemRequestDto;
import bookstore.dto.cartitem.CartItemResponseDto;
import bookstore.dto.cartitem.UpdateCartItemDto;
import bookstore.dto.shoppingcart.ShoppingCartDto;
import bookstore.mapper.CartItemMapper;
import bookstore.mapper.ShoppingCartMapper;
import bookstore.model.Book;
import bookstore.model.CartItem;
import bookstore.model.ShoppingCart;
import bookstore.model.User;
import bookstore.repository.BookRepository;
import bookstore.repository.CartItemRepository;
import bookstore.repository.ShoppingCartRepository;
import bookstore.service.ShoppingCartService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private static final String SHOPPING_CART_NOT_FOUND_MESSAGE =
            "A shopping cart with id {0} does not exist";
    private static final String CART_ITEM_NOT_FOUND_MESSAGE =
            "A cart item with id {0} does not exist";
    private final ShoppingCartRepository cartRepository;
    private final ShoppingCartMapper cartMapper;
    private final BookRepository bookRepository;
    private final CartItemMapper itemMapper;
    private final CartItemRepository itemRepository;

    @Override
    public ShoppingCartDto getShoppingCart(User user) {
        return cartMapper.toDto(getCartById(user.getId()));
    }

    @Transactional
    @Override
    public CartItemResponseDto addBook(User user, CartItemRequestDto requestDto) {
        ShoppingCart shoppingCart = getCartById(user.getId());
        Optional<CartItem> itemFromCart = shoppingCart.findCartItemByBookId(requestDto.bookId());
        CartItem cartItem = itemFromCart.orElseGet(
                () -> createNewCartItem(requestDto, shoppingCart));
        itemRepository.save(cartItem);
        return itemMapper.toDto(cartItem);
    }

    @Transactional
    @Override
    public CartItemResponseDto update(User user, Long cartItemId, UpdateCartItemDto updateDto) {
        ShoppingCart shoppingCart = getCartById(user.getId());
        CartItem cartItem = shoppingCart.findCartItemById(cartItemId)
                .orElseThrow(entityNotFoundException(CART_ITEM_NOT_FOUND_MESSAGE, cartItemId));
        itemMapper.update(cartItem, updateDto);
        itemRepository.save(cartItem);
        return itemMapper.toDto(cartItem);
    }

    @Transactional
    @Override
    public void deleteCartItemById(User user, Long itemId) {
        ShoppingCart shoppingCart = getCartById(user.getId());
        CartItem cartItem = shoppingCart.findCartItemById(itemId).orElseThrow(
                entityNotFoundException(CART_ITEM_NOT_FOUND_MESSAGE, itemId));
        itemRepository.delete(cartItem);
    }

    private ShoppingCart getCartById(Long id) {
        return cartRepository.findById(id).orElseThrow(
                entityNotFoundException(SHOPPING_CART_NOT_FOUND_MESSAGE, id));
    }

    private CartItem createNewCartItem(CartItemRequestDto requestDto, ShoppingCart shoppingCart) {
        Book book = bookRepository.findById(requestDto.bookId()).orElseThrow(
                entityNotFoundException(BOOK_NOT_FOUND_MESSAGE, requestDto.bookId()));
        return itemMapper.toModel(requestDto)
                .setBook(book)
                .setQuantity(requestDto.quantity())
                .setShoppingCart(shoppingCart);
    }
}
