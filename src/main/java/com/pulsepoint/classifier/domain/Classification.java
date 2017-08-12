package com.pulsepoint.classifier.domain;

import com.google.common.base.MoreObjects;

import java.util.Objects;

/**
 * An DTO entity representing a valid classification.  Used for training classifiers
 *
 * @see Classifier#train(Classification)
 */
public class Classification {

    private String document;

    private String category;

    /**
     * Get document text for this classification
     *
     * @return document text
     */
    public String getDocument() {
        return document;
    }

    /**
     * Set document text for this classification
     *
     * @param document document text
     * @return {@code this}
     */
    public Classification setDocument(String document) {
        this.document = document;
        return this;
    }

    /**
     * Get the category for this classification
     *
     * @return category for this classification
     */
    public String getCategory() {
        return category;
    }

    /**
     * Set the category for this classification
     *
     * @param category category for this classification
     * @return {@code this}
     */
    public Classification setCategory(String category) {
        this.category = category;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Classification)) return false;
        Classification that = (Classification) o;
        return Objects.equals(document, that.document) &&
                Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(document, category);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("document", document)
                .add("category", category)
                .toString();
    }
}
