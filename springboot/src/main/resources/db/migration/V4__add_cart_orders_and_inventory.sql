ALTER TABLE goods
    ADD COLUMN stock INT NOT NULL DEFAULT 0 AFTER count;

UPDATE goods SET stock = 25 WHERE stock = 0;

CREATE TABLE cart_item (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    goods_id INT NOT NULL,
    quantity INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_user_goods (user_id, goods_id),
    KEY idx_cart_user_id (user_id),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_goods FOREIGN KEY (goods_id) REFERENCES goods (id) ON DELETE CASCADE,
    CONSTRAINT chk_cart_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE shop_order (
    id INT NOT NULL AUTO_INCREMENT,
    order_no VARCHAR(40) NOT NULL,
    user_id INT NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_user_created (user_id, created_at),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE RESTRICT,
    CONSTRAINT chk_order_total CHECK (total_amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE order_item (
    id INT NOT NULL AUTO_INCREMENT,
    order_id INT NOT NULL,
    goods_id INT NULL,
    product_name VARCHAR(160) NOT NULL,
    product_img VARCHAR(500) NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_order_item_order_id (order_id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES shop_order (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_goods FOREIGN KEY (goods_id) REFERENCES goods (id) ON DELETE SET NULL,
    CONSTRAINT chk_order_item_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
