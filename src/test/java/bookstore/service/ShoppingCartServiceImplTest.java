package bookstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import bookstore.TestObjectsFactory;
import bookstore.dto.cartitem.CartItemDto;
import bookstore.dto.cartitem.CartItemRequestDto;
import bookstore.dto.cartitem.UpdateCartItemDto;
import bookstore.dto.shoppingcart.ShoppingCartDto;
import bookstore.exception.EntityNotFoundException;
import bookstore.mapper.CartItemMapper;
import bookstore.mapper.ShoppingCartMapper;
import bookstore.model.Book;
import bookstore.model.CartItem;
import bookstore.model.ShoppingCart;
import bookstore.model.User;
import bookstore.repository.BookRepository;
import bookstore.repository.CartItemRepository;
import bookstore.repository.ShoppingCartRepository;
import bookstore.service.impl.ShoppingCartServiceImpl;
import java.text.MessageFormat;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {
    private static final String CART_ITEM_NOT_FOUND_MESSAGE =
            "A cart item with id {0} does not exist";
    private static final String BOOK_IS_ALREADY_IN_SHOPPING_CART_MESSAGE =
            "The book with the id {0} is already in shopping cart";
    private static final String BOOK_NOT_FOUND_MESSAGE = "A book with id {0} does not exist";

    private static final Long BOOK_ID = 1L;
    private static final Long SHOPPING_CART_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long CART_ITEM_ID = 1L;

    private CartItemRequestDto cartItemRequestDto;
    private CartItem cartItem;
    private CartItemDto cartItemDto;
    private User user;

    @Mock
    private ShoppingCartRepository cartRepository;
    @Mock
    private ShoppingCartMapper cartMapper;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CartItemMapper cartItemMapper;
    @Mock
    private CartItemRepository cartItemRepository;
    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    @BeforeEach
    void setUp() {
        cartItemRequestDto = TestObjectsFactory.createCartItemRequestDto(1L, 1);
        cartItem = TestObjectsFactory.createCartItem();
        cartItemDto = TestObjectsFactory.createCartItemDto();
        user = TestObjectsFactory.createUser();
    }

    @AfterEach
    void verifyNoUnexpectedInteractions() {
        verifyNoMoreInteractions(cartRepository, cartMapper, bookRepository, cartItemMapper,
                cartItemRepository);
    }

    @Nested
    class GetShoppingCartMethodTests {
        @Test
        @DisplayName("Should return shopping cart DTO")
        void getShoppingCart_ShouldReturnShoppingCart() {
            ShoppingCart shoppingCart = TestObjectsFactory.createShoppingCartWithTwoBooks();
            ShoppingCartDto shoppingCartDto = TestObjectsFactory.createShoppingCartDto();

            when(cartRepository.getByUserId(USER_ID)).thenReturn(shoppingCart);
            when(cartMapper.toDto(shoppingCart)).thenReturn(shoppingCartDto);

            ShoppingCartDto actual = shoppingCartService.getShoppingCart(user);

            assertThat(actual).isEqualTo(shoppingCartDto);
            verify(cartRepository).getByUserId(USER_ID);
            verify(cartMapper).toDto(shoppingCart);
        }
    }

    @Nested
    class AddBookMethodTests {
        @Test
        @DisplayName("Should add book to cart when it is not already present")
        void addBook_BookNotPresentInCartYet_ShouldAddBookToCart() {
            Book book = TestObjectsFactory.create1984Book();

            when(cartItemRepository.existsByShoppingCart_User_IdAndBook_Id(USER_ID, BOOK_ID))
                    .thenReturn(false);
            when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));
            when(cartItemMapper.toModel(cartItemRequestDto)).thenReturn(cartItem);
            when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
            when(cartItemMapper.toDto(cartItem)).thenReturn(cartItemDto);

            CartItemDto actual = shoppingCartService.addBook(user, cartItemRequestDto);

            assertThat(actual).isEqualTo(cartItemDto);
            verify(cartItemRepository).existsByShoppingCart_User_IdAndBook_Id(SHOPPING_CART_ID,
                    BOOK_ID);
            verify(bookRepository).findById(BOOK_ID);
            verify(cartItemMapper).toModel(cartItemRequestDto);
            verify(cartItemRepository).save(cartItem);
            verify(cartItemMapper).toDto(cartItem);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when book is already in the cart")
        void addBook_BookAlreadyPresentInCart_ShouldThrowIllegalStateException() {
            when(cartItemRepository.existsByShoppingCart_User_IdAndBook_Id(
                    SHOPPING_CART_ID, BOOK_ID)).thenReturn(true);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> shoppingCartService.addBook(user, cartItemRequestDto));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(BOOK_IS_ALREADY_IN_SHOPPING_CART_MESSAGE, BOOK_ID));
            verify(cartItemRepository).existsByShoppingCart_User_IdAndBook_Id(SHOPPING_CART_ID,
                    BOOK_ID);
            verify(cartItemRepository, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when book ID is invalid")
        void addBook_InvalidBookId_ShouldThrowEntityNotFoundException() {
            when(cartItemRepository.existsByShoppingCart_User_IdAndBook_Id(USER_ID, BOOK_ID))
                    .thenReturn(false);
            when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> shoppingCartService.addBook(user, cartItemRequestDto));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(BOOK_NOT_FOUND_MESSAGE, BOOK_ID));
            verify(cartItemRepository).existsByShoppingCart_User_IdAndBook_Id(USER_ID, BOOK_ID);
            verify(bookRepository).findById(BOOK_ID);
            verify(cartItemRepository, never()).save(any(CartItem.class));
        }
    }

    @Nested
    class UpdateMethodTests {
        private UpdateCartItemDto updateCartItemDto;

        @BeforeEach
        void setUp() {
            updateCartItemDto = TestObjectsFactory.createUpdateCartItemDto(1);
        }

        @Test
        @DisplayName("Should return updated cart item DTO when item is found")
        void update_ItemFound_ShouldReturnCartItemDto() {
            when(cartItemRepository.findByIdAndShoppingCart_User_Id(CART_ITEM_ID, USER_ID))
                    .thenReturn(Optional.of(cartItem));
            doNothing().when(cartItemMapper).update(cartItem, updateCartItemDto);
            when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
            when(cartItemMapper.toDto(cartItem)).thenReturn(cartItemDto);

            CartItemDto actual = shoppingCartService.update(user, CART_ITEM_ID, updateCartItemDto);

            assertThat(actual).isEqualTo(cartItemDto);
            verify(cartItemRepository).findByIdAndShoppingCart_User_Id(CART_ITEM_ID, USER_ID);
            verify(cartItemMapper).update(cartItem, updateCartItemDto);
            verify(cartItemRepository).save(cartItem);
            verify(cartItemMapper).toDto(cartItem);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when cart item is not found")
        void update_ItemNotFound_ShouldThrowEntityNotFoundException() {
            when(cartItemRepository.findByIdAndShoppingCart_User_Id(CART_ITEM_ID, USER_ID))
                    .thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> shoppingCartService.update(user, CART_ITEM_ID, updateCartItemDto));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(CART_ITEM_NOT_FOUND_MESSAGE, CART_ITEM_ID));
            verify(cartItemRepository).findByIdAndShoppingCart_User_Id(CART_ITEM_ID, USER_ID);
            verify(cartItemRepository, never()).save(any(CartItem.class));
        }
    }

    @Nested
    class DeleteCartItemByIdMethodTests {
        @Test
        @DisplayName("Should delete cart item when valid ID is provided")
        void deleteCartItemById_ValidId_ShouldDeleteCartItem() {
            when(cartItemRepository.findByIdAndShoppingCart_User_Id(CART_ITEM_ID, USER_ID))
                    .thenReturn(Optional.of(cartItem));
            doNothing().when(cartItemRepository).delete(cartItem);

            shoppingCartService.deleteCartItemById(user, CART_ITEM_ID);

            verify(cartItemRepository).findByIdAndShoppingCart_User_Id(CART_ITEM_ID, USER_ID);
            verify(cartItemRepository).delete(cartItem);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when cart item is not found")
        void deleteCartItemById_InvalidId_ShouldThrowEntityNotFoundException() {
            when(cartItemRepository.findByIdAndShoppingCart_User_Id(CART_ITEM_ID, USER_ID))
                    .thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> shoppingCartService.deleteCartItemById(user, CART_ITEM_ID));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(CART_ITEM_NOT_FOUND_MESSAGE, CART_ITEM_ID));
            verify(cartItemRepository).findByIdAndShoppingCart_User_Id(CART_ITEM_ID, USER_ID);
            verify(cartItemRepository, never()).delete(any(CartItem.class));
        }
    }

    @Nested
    class ClearCartMethodTests {
        @Test
        @DisplayName("Should call repository to delete all cart items for the given user")
        void clearCart_ShouldCallDeleteAllByShoppingCart_User_IdFromRepository() {
            doNothing().when(cartItemRepository).deleteAllByShoppingCart_User_Id(USER_ID);

            shoppingCartService.clearCart(user);

            verify(cartItemRepository, times(1)).deleteAllByShoppingCart_User_Id(USER_ID);
        }
    }
}
