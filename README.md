# **InWork App: Role-Based Access Control (RBAC) Implementation**

## **Overview**

This project is a demonstration of **Authentication**, **Authorization**, and **Role-Based Access Control (RBAC)** implemented in the **InWork App**. The system supports two user roles: **Admin** and **Client**, each with specific permissions and capabilities, ensuring secure and role-based access to resources.

This implementation aligns with the requirements for VRV Security's Backend Developer Intern assignment and demonstrates the following core features:

1. Secure user authentication using industry best practices.
2. Role-based authorization ensuring restricted access based on roles.
3. Comprehensive RBAC integration to manage user roles and permissions.

---

## **Features**

### **Authentication**
- Users can securely register, log in, and log out.
- Passwords are hashed using **bcrypt** for security.
- Sessions are managed using **JSON Web Tokens (JWT)** to ensure stateless and secure authentication.

### **Authorization**
- Access to specific endpoints is restricted based on user roles.
- **Admins** have privileges to:
  - Create client accounts.
  - Generate and manage client credentials.
- **Clients** have access to features specific to their role.

### **Role-Based Access Control (RBAC)**
- Permissions are dynamically assigned based on roles.
- Middleware ensures only authorized users can access role-specific resources.

---

## **System Architecture**

1. **Backend Framework**: Django
2. **Database**: AWS
3. **Authentication**: JWT-based token system.
4. **Authorization**: Middleware for role-specific access control.

---

## **Endpoints**

### **Admin Endpoints**
1. **Create Client**:  
   `POST /api/admin/create-client`  
   Allows admins to create new client accounts and generate credentials.

2. **Manage Clients**:  
   `GET /api/admin/clients`  
   View and manage all client accounts.

### **Client Endpoints**
1. **Access Client Dashboard**:  
   `GET /api/client/dashboard`  
   Provides access to client-specific features and data.

2. **Profile Management**:  
   `PUT /api/client/update-profile`  
   Enables clients to update their profile information.

### **Common Endpoints**
1. **Login**:  
   `POST /api/auth/login`  
   Authenticate users and generate a JWT.

2. **Logout**:  
   `POST /api/auth/logout`  
   Invalidates the session.

---

## **Technologies Used**

- **Backend**: AWS
- **Database**: MySql
- **Authentication**: JWT for token-based sessions
- **Password Hashing**: bcrypt for secure password storage

---

## **Security Best Practices**

- Passwords are hashed using bcrypt to prevent plaintext storage.
- JWTs are signed with a secure secret key and have expiration times to minimize risks.
- Middleware ensures that unauthorized access attempts are logged and denied.

---

