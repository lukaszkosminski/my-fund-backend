CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE budget (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    local_date_time VARCHAR(255),
    balance VARCHAR(255),
    total_income VARCHAR(255),
    total_expense VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE expense (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    amount VARCHAR(255) NOT NULL,
    local_date_time VARCHAR(255),
    budget_id BIGINT,
    category_id BIGINT,
    sub_category_id BIGINT,
    user_id BIGINT,
    FOREIGN KEY (budget_id) REFERENCES budget(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE income (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    amount VARCHAR(255) NOT NULL,
    local_date_time VARCHAR(255),
    budget_id BIGINT,
    category_id BIGINT,
    sub_category_id BIGINT,
    user_id BIGINT,
    FOREIGN KEY (budget_id) REFERENCES budget(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE subcategory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    category_id BIGINT,
    FOREIGN KEY (category_id) REFERENCES category(id)
);