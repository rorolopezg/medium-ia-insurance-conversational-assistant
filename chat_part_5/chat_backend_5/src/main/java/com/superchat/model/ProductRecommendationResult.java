package com.superchat.model;

import com.superchat.interfaces.IChatAgentA;
import com.superchat.interfaces.IProfileExtractionAgent;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Clase utilitaria que agrupa los objetos de langchain4j necesarios para la recomendación de productos:
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductRecommendationResult {
    private IChatAgentA chatAgentA;
    private IProfileExtractionAgent profileExtractionAgent;
    private EmbeddingModel embeddingModel;
    private EmbeddingStore<TextSegment> embeddingStore;
}
