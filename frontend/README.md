# Farmer to Consumer Platform - Frontend

A modern React frontend for the Farmer-to-Consumer Direct Platform.

## Features

- **Authentication**: User registration and login with JWT tokens
- **Farmer Dashboard**: 
  - Add, edit, and manage products
  - Update inventory
  - Upload product images
- **Consumer Dashboard**:
  - Browse products with filters and search
  - Shopping cart functionality
  - Place orders
  - Track order status
  - Mock payment integration

## Prerequisites

- Node.js (v14 or higher)
- npm or yarn
- Backend server running on `http://localhost:8082`

## Installation

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

## Running the Application

Start the development server:
```bash
npm start
```

The application will open at `http://localhost:3000`

## Project Structure

```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   ├── Auth/
│   │   │   ├── Login.js
│   │   │   ├── Register.js
│   │   │   └── Auth.css
│   │   ├── Consumer/
│   │   │   ├── ConsumerDashboard.js
│   │   │   ├── ProductList.js
│   │   │   ├── Cart.js
│   │   │   ├── Orders.js
│   │   │   ├── OrderDetails.js
│   │   │   └── *.css
│   │   ├── Farmer/
│   │   │   ├── FarmerDashboard.js
│   │   │   ├── ProductList.js
│   │   │   ├── AddProductForm.js
│   │   │   ├── EditProductForm.js
│   │   │   └── *.css
│   │   └── Shared/
│   │       ├── Navbar.js
│   │       └── Navbar.css
│   ├── context/
│   │   ├── AuthContext.js
│   │   └── CartContext.js
│   ├── services/
│   │   └── api.js
│   ├── App.js
│   ├── App.css
│   ├── index.js
│   └── index.css
├── package.json
└── README.md
```

## API Integration

The frontend connects to the backend API at `http://localhost:8082/api`. All API calls are handled through the `api.js` service file.

### Key API Endpoints Used:

- **Authentication**: `/api/auth/login`, `/api/auth/register`
- **Farmer**: `/api/farmer/products/*`
- **Consumer**: `/api/consumer/products/*`, `/api/consumer/orders/*`

## User Roles

- **FARMER**: Can manage products and inventory
- **CONSUMER**: Can browse products, place orders, and track deliveries
- **ADMIN**: Has access to all features

## Mock Payment System

The platform uses a mock payment system for testing. When placing an order:
- Use any payment ID starting with `mock_pay_` for testing
- The system will accept any signature value
- No real payments are processed

## Building for Production

To create a production build:

```bash
npm run build
```

The build folder will contain the optimized production build.

## Environment Variables

You can create a `.env` file in the frontend directory to customize:

```
REACT_APP_API_URL=http://localhost:8082/api
```

Then update `src/services/api.js` to use `process.env.REACT_APP_API_URL` instead of the hardcoded URL.

## Troubleshooting

1. **CORS Errors**: Ensure the backend has CORS enabled for `http://localhost:3000`
2. **API Connection Issues**: Verify the backend is running on port 8082
3. **Authentication Issues**: Check that JWT tokens are being stored in localStorage

## Technologies Used

- React 18
- React Router DOM
- Axios
- React Icons
- CSS3

