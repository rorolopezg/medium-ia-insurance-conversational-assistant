-- See Part 3: https://medium.com/@rodrigo.lopez.gatica/build-an-ai-chatbot-with-java-part-3-using-a-persistent-embedding-store-with-postgresql-17-329c03319277

CREATE TABLE IF NOT EXISTS medium.product_embeddings (
    embedding_id UUID PRIMARY KEY,
    embedding VECTOR(1536),
    text TEXT,
    metadata JSONB
);

CREATE INDEX IF NOT EXISTS idx_product_embeddings_cosine
    ON medium.product_embeddings
    USING hnsw (embedding vector_cosine_ops)
;