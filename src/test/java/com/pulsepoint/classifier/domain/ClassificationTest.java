package com.pulsepoint.classifier.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Classification}
 */
public class ClassificationTest {
    @Test
    public void assertEquality() {
        assertThat(new Classification()).isEqualTo(new Classification());
        assertThat(new Classification().setCategory("a")).isEqualTo(new Classification().setCategory("a"));
        assertThat(new Classification().setDocument("a")).isEqualTo(new Classification().setDocument("a"));
        assertThat(new Classification().setDocument("a").setCategory("b")).isEqualTo(new Classification().setDocument("a").setCategory("b"));

        assertThat(new Classification()).isNotEqualTo(null);

        assertThat(new Classification()).isNotEqualTo(new Classification().setCategory("a"));
        assertThat(new Classification()).isNotEqualTo(new Classification().setDocument("a"));
        assertThat(new Classification()).isNotEqualTo(new Classification().setCategory("a").setDocument("a"));

        assertThat(new Classification().setCategory("b")).isNotEqualTo(new Classification().setCategory("a"));
        assertThat(new Classification().setCategory("b")).isNotEqualTo(new Classification().setDocument("a"));
        assertThat(new Classification().setCategory("b")).isNotEqualTo(new Classification().setCategory("a").setDocument("a"));
    }

    @Test
    public void assertToString() {
        assertThat(new Classification().toString()).isNotEmpty();
        assertThat(new Classification().setCategory("example").toString()).contains("example");
    }
}
