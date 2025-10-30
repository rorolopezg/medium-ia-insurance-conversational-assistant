package com.superchat;

import com.superchat.services.ProductIngestionService;
import com.superchat.services.ProductService;
import com.superchat.interfaces.IChatAgentA;
import com.superchat.interfaces.IProfileExtractionAgent;
import com.superchat.model.ClientProfile;
import com.superchat.model.Product;
import com.superchat.model.ProductRecommendationResult;
import com.superchat.services.AudienceSearcherService;
import com.superchat.services.IABuilderService;
import com.superchat.utils.AgentContextBuilder;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
@Profile("langchain-example-1") // Run only if this profile is active
@Slf4j
public class LangChainExampleRunner implements CommandLineRunner {
    private final Boolean firstRun = false; // Flag to control data ingestion
    private final IABuilderService iaBuilderService;
    private final AudienceSearcherService audienceSearcher;
    private final ProductIngestionService productIngestionService;
    private final ProductService productService;

    // Step 0 - Constructor-based dependency injection
    public LangChainExampleRunner(IABuilderService iaBuilderService,
                                  AudienceSearcherService audienceSearcher,
                                  ProductIngestionService productIngestionService,
                                  ProductService productService) {
        this.iaBuilderService = iaBuilderService;
        this.audienceSearcher = audienceSearcher;
        this.productIngestionService = productIngestionService;
        this.productService = productService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("LangChain4j Example Runner started...");
        ClientProfile clientProfile = new ClientProfile();
        String jsonDataForProfileExtractionAgent = null;
        List<String> candidateIds = null;
        String contextForAgent = "";

        // Step 1 - Setup: Build the models and the store:
        final ProductRecommendationResult productRecommendationResult = iaBuilderService.createChatRecommenderAgent();

        final EmbeddingModel embeddingModel = productRecommendationResult.getEmbeddingModel();
        final EmbeddingStore<TextSegment> embeddingStore = productRecommendationResult.getEmbeddingStore();
        final IProfileExtractionAgent profileExtractionAgent = productRecommendationResult.getProfileExtractionAgent();
        final IChatAgentA chatAgentA = productRecommendationResult.getChatAgentA();

        // Step 2 - Manage Products
        // 2.1. Get the insurance products:
        List<Product> products = productService.findAllProducts();

        // 2.2. If first run, ingest products into the embedding store:
        if (firstRun)
            // Ingest only on first run
            productIngestionService.ingestAll(products, embeddingStore, embeddingModel);

        // Step 3 - Start interaction loop:
        System.out.println("Type your question here:");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("You: ");
            String userQueryLine = scanner.nextLine();
            if ("exit".equalsIgnoreCase(userQueryLine) || "quit".equalsIgnoreCase(userQueryLine) || "bye".equalsIgnoreCase(userQueryLine)) {
                log.info("LangChain4j Example Runner finished.");
                break;
            }

            if (userQueryLine == null || userQueryLine.trim().isEmpty()) {
                continue; // Skip empty lines
            }

            // Step 4 - Get client profile. When a message arrives from the user, try yo extract profile info using
            //          the profile extraction agent:
            jsonDataForProfileExtractionAgent = profileExtractionAgent.extractData(userQueryLine);
            jsonDataForProfileExtractionAgent = jsonDataForProfileExtractionAgent.replaceAll("```json", "").replaceAll("```", "");
            clientProfile.applyJson(jsonDataForProfileExtractionAgent);

            log.info("Extracted client profile: {}", clientProfile.toString());
            log.info("Friendly description: {}", clientProfile.friendlyProfileDescription());

            // Step 5 - Find candidate products. Use AudienceSearcher to find candidate products based on the extracted
            //          profile:
            candidateIds = audienceSearcher.findCandidateProductIds(
                    clientProfile,
                    embeddingModel,
                    embeddingStore,
                    7, // maxResults
                    0.78 // minScore
            );

            // Step 6 - Build context for main agent.
            //    The context will include only the candidate products... Not ALL products!:
            //    This improves performance and reduces costs.
            contextForAgent = AgentContextBuilder.buildContextForAgent(products, candidateIds);

            // Step 7 - Response to customer. Call main chat agent with context and user message, to get and appropriate
            //          response/offer for the customer:
            String respuesta = chatAgentA.chat(userQueryLine, contextForAgent);
            System.out.printf("Agent response: %s%n", respuesta);
        }
    }
}