CREATE TABLE article_rating (
    article_id VARCHAR(255) PRIMARY KEY,
    average_rating DOUBLE PRECISION NOT NULL,
    total_reviews INTEGER NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
