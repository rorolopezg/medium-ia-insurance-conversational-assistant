package com.superchat.services;

import com.superchat.model.Product;
import com.superchat.model.ProductRecommendationResult;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiBootstrap {

    private final IABuilderService iaBuilderService;
    private final ProductService productService;
    private final ProductIngestionService ingestionService;

    @Getter
    private ProductRecommendationResult wiring;

    private final boolean firstRun = false; // cámbialo si requieres ingesta inicial

    @PostConstruct
    public void init() {
        log.info("Bootstrapping AI components...");
        wiring = iaBuilderService.createChatRecommenderAgent();

        if (firstRun) {
            List<Product> products = productService.findAllProducts();
            EmbeddingModel model = wiring.getEmbeddingModel();
            EmbeddingStore<TextSegment> store = wiring.getEmbeddingStore();
            ingestionService.ingestAll(products, store, model);
            log.info("Initial embeddings ingestion completed.");
        }
    }
}
