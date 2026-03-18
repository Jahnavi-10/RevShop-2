# RevShop – Testing Artifacts

## Testing Strategy

RevShop uses a comprehensive unit testing strategy to verify the correctness of all
business logic in the Service layer. Tests are written using JUnit 5 and Mockito,
ensuring each service is tested in complete isolation without touching the database.

### Testing Types
- Unit Testing (Primary)
- Integration Testing
- Manual UI Testing
- Functional Testing

---

## Unit Testing

### Framework Used
- JUnit 5 (Jupiter)
- Mockito (for mocking repositories and dependencies)

### Approach
All Repository dependencies are mocked using Mockito `@Mock`.
The Service implementation is injected using `@InjectMocks`.
This ensures only the business logic of the Service is tested — not the database.

---

## Test Classes & Results

| Test Class | Tests Run | Failures | Errors | Skipped | Time |
|---|---|---|---|---|---|
| UserServiceImplTest | 6 | 0 | 0 | 0 | 0.183s |
| ProductServiceImplTest | 10 | 0 | 0 | 0 | 0.124s |
| OrderServiceImplTest | 5 | 0 | 0 | 0 | 0.246s |
| CartServiceImplTest | 9 | 0 | 0 | 0 | 0.757s |
| ReviewServiceImplTest | 7 | 0 | 0 | 0 | 0.115s |
| CategoryServiceImplTest | 4 | 0 | 0 | 0 | 0.128s |
| FavoriteServiceImplTest | 6 | 0 | 0 | 0 | 0.221s |
| NotificationServiceImplTest | 6 | 0 | 0 | 0 | 0.107s |
| SellerServiceImplTest | 2 | 0 | 0 | 0 | 0.012s |
| ApplicationTests | 1 | 0 | 0 | 0 | 7.076s |
| **TOTAL** | **56** | **0** | **0** | **0** | — |

✅ All 56 tests passed. Zero failures. Zero errors.

---

## Test Cases

---

### UserServiceImplTest

**TC_USER_01 — Register New User Successfully**
- Input: Valid UserRegistrationDTO with name, email, password, role BUYER
- Expected: User saved to DB with BCrypt hashed password, Cart automatically created
- Result: ✅ PASS

**TC_USER_02 — Register Duplicate Email**
- Input: Registration DTO with an email that already exists in DB
- Expected: UserAlreadyExistsException thrown
- Result: ✅ PASS

**TC_USER_03 — Change Password Successfully**
- Input: ChangePasswordDTO with correct current password and new password
- Expected: Password updated and BCrypt re-encoded
- Result: ✅ PASS

**TC_USER_04 — Forgot Password — Correct Security Answer**
- Input: ForgotPasswordDTO with correct email and security answer
- Expected: Password successfully reset
- Result: ✅ PASS

**TC_USER_05 — Forgot Password — Wrong Security Answer**
- Input: ForgotPasswordDTO with incorrect security answer
- Expected: Exception thrown, password not changed
- Result: ✅ PASS

**TC_USER_06 — Load User by Email (Spring Security)**
- Input: Valid email address
- Expected: UserDetails object returned with correct role
- Result: ✅ PASS

---

### ProductServiceImplTest

**TC_PROD_01 — Add New Product Successfully**
- Input: ProductRequestDTO with name, price, stock, category
- Expected: Product saved and ProductResponseDTO returned
- Result: ✅ PASS

**TC_PROD_02 — Get All Products**
- Input: None
- Expected: List of all products returned as ProductResponseDTOs
- Result: ✅ PASS

**TC_PROD_03 — Get Product By ID — Found**
- Input: Valid product ID
- Expected: ProductResponseDTO returned
- Result: ✅ PASS

**TC_PROD_04 — Get Product By ID — Not Found**
- Input: Non-existent product ID
- Expected: ResourceNotFoundException thrown
- Result: ✅ PASS

**TC_PROD_05 — Search Products by Keyword**
- Input: Search keyword "laptop"
- Expected: List of matching products returned
- Result: ✅ PASS

**TC_PROD_06 — Update Product Successfully**
- Input: Valid product ID + updated data
- Expected: Product fields updated and saved
- Result: ✅ PASS

**TC_PROD_07 — Delete Product**
- Input: Valid product ID
- Expected: Product deleted from DB
- Result: ✅ PASS

**TC_PROD_08 — Get Products By Seller**
- Input: Authenticated seller's user ID
- Expected: List of that seller's products returned
- Result: ✅ PASS

**TC_PROD_09 — Get Products By Category**
- Input: Category ID
- Expected: All products under that category returned
- Result: ✅ PASS

**TC_PROD_10 — Add Product with Low Stock Threshold**
- Input: Product with inventory threshold set
- Expected: Product saved with threshold value intact
- Result: ✅ PASS

---

### OrderServiceImplTest

**TC_ORD_01 — Place Order Successfully**
- Input: OrderRequestDTO with cart items, shipping details, payment method
- Expected: Order created with status PENDING, stock reduced, payment record created
- Result: ✅ PASS

**TC_ORD_02 — Place Order with Insufficient Stock**
- Input: Order quantity greater than available product stock
- Expected: InsufficientStockException thrown, order not placed
- Result: ✅ PASS

**TC_ORD_03 — Accept Order (Seller)**
- Input: Order ID with status PENDING
- Expected: Order status updated to ACCEPTED, notification sent to buyer
- Result: ✅ PASS

**TC_ORD_04 — Reject Order (Seller)**
- Input: Order ID with status PENDING
- Expected: Order status updated to REJECTED
- Result: ✅ PASS

**TC_ORD_05 — Mark Order as Delivered**
- Input: Order ID with status ACCEPTED
- Expected: Order status updated to DELIVERED
- Result: ✅ PASS

---

### CartServiceImplTest

**TC_CART_01 — Add New Item to Cart**
- Input: Product ID + quantity for authenticated buyer
- Expected: New CartItem created and linked to buyer's Cart
- Result: ✅ PASS

**TC_CART_02 — Add Existing Item (Increase Quantity)**
- Input: Product already in cart + additional quantity
- Expected: Existing CartItem quantity incremented
- Result: ✅ PASS

**TC_CART_03 — Remove Item from Cart**
- Input: CartItem ID
- Expected: CartItem deleted from DB
- Result: ✅ PASS

**TC_CART_04 — Get Cart for Authenticated Buyer**
- Input: Logged-in buyer's session
- Expected: CartResponseDTO with all items and total price
- Result: ✅ PASS

**TC_CART_05 — Update Item Quantity**
- Input: CartItem ID + new quantity
- Expected: CartItem quantity updated
- Result: ✅ PASS

**TC_CART_06 — Clear Cart After Order Placed**
- Input: Buyer's cart after order placement
- Expected: All CartItems removed
- Result: ✅ PASS

**TC_CART_07 — Get Cart — Empty Cart**
- Input: Buyer with no items in cart
- Expected: CartResponseDTO with empty list and zero total
- Result: ✅ PASS

**TC_CART_08 — Cart Total Calculation**
- Input: Multiple items with different quantities and prices
- Expected: Correct total amount calculated
- Result: ✅ PASS

**TC_CART_09 — Create Cart on Registration**
- Input: New user registration
- Expected: Empty cart automatically created for buyer
- Result: ✅ PASS

---

### ReviewServiceImplTest

**TC_REV_01 — Add Review Successfully**
- Input: ReviewRequestDTO with product ID, rating, comment
- Expected: Review saved and linked to buyer and product
- Result: ✅ PASS

**TC_REV_02 — Get Reviews by Product ID**
- Input: Valid product ID
- Expected: List of all reviews for that product returned
- Result: ✅ PASS

**TC_REV_03 — Add Review — Product Not Found**
- Input: Non-existent product ID
- Expected: ResourceNotFoundException thrown
- Result: ✅ PASS

**TC_REV_04 — Average Rating Calculation**
- Input: Product with multiple reviews of varying ratings
- Expected: Correct average rating calculated (rounded to 1 decimal)
- Result: ✅ PASS

**TC_REV_05 — Get All Reviews**
- Input: None
- Expected: Full list of all reviews across all products
- Result: ✅ PASS

**TC_REV_06 — Delete Review**
- Input: Review ID
- Expected: Review deleted from DB
- Result: ✅ PASS

**TC_REV_07 — Review with Zero Rating**
- Input: Rating value of 0
- Expected: Review saved with 0 stars
- Result: ✅ PASS

---

### CategoryServiceImplTest

**TC_CAT_01 — Create Category Successfully**
- Input: CategoryRequestDTO with category name
- Expected: Category saved and returned
- Result: ✅ PASS

**TC_CAT_02 — Get All Categories**
- Input: None
- Expected: List of all categories returned
- Result: ✅ PASS

**TC_CAT_03 — Delete Category**
- Input: Valid category ID
- Expected: Category deleted from DB
- Result: ✅ PASS

**TC_CAT_04 — Get Category By ID — Not Found**
- Input: Non-existent category ID
- Expected: ResourceNotFoundException thrown
- Result: ✅ PASS

---

### FavoriteServiceImplTest

**TC_FAV_01 — Add Product to Favorites**
- Input: Product ID + authenticated buyer
- Expected: Favorite record created
- Result: ✅ PASS

**TC_FAV_02 — Remove Product from Favorites**
- Input: Favorite ID
- Expected: Favorite record deleted
- Result: ✅ PASS

**TC_FAV_03 — Get All Favorites for Buyer**
- Input: Authenticated buyer
- Expected: List of all saved products returned
- Result: ✅ PASS

**TC_FAV_04 — Add Already Favorited Product**
- Input: Product already in buyer's favorites
- Expected: Duplicate not created
- Result: ✅ PASS

**TC_FAV_05 — Favorites — Product Not Found**
- Input: Non-existent product ID
- Expected: ResourceNotFoundException thrown
- Result: ✅ PASS

**TC_FAV_06 — Empty Favorites List**
- Input: Buyer with no saved products
- Expected: Empty list returned, no error
- Result: ✅ PASS

---

### NotificationServiceImplTest

**TC_NOTIF_01 — Create Notification**
- Input: User ID + message
- Expected: Notification record saved
- Result: ✅ PASS

**TC_NOTIF_02 — Get All Notifications for User**
- Input: Authenticated user
- Expected: List of all notifications for that user
- Result: ✅ PASS

**TC_NOTIF_03 — Mark Notification as Read**
- Input: Notification ID
- Expected: is_read flag set to true
- Result: ✅ PASS

**TC_NOTIF_04 — Get Unread Notifications Count**
- Input: Authenticated user
- Expected: Count of unread notifications returned
- Result: ✅ PASS

**TC_NOTIF_05 — Notification Created on Order Accept**
- Input: Order accepted by seller
- Expected: Notification automatically sent to buyer
- Result: ✅ PASS

**TC_NOTIF_06 — Notification Created on Order Reject**
- Input: Order rejected by seller
- Expected: Notification automatically sent to buyer
- Result: ✅ PASS

---

### SellerServiceImplTest

**TC_SELL_01 — Get Seller Dashboard Data**
- Input: Authenticated seller
- Expected: List of seller's products and orders returned
- Result: ✅ PASS

**TC_SELL_02 — Get Orders for Seller's Products**
- Input: Seller ID
- Expected: All incoming orders for that seller's products
- Result: ✅ PASS

---

## Integration Testing

### Modules Tested Together

- Authentication + User Management
- Product Module + Category Module
- Cart Module + Order Module
- Order Module + Notification Module
- Seller Dashboard + Product + Order Modules

---

## Manual UI Testing

### Pages Tested

| Page | URL | Result |
|---|---|---|
| Home / Product Listing | / | ✅ PASS |
| Login Page | /login | ✅ PASS |
| Register Page | /register | ✅ PASS |
| Forgot Password | /forgot-password | ✅ PASS |
| Product Detail | /product/{id} | ✅ PASS |
| Cart Page | /cart | ✅ PASS |
| Orders Page | /orders | ✅ PASS |
| Favorites Page | /favorites | ✅ PASS |
| Notifications Page | /notifications | ✅ PASS |
| Seller Dashboard | /seller/dashboard | ✅ PASS |
| 404 Error Page | /error/404 | ✅ PASS |
| 500 Error Page | /error/500 | ✅ PASS |

---

## Test Environment

- Operating System: Windows 10/11
- IDE: IntelliJ IDEA
- Database: Oracle Database XE
- Browser: Google Chrome
- Java Version: 17+
- Spring Boot Version: 3.x
- Build Tool: Maven
