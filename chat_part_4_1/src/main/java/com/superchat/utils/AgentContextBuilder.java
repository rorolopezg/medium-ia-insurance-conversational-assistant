package com.superchat.utils;

import com.superchat.model.Product;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class AgentContextBuilder {

    private AgentContextBuilder(){}

    public static String buildContextForAgent(List<Product> allProducts, List<String> candidateIds) {
        Map<String, Product> byId = allProducts.stream()
                .collect(Collectors.toMap(Product::id, p -> p));

        StringBuilder sb = new StringBuilder();
        for (String id : candidateIds) {
            Product p = byId.get(id);
            if (p == null) continue;

            sb.append("CONTEXT_PRODUCTS_COUNT: ").append(candidateIds.size()).append("\n---\n");
            sb.append("""
                      Product ID: %s
                      Product Name: %s
                      Product Description: %s
                      Coverages:
                      %s
                      Target Audience: %s
                      ---
                      """.formatted(
                    p.id(), p.name(), p.description().trim(),
                    p.coveragesText().trim(), p.audienceText().trim()
            ));
        }
        return sb.toString();
    }
}
