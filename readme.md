# My Fund - Financial Management Application

## Description

My Fund is a comprehensive financial management application designed to help users track and manage their finances efficiently. Built with Java 17 and Spring Boot 3 for the backend, and MySQL for data storage, this application offers a robust platform for personal finance management. Whether you're looking to monitor your spending, create budgets, or track investments, My Fund provides the tools you need to take control of your financial health.

## Table of Contents

- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Features](#features)

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


### Encryption Key for Database Security
My Fund uses AES-128 encryption to ensure the security of sensitive data stored in the database, such as user passwords and financial information.
This key must be a 16-character string, which corresponds to the 128-bit key required by the AES-128 encryption algorithm. Make sure the key is exactly 128 bits (16 characters) long to properly encrypt and decrypt sensitive data stored in the database.
This encryption ensures that even if someone gains unauthorized access to the database, the sensitive information will remain secure and unreadable without the correct key.

To enable this encryption, you must provide an encryption key in the application.properties file:
```
encryption.key=encyptionKey
```
