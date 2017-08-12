package com.pulsepoint.classifier.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic correctness tests for {@link Classifier} instances
 */
public abstract class ClassifierTestBase<T extends Classifier> {
    abstract T getClassifier();

    @Test
    public void testClassificationAPI() {
        /* verify supplying bad values does not result in an immediate exception */
        assertThat(getClassifier().classify(null)).isNotNull();
    }

    @Test
    public void testTrainingAPI() {
        /* verify supplying bad values does not result in an immediate exception */
        assertThat(getClassifier().train(null)).isNotNull();
    }
}
