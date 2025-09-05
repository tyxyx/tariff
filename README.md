# TARIFF  
**Trade Agreements Regulating Imports and Foreign Fees**

## 📌 Project Vision  
TARIFF is a system designed to manage and calculate **import tariffs and fees** based on a product's **country of origin/destination, brand, price, and category**.  
It is tailored for the **Technology Sector**, focusing on trade-related data management and tariff calculations.  

---

## 🌐 Target Industry & Scope  
- **Industry Focus**: Technology Sector  
- **Product Categories**:  
  - Semiconductors  
  - Laptops  
  - Smartphones  
  - Solid-State Drives (SSDs)  
  - Graphics Processing Units (GPUs)  

---

## 👥 User Roles  

### **Admin**  
- Manage tariff data (add, remove, modify tariff rates).  
- Access to full CRUD operations on tariffs.  

### **User**  
- Browse, search, and view tariff data.  
- Perform tariff calculations using the calculator tool.  
- Export tariff data and results for further analysis.  

---

## 📄 Key Pages (Baseline Requirements)  

1. **Signup/Login Page**  
   - Authentication system with **hashed password storage**.  
   - Support for multiple user roles (Admin, User).  

2. **Summary Tariff Table**  
   - Central dashboard displaying up-to-date tariff data.  

3. **Calculator Page**  
   - Interactive tool for tariff calculations.  

4. **Update Tariff Page (Admin Only)**  
   - Manage tariff rates (CRUD interface).  

5. **Profile Page**  
   - Manage user account details.  

---

## 🏗️ Technical Architecture  

- **Frontend**: ReactJS  
- **Backend**: Spring Boot (Java) with Swagger UI for API documentation  
- **Database**: PostgreSQL  
- **Deployment**:  
  - **Backend & Database**: AWS EC2  
  - **Frontend (static assets)**: AWS S3  

---

## 🚀 Advanced Features (Stretch Goals)  

- **Data Exportability**  
  - Export tariff data and calculation results in formats such as **CSV**.  
  - Enables further data analysis and storage for reference.  

- **Visualization Page**  
  - Interactive **geographical heat map** using **Leaflet.js** and **Heatmap.js**.  
  - Governments and organizations can use it to:  
    - Decide which countries to trade with.  
    - Predict trade opportunities and risks.  

---

## ✅ Summary  
TARIFF will provide a **centralized platform** for tariff data management, calculation, and visualization, making it useful for governments, businesses, and analysts in the **tech trade industry**.  

