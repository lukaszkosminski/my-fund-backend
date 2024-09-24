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

## Usage

After installation and configuration, you can use My Fund to:
- Add and track your daily expenses and incomes.
- Create and manage budgets for different categories.
- View detailed reports and insights about your financial activities.
- Set financial goals and track your progress towards achieving them.

## Features

- **Dashboard**: Get an overview of your financial status at a glance.
- **Expense Tracking**: Log and categorize your daily expenses.
- **Income Tracking**: Keep track of your income sources.
- **Budgeting**: Set monthly budgets for different categories and monitor your spending.
- **Reports**: Generate detailed reports and insights to understand your financial habits.
- **Goals**: Set and track your financial goals.
