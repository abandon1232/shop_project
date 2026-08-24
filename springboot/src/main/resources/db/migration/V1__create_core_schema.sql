CREATE TABLE IF NOT EXISTS admin (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(32) NULL,
    email VARCHAR(255) NULL,
    avatar VARCHAR(500) NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'ADMIN',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS business (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(32) NULL,
    email VARCHAR(255) NULL,
    avatar VARCHAR(500) NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'BUSINESS',
    description TEXT NULL,
    status VARCHAR(32) NOT NULL DEFAULT '审核中',
    PRIMARY KEY (id),
    UNIQUE KEY uk_business_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS user (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(32) NULL,
    email VARCHAR(255) NULL,
    avatar VARCHAR(500) NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS type (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    img VARCHAR(500) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_type_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS goods (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(160) NOT NULL,
    description TEXT NULL,
    img VARCHAR(500) NULL,
    price DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    unit VARCHAR(32) NULL,
    count INT NOT NULL DEFAULT 0,
    type_id INT NULL,
    business_id INT NULL,
    PRIMARY KEY (id),
    KEY idx_goods_name (name),
    KEY idx_goods_type_id (type_id),
    KEY idx_goods_business_id (business_id),
    CONSTRAINT fk_goods_type FOREIGN KEY (type_id) REFERENCES type (id) ON DELETE SET NULL,
    CONSTRAINT fk_goods_business FOREIGN KEY (business_id) REFERENCES business (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS notice (
    id INT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NULL,
    time VARCHAR(32) NOT NULL,
    user VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_notice_time (time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
