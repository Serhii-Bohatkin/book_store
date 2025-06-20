package bookstore;

import bookstore.dto.book.BookDto;
import bookstore.dto.book.BookDtoWithoutCategoryIds;
import bookstore.dto.book.BookSearchParametersDto;
import bookstore.dto.book.CreateBookRequestDto;
import bookstore.dto.book.UpdateBookRequestDto;
import bookstore.dto.cartitem.CartItemDto;
import bookstore.dto.cartitem.CartItemRequestDto;
import bookstore.dto.cartitem.UpdateCartItemDto;
import bookstore.dto.category.CategoryDto;
import bookstore.dto.category.CreateCategoryRequestDto;
import bookstore.dto.category.UpdateCategoryRequestDto;
import bookstore.dto.order.OrderAddressDto;
import bookstore.dto.order.OrderDto;
import bookstore.dto.order.OrderStatusDto;
import bookstore.dto.orderitem.OrderItemDto;
import bookstore.dto.shoppingcart.ShoppingCartDto;
import bookstore.dto.user.UserLoginRequestDto;
import bookstore.dto.user.UserRegistrationRequestDto;
import bookstore.dto.user.UserResponseDto;
import bookstore.dto.user.UserUpdateDto;
import bookstore.model.Book;
import bookstore.model.CartItem;
import bookstore.model.Category;
import bookstore.model.Order;
import bookstore.model.OrderItem;
import bookstore.model.Role;
import bookstore.model.ShoppingCart;
import bookstore.model.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

public class TestObjectsFactory {

    private static final PageRequest DEFAULT_PAGE_REQUEST = PageRequest.of(0, 10);

    public static Book create1984Book() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("1984");
        book.setAuthor("George Orwell");
        book.setIsbn("9780451524935");
        book.setPrice(BigDecimal.valueOf(9.99));
        book.setDescription("Dystopian novel about totalitarian regime of Big Brother.");
        book.setCoverImage(
                "https://upload.wikimedia.org/wikipedia/commons/5/51/1984_first_edition_cover.jpg");
        book.setCategories(Set.of(createFictionCategory()));
        return book;
    }

    public static Book createToKillMockingbirdBook() {
        Book book = new Book();
        book.setId(2L);
        book.setTitle("To Kill a Mockingbird");
        book.setAuthor("Harper Lee");
        book.setIsbn("9780061120084");
        book.setPrice(BigDecimal.valueOf(10.99));
        book.setDescription("A powerful novel set in the American South, exploring themes of racial"
                + " injustice, moral growth, and compassion through the eyes of a young girl.");
        book.setCoverImage("https://upload.wikimedia.org/wikipedia/commons/4/4f/To_Kill_a_Mockingbi"
                + "rd_%28first_edition_cover%29.jpg");
        book.setCategories(Set.of(createFictionCategory()));
        return book;
    }

    public static Book createABriefHistoryOfTimeBook() {
        Book book = new Book();
        book.setId(3L);
        book.setTitle("A Brief History of Time");
        book.setAuthor("Stephen Hawking");
        book.setIsbn("9780553380163");
        book.setPrice(BigDecimal.valueOf(14.99));
        book.setDescription("Non-fiction book explaining cosmology for general audience.");
        book.setCoverImage("https://upload.wikimedia.org/wikipedia/en/a/a3/BriefHistoryTime.jpg");
        book.setCategories(Set.of(createNonFictionCategory()));
        return book;
    }

    public static Category createFictionCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Fiction");
        category.setDescription("Fictional books");
        return category;
    }

    public static Category createNonFictionCategory() {
        Category category = new Category();
        category.setId(2L);
        category.setName("Non Fiction");
        category.setDescription("Non Fiction books");
        return category;
    }

    public static BookSearchParametersDto createSearchParameters(String title) {
        return new BookSearchParametersDto(
                title,
                null,
                null,
                null,
                null
        );
    }

    public static BookSearchParametersDto createEmptySearchParameters() {
        return new BookSearchParametersDto(
                null,
                null,
                null,
                null,
                null
        );
    }

    public static ShoppingCart createEmptyShoppingCart() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(1L);
        shoppingCart.setUser(createUser());
        return shoppingCart;
    }

    public static ShoppingCart createShoppingCartWithTwoBooks() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(1L);
        shoppingCart.setUser(createUser());
        shoppingCart.setCartItems(Set.of(createCartItem(), createSecondCartItem()));
        return shoppingCart;
    }

    public static ShoppingCartDto createShoppingCartDto() {
        return new ShoppingCartDto(
                1L,
                1L,
                Set.of(createCartItemDto(), createSecondCartItemDto())
        );
    }

    public static UserLoginRequestDto createUserLoginRequestDto() {
        return new UserLoginRequestDto(
                "user@gmail.com",
                "xBO4cH2f5603Na4ZteL!"
        );
    }

    public static UserRegistrationRequestDto createRegistrationRequestDto(String email,
                                                                          String inviteCode) {
        return new UserRegistrationRequestDto(
                email,
                "xBO4cH2f5603Na4ZteL!",
                "xBO4cH2f5603Na4ZteL!",
                "John",
                "Doe",
                "26871, Zaporizhzhya region, Zaporizhzhya, st. Kopilenka, 94",
                inviteCode
        );
    }

    public static UserRegistrationRequestDto createInvalidRegistrationRequestDto() {
        return new UserRegistrationRequestDto(
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7"
        );
    }

    public static UserUpdateDto createUserUpdateDto(String name) {
        return new UserUpdateDto(
                name,
                "Doe",
                "26871, Zaporizhzhya region, Zaporizhzhya, st. Kopilenka, 94"
        );
    }

    public static User createUser() {

        HashSet<Role> roles = new HashSet<>();
        roles.add(createUserRole());
        return new User()
                .setId(1L)
                .setEmail("user@gmail.com")
                .setPassword("$2a$10$TiWXRP/UCiXdc21fpBvQu.HqHwVuvPPZH7/FgUsHMDnLq2sxlSD62")
                .setFirstName("John")
                .setLastName("Doe")
                .setShippingAddress("26871, Zaporizhzhya region, Zaporizhzhya, st. Kopilenka, 94")
                .setRoles(roles);
    }

    public static UserResponseDto createUserResponseDto(String email) {
        return new UserResponseDto(
                1L,
                email,
                "John",
                "Doe",
                "26871, Zaporizhzhya region, Zaporizhzhya, st. Kopilenka, 94"
        );
    }

    public static Role createUserRole() {
        Role role = new Role();
        role.setId(1L);
        role.setName(Role.RoleName.USER);
        return role;
    }

    public static Role createAdminRole() {
        Role role = new Role();
        role.setId(2L);
        role.setName(Role.RoleName.ADMIN);
        return role;
    }

    public static CartItemRequestDto createCartItemRequestDto(Long bookId, int quantity) {
        return new CartItemRequestDto(bookId, quantity);
    }

    public static CartItem createCartItem() {
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setBook(create1984Book());
        cartItem.setQuantity(1);
        return cartItem;
    }

    public static CartItem createSecondCartItem() {
        CartItem cartItem = new CartItem();
        cartItem.setId(2L);
        cartItem.setBook(createToKillMockingbirdBook());
        cartItem.setQuantity(2);
        return cartItem;
    }

    public static UpdateCartItemDto createUpdateCartItemDto(int quantity) {
        return new UpdateCartItemDto(
                quantity
        );
    }

    public static CartItemDto createCartItemDto() {
        return new CartItemDto(
                1L,
                1L,
                "1984",
                1
        );
    }

    public static CartItemDto createSecondCartItemDto() {
        return new CartItemDto(
                2L,
                2L,
                "To Kill a Mockingbird",
                2
        );
    }

    public static OrderItem createOrderItem() {
        return new OrderItem()
                .setId(1L)
                .setBook(create1984Book())
                .setQuantity(1)
                .setPrice(BigDecimal.valueOf(9.99));
    }

    public static OrderItem createSecondOrderItem() {
        return new OrderItem()
                .setId(2L)
                .setBook(createToKillMockingbirdBook())
                .setQuantity(2)
                .setPrice(BigDecimal.valueOf(21.98));
    }

    public static OrderItemDto createOrderItemDto() {
        return new OrderItemDto(1L, 1L, 1);
    }

    public static OrderItemDto createSecondOrderItemDto() {
        return new OrderItemDto(2L, 2L, 2);
    }

    public static Order createOrder() {
        return new Order()
                .setId(1L)
                .setUser(createUser())
                .setTotal(BigDecimal.valueOf(31.97))
                .setOrderDate(LocalDateTime.parse("2025-06-01T00:00:00"))
                .setShippingAddress("26871, Zaporizhzhya region, Zaporizhzhya, st. Kopilenka, 94")
                .setOrderItems(Set.of(createOrderItem(), createSecondOrderItem()));
    }

    public static OrderAddressDto createOrderAddressDto() {
        return new OrderAddressDto("26871, Zaporizhzhya region, Zaporizhzhya, st. Kopilenka, 94");
    }

    public static OrderDto createOrderDtoWithStatus(Order.Status status) {
        return new OrderDto(
                1L,
                1L,
                status,
                BigDecimal.valueOf(31.97),
                LocalDateTime.parse("2025-06-01T00:00:00"),
                "26871, Zaporizhzhya region, Zaporizhzhya, st. Kopilenka, 94",
                Set.of(createOrderItemDto(), createSecondOrderItemDto())
        );
    }

    public static CreateBookRequestDto createBookRequestDto() {
        return new CreateBookRequestDto(
                "1984",
                "George Orwell",
                "9780451524935",
                BigDecimal.valueOf(9.99),
                "Dystopian novel about totalitarian regime of Big Brother.",
                "https://upload.wikimedia.org/wikipedia/commons/5/51/1984_first_edition_cover.jpg",
                List.of(1L)
        );
    }

    public static CreateBookRequestDto createInvalidBookRequestDto() {
        return new CreateBookRequestDto(
                "",
                "",
                "someText",
                BigDecimal.valueOf(-9.99),
                null,
                null,
                null
        );
    }

    public static CreateBookRequestDto createBookRequestDtoWithoutCategory() {
        return new CreateBookRequestDto(
                "1984",
                "George Orwell",
                "9780451524935",
                BigDecimal.valueOf(9.99),
                "Dystopian novel about totalitarian regime of Big Brother.",
                "https://upload.wikimedia.org/wikipedia/commons/5/51/1984_first_edition_cover.jpg",
                Collections.emptyList()
        );
    }

    public static BookDto createToKillMockingbirdBookDto() {
        return new BookDto(
                2L,
                "To Kill a Mockingbird",
                "Harper Lee",
                "9780061120084",
                BigDecimal.valueOf(10.99),
                "A powerful novel set in the American South, exploring"
                        + " themes of racial injustice, moral growth, and compassion"
                        + " through the eyes of a young girl.",
                "https://upload.wikimedia.org/wikipedia/commons/4/4f/To_Kill_a_Mockingbi"
                        + "rd_%28first_edition_cover%29.jpg",
                List.of(1L)
        );
    }

    public static BookDto create1984BookDto(String title) {
        return new BookDto(
                1L,
                title,
                "George Orwell",
                "9780451524935",
                BigDecimal.valueOf(9.99),
                "Dystopian novel about totalitarian regime of Big Brother.",
                "https://upload.wikimedia.org/wikipedia/commons/5/51/1984_first_edition_cover.jpg",
                List.of(1L)
        );
    }

    public static BookDto createABriefHistoryOfTimeBookDto() {
        return new BookDto(
                3L,
                "A Brief History of Time",
                "Stephen Hawking",
                "9780553380163",
                BigDecimal.valueOf(14.99),
                "Non-fiction book explaining cosmology for general audience.",
                "https://upload.wikimedia.org/wikipedia/en/a/a3/BriefHistoryTime.jpg",
                List.of(2L)
        );
    }

    public static List<Book> createThreeBooksList() {
        return List.of(
                create1984Book(),
                createToKillMockingbirdBook(),
                createABriefHistoryOfTimeBook()
        );
    }

    public static List<BookDto> createThreeBookDtosList() {
        return List.of(
                create1984BookDto("1984"),
                createToKillMockingbirdBookDto(),
                createABriefHistoryOfTimeBookDto()
        );
    }

    public static Page<Book> createTwoBooksPage() {
        return new PageImpl<>(
                List.of(create1984Book(), createToKillMockingbirdBook()),
                DEFAULT_PAGE_REQUEST,
                2
        );
    }

    public static UpdateBookRequestDto createUpdateBookRequestDto(String title) {
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(1L);
        return new UpdateBookRequestDto(
                title,
                "George Orwell",
                "9780451524935",
                BigDecimal.valueOf(9.99),
                "Dystopian novel about totalitarian regime of Big Brother.",
                "https://upload.wikimedia.org/wikipedia/commons/5/51/1984_first_edition_cover.jpg",
                categoryIds
        );
    }

    public static UpdateBookRequestDto createInvalidUpdateBookRequestDto() {
        return new UpdateBookRequestDto(
                null,
                null,
                "someText",
                BigDecimal.valueOf(-9.99),
                null,
                null,
                null
        );
    }

    public static Page<Book> createOneBookPage() {
        return new PageImpl<>(
                List.of(create1984Book()),
                DEFAULT_PAGE_REQUEST,
                1
        );
    }

    public static BookDtoWithoutCategoryIds create1984BookDtoWithoutCategory() {
        return new BookDtoWithoutCategoryIds(
                1L,
                "1984",
                "George Orwell",
                "9780451524935",
                BigDecimal.valueOf(9.99),
                "Dystopian novel about totalitarian regime of Big Brother.",
                "https://upload.wikimedia.org/wikipedia/commons/5/51/1984_first_edition_cover.jpg"
        );
    }

    public static BookDtoWithoutCategoryIds createToKillMockingbirdDtoWithoutCategory() {
        return new BookDtoWithoutCategoryIds(
                2L,
                "To Kill a Mockingbird",
                "Harper Lee",
                "9780061120084",
                BigDecimal.valueOf(10.99),
                "A powerful novel set in the American South, exploring"
                        + " themes of racial injustice, moral growth, and compassion"
                        + " through the eyes of a young girl.",
                "https://upload.wikimedia.org/wikipedia/commons/4/4f/To_Kill_a_Mockingbi"
                        + "rd_%28first_edition_cover%29.jpg"
        );
    }

    public static List<BookDtoWithoutCategoryIds> createTwoBookDtoWithoutCategoryIdsList() {
        return List.of(
                create1984BookDtoWithoutCategory(),
                createToKillMockingbirdDtoWithoutCategory()
        );
    }

    public static Page<Category> createTwoCategoriesPage() {
        return new PageImpl<>(
                List.of(createFictionCategory(), createNonFictionCategory()),
                DEFAULT_PAGE_REQUEST,
                2
        );
    }

    public static Page<Order> createOneOrderPage() {
        return new PageImpl<>(
                List.of(createOrder()),
                DEFAULT_PAGE_REQUEST,
                1
        );
    }

    public static CategoryDto createFictionCategoryDto() {
        return new CategoryDto(
                1L,
                "Fiction",
                "Fictional books"
        );
    }

    public static CategoryDto createFantasyCategoryDto() {
        return new CategoryDto(
                2L,
                "Fantasy",
                "Fantasy books"
        );
    }

    public static List<CategoryDto> createTwoCategoryDtosList() {
        return List.of(
                createFictionCategoryDto(),
                createFantasyCategoryDto()
        );
    }

    public static CreateCategoryRequestDto createCategoryRequestDto() {
        return new CreateCategoryRequestDto(
                "Fiction",
                "Fictional books"
        );
    }

    public static UpdateCategoryRequestDto createUpdateCategoryRequestDto() {
        return new UpdateCategoryRequestDto(
                "Fiction",
                "Fictional books"
        );
    }

    public static OrderStatusDto createOrderStatusDto(Order.Status status) {
        return new OrderStatusDto(status);
    }

    public static Page<OrderItem> createTwoOrderItemsPage() {
        return new PageImpl<>(
                List.of(createOrderItem(), createSecondOrderItem()),
                DEFAULT_PAGE_REQUEST,
                2
        );
    }
}
