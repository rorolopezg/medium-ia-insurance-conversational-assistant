CREATE TABLE IF NOT EXISTS medium.product_embeddings (
    embedding_id UUID PRIMARY KEY,
    embedding VECTOR(1536),
    text TEXT,
    metadata JSONB
);