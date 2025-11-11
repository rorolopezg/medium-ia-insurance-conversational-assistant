package com.superchat.services;

import com.superchat.interfaces.IChatAgentA;
import com.superchat.interfaces.IProfileExtractionAgent;
import com.superchat.model.AiAgentsPerSession;
import com.superchat.model.AiModelsAndStorage;
import com.superchat.model.Product;
import com.superchat.tools.InsuranceQuoteTool;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AiBuilderService {

    // === Dependencias de dominio (para ingesta inicial opcional) ===
    private final ProductService productService;
    private final ProductIngestionService ingestionService;

    // === Configuración OpenAI ===
    @Value("${langchain4j.openai.chat-model.api-key}")
    private String openAiApiKey;

    @Value("${langchain4j.openai.chat-model.name:gpt-4o}")
    private String chatModelName;

    @Value("${langchain4j.openai.chat-model.temperature:0.1}")
    private double chatTemperature;

    // === Configuración de embeddings ===
    @Value("${langchain4j.openai.embedding-model.name:text-embedding-3-small}")
    private String embeddingModelName;

    @Value("${app.pgvector.dimension:1536}")
    private int embeddingDimension;

    // === Configuración PGVector ===
    @Value("${app.pgvector.host:localhost}")
    private String pgHost;

    @Value("${app.pgvector.port:5432}")
    private int pgPort;

    @Value("${app.pgvector.user:test_user}")
    private String pgUser;

    @Value("${app.pgvector.password:12345}")
    private String pgPassword;

    @Value("${app.pgvector.database:medium_ia}")
    private String pgDatabase;

    @Value("${app.pgvector.table:medium.product_embeddings}")
    private String pgTable;

    // === Bootstrap: ejecutar ingesta inicial (opcional) ===
    @Value("${app.ai.bootstrap.first-run:false}")
    private boolean firstRun;

    // === Infraestructura compartida expuesta como singleton ===
    @Getter private EmbeddingModel embeddingModel;
    @Getter private EmbeddingStore<TextSegment> embeddingStore;
    @Getter private OpenAiChatModel chatModel;

    // === Bootstrap al iniciar el contexto Spring ===
    @PostConstruct
    public void init() {
        log.info("Bootstrapping shared AI components...");
        AiModelsAndStorage shared = createSharedInfra();

        this.embeddingModel = shared.getEmbeddingModel();
        this.embeddingStore = shared.getEmbeddingStore();
        this.chatModel      = shared.getChatModel();

        if (firstRun) {
            log.info("First-run ingestion enabled. Ingesting products to embeddings store...");
            List<Product> products = productService.findAllProducts();
            ingestionService.ingestAll(products, embeddingStore, embeddingModel);
            log.info("Initial embeddings ingestion completed.");
        } else {
            log.info("First-run ingestion disabled. Skipping initial ingestion.");
        }
    }

    // === Infraestructura compartida (creación de modelos y storage) ===
    public AiModelsAndStorage createSharedInfra() {
        AiModelsAndStorage res = new AiModelsAndStorage();

        EmbeddingModel embModel = OpenAiEmbeddingModel.builder()
                .apiKey(openAiApiKey)
                .modelName(embeddingModelName)
                .dimensions(embeddingDimension)
                .build();

        EmbeddingStore<TextSegment> store = PgVectorEmbeddingStore.builder()
                .host(pgHost)
                .port(pgPort)
                .user(pgUser)
                .password(pgPassword)
                .database(pgDatabase)
                .table(pgTable)
                .dimension(embeddingDimension)
                .build();

        OpenAiChatModel chat = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(chatModelName)
                .temperature(chatTemperature)
                .build();

        res.setEmbeddingModel(embModel);
        res.setEmbeddingStore(store);
        res.setChatModel(chat);
        return res;
    }

    // === Wiring por sesión (agentes con memoria propia) ===
    public AiAgentsPerSession buildPerSessionAgents(OpenAiChatModel model) {
        AiAgentsPerSession perSession = new AiAgentsPerSession();

        IProfileExtractionAgent extractor = AiServices.builder(IProfileExtractionAgent.class)
                .chatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(3))
                .build();

        IChatAgentA chatAgent = AiServices.builder(IChatAgentA.class)
                .chatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(30))
                .tools(new InsuranceQuoteTool())
                .build();

        perSession.setProfileExtractionAgent(extractor);
        perSession.setChatAgentA(chatAgent);
        return perSession;
    }

    // (Convenience) Overload por si quieres usar siempre el chatModel compartido
    public AiAgentsPerSession buildPerSessionAgents() {
        return buildPerSessionAgents(this.chatModel);
    }
}
