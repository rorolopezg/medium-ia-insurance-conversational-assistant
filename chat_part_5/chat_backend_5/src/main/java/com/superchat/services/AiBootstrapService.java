package com.superchat.services;

import com.superchat.model.Product;
import com.superchat.model.AiModelsAndStorage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiBootstrapService {

    private final AiBuilderService aiBuilderService;
    private final ProductService productService;
    private final ProductIngestionService ingestionService;

    // Infra compartida
    @Getter private EmbeddingModel embeddingModel;
    @Getter private EmbeddingStore<TextSegment> embeddingStore;
    @Getter private dev.langchain4j.model.openai.OpenAiChatModel chatModel;

    private final boolean firstRun = false; // cámbialo si requieres ingesta inicial

    @PostConstruct
    public void init() {
        log.info("Bootstrapping shared AI components...");
        AiModelsAndStorage shared = aiBuilderService.createSharedInfra();
        this.embeddingModel = shared.getEmbeddingModel();
        this.embeddingStore = shared.getEmbeddingStore();
        this.chatModel      = shared.getChatModel();

        if (firstRun) {
            List<Product> products = productService.findAllProducts();
            ingestionService.ingestAll(products, embeddingStore, embeddingModel);
            log.info("Initial embeddings ingestion completed.");
        }
    }
}