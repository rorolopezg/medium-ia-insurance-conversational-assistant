package com.superchat.model;

public record Product(
        String id,
        String name,
        String description,
        String coveragesText,
        String audienceText,
        int ageMin,
        int ageMax,
        String category
) {}
