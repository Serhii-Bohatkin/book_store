package bookstore.service.impl;

import static bookstore.exception.EntityNotFoundException.entityNotFoundException;
import static bookstore.service.impl.BookServiceImpl.BOOK_NOT_FOUND_MESSAGE;

import bookstore.dto.cartitem.CartItemDto;
import bookstore.dto.cartitem.CartItemRequestDto;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    protected static final String SHOPPING_CART_NOT_FOUND_MESSAGE =
            "A shopping cart with id {0} does not exist";
    private static final String CART_ITEM_NOT_FOUND_MESSAGE =
            "A cart item with id {0} does not exist";
    private static final String BOOK_IS_ALREADY_IN_SHOPPING_CART_MESSAGE =
            "The book with the id %d is already in shopping cart";
    private final ShoppingCartRepository cartRepository;
    private final ShoppingCartMapper cartMapper;
    private final BookRepository bookRepository;
    private final CartItemMapper cartItemMapper;
    private final CartItemRepository cartItemRepository;

    @Override
    public ShoppingCartDto getShoppingCart(User user) {
        return cartMapper.toDto(getCartByUserIdOrThrow(user.getId()));
    }

    @Transactional
    @Override
    public CartItemDto addBook(User user, CartItemRequestDto requestDto) {
        Long cartId = user.getId();
        if (cartItemRepository.existsByShoppingCart_IdAndBook_Id(cartId, requestDto.bookId())) {
            throw new IllegalStateException(String.format(
                    BOOK_IS_ALREADY_IN_SHOPPING_CART_MESSAGE, requestDto.bookId()));
        }
        CartItem cartItem = createNewCartItem(requestDto, cartId);
        return cartItemMapper.toDto(cartItemRepository.save(cartItem));
    }

    @Transactional
    @Override
    public CartItemDto update(User user, Long cartItemId, UpdateCartItemDto updateDto) {
        Long cartId = user.getId();
        CartItem cartItem = getCartItemOrThrow(cartItemId, cartId);
        cartItemMapper.update(cartItem, updateDto);
        return cartItemMapper.toDto(cartItemRepository.save(cartItem));
    }

    @Transactional
    @Override
    public void deleteCartItemById(User user, Long itemId) {
        Long cartId = user.getId();
        CartItem cartItem = getCartItemOrThrow(itemId, cartId);
        cartItemRepository.delete(cartItem);
    }

    private ShoppingCart getCartByUserIdOrThrow(Long userId) {
        return cartRepository.findById(userId).orElseThrow(
                entityNotFoundException(SHOPPING_CART_NOT_FOUND_MESSAGE, userId));
    }

    private CartItem createNewCartItem(CartItemRequestDto requestDto, Long shoppingCartId) {
        Book book = bookRepository.findById(requestDto.bookId()).orElseThrow(
                entityNotFoundException(BOOK_NOT_FOUND_MESSAGE, requestDto.bookId()));
        return cartItemMapper.toModel(requestDto)
                .setBook(book)
                .setQuantity(requestDto.quantity())
                .setShoppingCart(new ShoppingCart(shoppingCartId));
    }

    private CartItem getCartItemOrThrow(Long cartItemId, Long cartId) {
        return cartItemRepository.findByIdAndShoppingCartId(cartItemId, cartId)
                .orElseThrow(entityNotFoundException(CART_ITEM_NOT_FOUND_MESSAGE, cartItemId));
    }
}
