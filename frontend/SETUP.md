# Frontend Setup Guide

## Quick Start

1. **Install Dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Start Backend Server**
   Make sure your Spring Boot backend is running on `http://localhost:8082`

3. **Start Frontend**
   ```bash
   npm start
   ```

4. **Access the Application**
   Open `http://localhost:3000` in your browser

## First Time Setup

1. **Register a User**
   - Click "Register" on the login page
   - Choose your role (Farmer or Consumer)
   - Fill in all required fields

2. **For Farmers**
   - After login, you'll see the Farmer Dashboard
   - Click "Add New Product" to start adding products
   - Upload product images (optional)
   - Manage inventory quantities

3. **For Consumers**
   - After login, you'll see the Consumer Dashboard
   - Browse products with filters and search
   - Add products to cart
   - Place orders and track them

## Known Issues & Notes

### Backend API Compatibility

The `updateProduct` endpoint in the backend uses `@RequestBody` for product data and `@RequestParam` for the image file. This combination can cause issues with multipart/form-data requests.

**If you encounter issues updating products with images**, you may need to update the backend `FarmerController.updateProduct` method to use `@ModelAttribute` instead of `@RequestBody`:

```java
@PutMapping("/products/{productId}")
public ResponseEntity<?> updateProduct(
        @PathVariable String productId,
        @ModelAttribute Product productDetails,  // Changed from @RequestBody
        @RequestParam(value = "image", required = false) MultipartFile imageFile) {
    // ... rest of the code
}
```

Alternatively, you can update products without images, which should work fine.

## Troubleshooting

### CORS Errors
- Ensure backend has `@CrossOrigin(origins = "*")` on controllers
- Check that backend is running on port 8082

### Authentication Issues
- Clear browser localStorage if you encounter token issues
- Check browser console for error messages

### API Connection
- Verify backend is running: `http://localhost:8082/api/test/mock-payment-connection`
- Check network tab in browser dev tools for API calls

## Development Tips

- Use browser dev tools to inspect API calls
- Check the Network tab to see request/response details
- Use React DevTools extension for component debugging
- Cart data is stored in localStorage

