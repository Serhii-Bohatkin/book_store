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
import java.text.MessageFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private static final String CART_ITEM_NOT_FOUND_MESSAGE =
            "A cart item with id {0} does not exist";
    private static final String BOOK_IS_ALREADY_IN_SHOPPING_CART_MESSAGE =
            "The book with the id {0} is already in shopping cart";
    private final ShoppingCartRepository cartRepository;
    private final ShoppingCartMapper cartMapper;
    private final BookRepository bookRepository;
    private final CartItemMapper cartItemMapper;
    private final CartItemRepository cartItemRepository;

    @Override
    public ShoppingCartDto getShoppingCart(User user) {
        return cartMapper.toDto(cartRepository.getByUserId(user.getId()));
    }

    @Transactional
    @Override
    public CartItemDto addBook(User user, CartItemRequestDto requestDto) {
        if (cartItemRepository.existsByShoppingCart_User_IdAndBook_Id(
                user.getId(), requestDto.bookId())) {
            throw new IllegalStateException(MessageFormat.format(
                    BOOK_IS_ALREADY_IN_SHOPPING_CART_MESSAGE, requestDto.bookId()));
        }
        Book book = getBookOrThrow(requestDto.bookId());
        CartItem cartItem = createNewCartItem(book, requestDto, user.getId());
        return cartItemMapper.toDto(cartItemRepository.save(cartItem));
    }

    @Transactional
    @Override
    public CartItemDto update(User user, Long cartItemId, UpdateCartItemDto updateDto) {
        CartItem cartItem = getCartItemOrThrow(cartItemId, user.getId());
        cartItemMapper.update(cartItem, updateDto);
        return cartItemMapper.toDto(cartItemRepository.save(cartItem));
    }

    @Transactional
    @Override
    public void deleteCartItemById(User user, Long itemId) {
        CartItem cartItem = getCartItemOrThrow(itemId, user.getId());
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    @Override
    public void clearCart(User user) {
        cartItemRepository.deleteAllByShoppingCart_User_Id(user.getId());
    }

    private CartItem createNewCartItem(Book book, CartItemRequestDto requestDto, Long userId) {
        return cartItemMapper.toModel(requestDto)
                .setBook(book)
                .setQuantity(requestDto.quantity())
                .setShoppingCart(new ShoppingCart(userId));
    }

    private Book getBookOrThrow(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(
                entityNotFoundException(BOOK_NOT_FOUND_MESSAGE, bookId));
    }

    private CartItem getCartItemOrThrow(Long cartItemId, Long userId) {
        return cartItemRepository.findByIdAndShoppingCart_User_Id(cartItemId, userId)
                .orElseThrow(entityNotFoundException(CART_ITEM_NOT_FOUND_MESSAGE, cartItemId));
    }
}
