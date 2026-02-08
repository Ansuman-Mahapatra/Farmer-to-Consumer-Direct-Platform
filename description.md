# Farmer-to-Consumer Direct Platform

## 📌 Project Overview
The **Farmer-to-Consumer Direct Platform** is a digital marketplace designed to bridge the gap between local farmers and consumers. By eliminating intermediaries, this platform empowers farmers to sell their produce directly to buyers, ensuring fair pricing, fresher products, and a sustainable food ecosystem.

## 🎯 Purpose & Goals
- **For Farmers:** Provide a direct channel to sell produce, control pricing, manage inventory, and build long-term relationships with customers.
- **For Consumers:** Offer access to fresh, locally sourced organic products, with transparent origin information and competitive prices.
- **Societal Impact:** foster community support, reduce carbon footprints by promoting local sourcing, and enhance food transparency.

## 🛠 Technology Stack

### Backend
- **Framework:** Spring Boot 3.5.7 (Java 21)
- **Database:** MongoDB (Local instance `f2c`)
- **Security:** Spring Security with JWT Authentication
- **Object Mapping:** Lombok
- **External Services:**
  - **Cloudinary:** For efficient image management and storage.
  - **JavaMail (SMTP):** For email notifications (e.g., order confirmations).
  - **Thymeleaf:** For server-side email templates.

### Frontend
- **Framework:** React 18
- **Routing:** React Router DOM
- **HTTP Client:** Axios (with interceptors for Auth)
- **Styling:** CSS3, React Icons
- **State Management:** React Context API (AuthContext, CartContext)

## 🚀 Key Features

### 🔐 Authentication & Roles
- Secure Registration and Login.
- Role-based access control: **Farmer** and **Consumer**.

### 👨‍🌾 Farmer Features
- **Dashboard:** Overview of sales and products.
- **Product Management:** Add, edit, and delete produce listings.
- **Inventory Control:** Real-time updates on stock availability.
- **Image Uploads:** Seamless integration with Cloudinary for product photos.

### 🛒 Consumer Features
- **Product Discovery:** Search, filter (Category, Price), and sort products.
- **Shopping Cart:** Manage items and quantities before purchase.
- **Order System:** Place orders and track their status.
- **Payments:** Integrated mock payment system for testing transactions.

## ⚙️ Configuration & Setup

### Prerequisites
- Java 21+
- Node.js & npm
- MongoDB (running locally on port `27017`)

### Backend Setup
1. Navigate to the root folder.
2. Update `src/main/resources/application.properties` with your credentials:
    - `cloudinary.cloud-name`, `api-key`, `api-secret`
    - `spring.mail.username`, `password`
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
   *Server runs on port `8082`.*

### Frontend Setup
1. Navigate to the `frontend` directory.
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm start
   ```
   *App runs on `http://localhost:3000`.*

## 📂 Project Structure
- **src/main/java:** Backend source code (Controllers, Services, Models).
- **src/main/resources:** Configuration files and templates.
- **frontend/:** React frontend application source.
