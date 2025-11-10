package com.superchat.services;

import com.superchat.interfaces.IChatAgentA;
import com.superchat.interfaces.IProfileExtractionAgent;
import com.superchat.model.ProductRecommendationResult;
import com.superchat.tools.InsuranceQuoteTool;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public final class IABuilderService {
    @Value("${langchain4j.openai.chat-model.api-key}") //Injects a variable with the value of the property "langchain4j.openai.chat-model.api-key" from application.properties
    private String openAiApiKey;

    private EmbeddingStore<TextSegment> buildPersistentEmbeddingStore() {
        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5432)
                .user("test_user")
                .password("12345")
                .database("medium_ia")
                .table("medium.product_embeddings")
                .dimension(1536) // Important for text-embedding-3-small
                .build();
    }

    public ProductRecommendationResult createChatRecommenderAgent() {
        ProductRecommendationResult result = new ProductRecommendationResult();

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(openAiApiKey)
                .modelName("text-embedding-3-small")
                .dimensions(1536) // Important for text-embedding-3-small
                .logRequests(true)
                .logResponses(true)
                .build();

        EmbeddingStore<TextSegment> embeddingStore = buildPersistentEmbeddingStore();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName("gpt-4o") // Or "gpt-4-1106-preview", "gpt-3.5-turbo"
                .temperature(0.1)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        IProfileExtractionAgent profileExtractionAgent = AiServices.builder(IProfileExtractionAgent.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(3))
                .build();

        IChatAgentA mainChatAgent = AiServices.builder(IChatAgentA.class)
                .chatModel(chatModel)
                //.contentRetriever(contentRetriever) --> From now on we won't use this because we'll perform the semantic search manually
                .chatMemory(MessageWindowChatMemory.withMaxMessages(30))
                .tools(new InsuranceQuoteTool())
                .build();

        result.setChatAgentA(mainChatAgent);
        result.setEmbeddingModel(embeddingModel);
        result.setEmbeddingStore(embeddingStore);
        result.setProfileExtractionAgent(profileExtractionAgent);

        return result;
    }
}
