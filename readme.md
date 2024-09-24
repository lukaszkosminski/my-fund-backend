# My Fund - Financial Management Application

## Description

My Fund is a comprehensive financial management application designed to help users track and manage their finances efficiently. Built with Java 17 and Spring Boot 3 for the backend, and MySQL for data storage, this application offers a robust platform for personal finance management. Whether you're looking to monitor your spending, create budgets, or track investments, My Fund provides the tools you need to take control of your financial health.

## Table of Contents

- [Installation](#installation)
- [Configuration](#configuration)
- [Encryption Key for Database Security](#encryption-key-for-database-security)
- [Caching and Security](#caching-and-security)
- [Testing and Documentation](#testing-and-documentation)
- [CI/CD and Build Process](#ci-cd-and-build-process)
- [Features](#features)
- [Usage](#usage)

## Installation

### Prerequisites

Before you begin, ensure you have the following installed:
- Java 17
- MySQL
- Docker

### Backend Setup

1. Clone the repository to your local machine.
2. Navigate to the backend directory: `cd my-fund-backend`
3. Build the project using Maven: `mvn clean install`
4. Run the Spring Boot application: `java -jar target/myfund.jar`

## Configuration

### Database Configuration

1. Create a new MySQL database using docker-compose.yml.
2. Update the `application.properties` file in the backend project with your MySQL username and password:
   ```
   spring.datasource.url=jdbc:mysql://localhost:3306/fund-db
   spring.datasource.username=yourUsername
   spring.datasource.password=yourPassword
   ```
   
### Email Service Configuration

These settings are required to enable email notifications, including functionalities such as password recovery.

1. Register an account on Postmark.
2. After registration, obtain your Postmark API Key.
3. Add the following configurations to your application.properties file:
  ```
   email.sender=emailSender
   encryption.key=ecnryptionKey
   postmark.apikey=apiKey
   change.password.url=changePasswordUrl
  ```
### Encryption Key for Database Security
My Fund uses AES-128 encryption to ensure the security of sensitive data stored in the database, such as user passwords and financial information.
This key must be a 16-character string, which corresponds to the 128-bit key required by the AES-128 encryption algorithm. Make sure the key is exactly 128 bits (16 characters) long to properly encrypt and decrypt sensitive data stored in the database.
This encryption ensures that even if someone gains unauthorized access to the database, the sensitive information will remain secure and unreadable without the correct key.

To enable this encryption, you must provide an encryption key in the application.properties file:
```
encryption.key=encyptionKey
```
### Caching and Security
- Token Caching: Password reset tokens are stored in the cache for a limited duration, ensuring secure and temporary access.
- Email Throttling: To prevent abuse, the application limits the number of emails sent to 3 per minute using the emailThrottleCache. This helps manage the rate at which password reset and notification emails are sent.
- Spring Security Integration: The application is secured using Spring Security, which requires users to log in to access protected resources. This ensures only authorized users can interact with sensitive endpoints.

### Testing and Documentation
- Swagger Documentation: The application includes integrated Swagger documentation that lists all available API endpoints, providing a clear overview for developers and testers to interact with the service.
- Unit and E2E Testing: The application includes both unit tests and end-to-end (E2E) tests to ensure full coverage of functionality and user flows. The E2E tests are configured to run with a separate application-test.properties file, ensuring a test-safe environment.

### CI/CD and Build Process
- TeamCity Integration: The application build process is automated using TeamCity, which assigns a unique build number to each version. This ensures consistent and traceable builds during development and deployment.
- Application Details Service: A custom service, ApplicationDetailsService, is used to manage and retrieve important application metadata, ensuring the build process and versioning is handled smoothly.
- Docker for Local Development: A docker-compose.yml file is provided in the repository to set up a local MySQL database, making it easy for developers to run the application locally with a consistent environment.
