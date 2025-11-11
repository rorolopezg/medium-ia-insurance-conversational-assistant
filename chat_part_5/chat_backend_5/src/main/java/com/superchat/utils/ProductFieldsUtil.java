package com.superchat.utils;

// Constants related to product fields. Used during the ingestion process, specifically when mapping
// the product metadata.
public final class ProductFieldsUtil {
    public static final String META_PRODUCT_ID = "productId";
    public static final String META_PRODUCT_NAME = "name";
    public static final String META_AGE_MIN = "ageMin";
    public static final String META_AGE_MAX = "ageMax";
    public static final String META_CATEGORY = "category";
    public static final String META_SEGMENT_TYPE = "segmentType";
    public static final String SEG_AUDIENCE = "audience";
    public static final String SEG_DETAILS = "details";

    private ProductFieldsUtil() {}
}
