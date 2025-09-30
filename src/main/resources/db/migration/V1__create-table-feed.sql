CREATE TABLE feed (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    article_id VARCHAR(50) NOT NULL,
    order_id VARCHAR(50) NOT NULL,
    comment VARCHAR(300) NOT NULL,
    rating int NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
