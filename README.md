# Guts IAM — Identity and Access Management Backend

# Overview

Guts IAM is a backend-focused Identity and Access Management system built using Spring Boot.
It provides a secure and extensible foundation for authentication, authorization, and account lifecycle management using JWT, refresh tokens, OTP-based recovery mechanisms, and audit logging.

# Features
Token-based authentication using JWT
Refresh token lifecycle management
OTP-based account unlock and password reset workflows
Role-based access control
Centralized audit logging of system activities
Scheduled cleanup of expired tokens and OTPs
Modular and maintainable architecture

# Architecture
auth        → authentication and account workflows  
security    → Spring Security configuration, JWT filters, and utilities  
token       → refresh token handling and lifecycle management  
auditlog    → tracking and persistence of system events  
user        → user and role management  
common      → shared utilities, exception handling, and API responses 

# Tech Stack
Java
Spring Boot
Spring Security
JWT
Maven
MySQL

# Setup and Run
Clone the repository
git clone https://github.com/Sanjay-Varadharajan/Guts_IAM.git
cd Guts_IAM
Configuration

Update the application configuration in:
src/main/resources/application.properties

Example:
spring.datasource.url=jdbc:mysql://localhost:3306/{YOUR_DB}
spring.datasource.username={USER_NAME}
spring.datasource.password={PASSWORD}
Run the application
mvn clean install
mvn spring-boot:run

# Notes
This project is intentionally backend-focused and does not include a user interface.
It is designed to be integrated with external clients or frontend applications.

# Purpose
This project demonstrates:
Secure authentication and authorization design
Practical application of Spring Security with JWT
Clean and modular backend architecture suitable for scalable systems
To fill up the security gaps in small-medium Scale Applications and Project

# License
MIT License

# Author
Sanjay Varadharajan
