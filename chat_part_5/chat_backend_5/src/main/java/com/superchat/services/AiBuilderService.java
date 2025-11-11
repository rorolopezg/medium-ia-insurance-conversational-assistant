package com.superchat.services;

import com.superchat.model.AiAgentsPerSession;
import com.superchat.interfaces.IChatAgentA;
import com.superchat.interfaces.IProfileExtractionAgent;
import com.superchat.model.AiModelsAndStorage;
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

@Service
public class AiBuilderService {

    @Value("${langchain4j.openai.chat-model.api-key}")
    private String openAiApiKey;

    // === Infraestructura compartida (singleton) ===
    public AiModelsAndStorage createSharedInfra() {
        AiModelsAndStorage res = new AiModelsAndStorage();

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(openAiApiKey)
                .modelName("text-embedding-3-small")
                .dimensions(1536)
                .build();

        EmbeddingStore<TextSegment> store = PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5432)
                .user("test_user")
                .password("12345")
                .database("medium_ia")
                .table("medium.product_embeddings")
                .dimension(1536)
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName("gpt-4o")
                .temperature(0.1)
                .build();

        res.setEmbeddingModel(embeddingModel);
        res.setEmbeddingStore(store);
        res.setChatModel(chatModel);
        return res;
    }

    // === Wiring por sesión (agentes con su propia memoria de chat) ===
    public AiAgentsPerSession buildPerSessionAgents(OpenAiChatModel chatModel) {
        AiAgentsPerSession perSession = new AiAgentsPerSession();

        IProfileExtractionAgent extractor = AiServices.builder(IProfileExtractionAgent.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(3))
                .build();

        IChatAgentA chatAgent = AiServices.builder(IChatAgentA.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(30))
                .tools(new InsuranceQuoteTool())
                .build();

        perSession.setProfileExtractionAgent(extractor);
        perSession.setChatAgentA(chatAgent);
        return perSession;
    }
}