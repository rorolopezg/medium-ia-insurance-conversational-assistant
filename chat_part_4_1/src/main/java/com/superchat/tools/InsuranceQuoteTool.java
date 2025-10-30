package com.superchat.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class InsuranceQuoteTool {
    @Tool("Calculate the price (premium) of a specific insurance product")
    public String generateQuote(@P("Product ID") String productId,
                                @P("Client's Name") String name,
                                @P("Client's Age") Integer age,
                                @P("CLient's marital status") String maritalStatus){

        log.info("Calculate the insurance premium: " + name);
        int safeAge = (age==null || age<0) ? 0 : age;

        BigDecimal premium = BigDecimal.valueOf( 20.0 + (safeAge * 0.05) );
        log.info("Estimated insurance price for " + name + " is " + premium + " USD.");
        return
            """
            {"productId":"%s","name":"%s","age":%d,"premiumUsd":%s}
            """
            .formatted(productId, name, safeAge, premium.toPlainString());
    }
}
