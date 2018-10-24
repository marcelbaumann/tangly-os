package net.tangly.commons.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CommentsAndTagsTest {
    @Test
    void testModifyTags() {
        // Given
        var item = Entity.of();
        var tag = new Tag("namespace", "tag", "value");
        // When
        item.add(tag);
        // Then
        assertThat(item.getTags().size()).isEqualTo(1);
        assertThat(item.getTags().contains(tag)).isTrue();
        assertThat(item.findByNamespace("namespace").size()).isEqualTo(1);
        assertThat(item.findBy("namespace", "tag").isPresent()).isTrue();
        // When
        item.remove(tag);
        // Then
        assertThat(item.getTags().size()).isEqualTo(0);
        assertThat(item.findByNamespace("namespace").isEmpty()).isTrue();
        assertThat(item.findBy("namespace", "tag").isPresent()).isFalse();
    }

    @Test
    void testSerializeTags() {
        var item = Entity.of();
        item.add(Tag.of("namespace", "tag", "value"));
        item.add(Tag.of("gis", "longitude", "0.0"));
        item.add(Tag.of("gis", "latitude", "0.0"));
        String rawTags = item.getRawTags();
        Set<Tag> tags = item.getTags();
        item.clear();
        assertThat(item.getTags().isEmpty()).isTrue();
        item.addRawTags(rawTags);
        assertThat(item.getTags().isEmpty()).isFalse();
        assertThat(item.getTags().size()).isEqualTo(3);
        assertThat(item.findByNamespace("gis").size()).isEqualTo(2);
    }

    @Test
    void testModifyComments() {
        // Given
        var item = Entity.of();
        var comment = new Comment(LocalDateTime.now(), "John Doe", "This is a comment");
        // When
        item.add(comment);
        // Then
        assertThat(item.getComments().size()).isEqualTo(1);
        assertThat(item.getComments().contains(comment)).isTrue();
        // When
        item.remove(comment);
        assertThat(item.getComments().isEmpty()).isTrue();
    }

    @Test
    void testFilterComments() {
        // Given
        var item = Entity.of();
        var comment = new Comment(LocalDateTime.of(1800, Month.JANUARY, 1, 0, 0), "John Doe", "This is comment 1");
        comment.add(new Tag("gis", "longitude", "0.0"));
        comment.add(new Tag("gis", "latitude", "0.0"));
        item.add(comment);
        comment = new Comment(LocalDateTime.of(1900, Month.JANUARY, 1, 0, 0), "John Doe", "This is comment 2");
        comment.add(new Tag("gis", "longitude", "0.0"));
        item.add(comment);
        comment = new Comment(LocalDateTime.of(2000, Month.JANUARY, 1, 0, 0), "John Doe", "This is comment 3");
        comment.add(new Tag("gis", "latitude", "0.0"));
        item.add(comment);
        // When
        assertThat(item.findByAuthor("John Doe").size()).isEqualTo(3);
        assertThat(item.findByTime(LocalDateTime.MIN, LocalDateTime.MAX).size()).isEqualTo(3);
        assertThat(item.findByTime(LocalDateTime.of(1700, 1, 1, 0, 0), LocalDateTime.of(2100, 1, 1, 0, 0)).size()).isEqualTo(3);
        assertThat(item.findByTime(LocalDateTime.of(1799, 1, 1, 0, 0), LocalDateTime.of(2100, 1, 1, 0, 0)).size()).isEqualTo(3);
        assertThat(item.findByTime(LocalDateTime.of(1800, 1, 1, 0, 0), LocalDateTime.of(2000, 1, 1, 0, 0)).size()).isEqualTo(3);
        assertThat(item.findByTime(LocalDateTime.of(1900, 1, 1, 0, 0), LocalDateTime.of(2000, 1, 1, 0, 0)).size()).isEqualTo(2);
        assertThat(item.findByTime(LocalDateTime.of(1900, 1, 1, 0, 0), LocalDateTime.of(1990, 1, 1, 0, 0)).size()).isEqualTo(1);
        assertThat(item.findByTag("gis", "longitude").size()).isEqualTo(2);
        assertThat(item.findByTag("gis", "latitude").size()).isEqualTo(2);
        assertThat(item.findByTag("gis", "none").isEmpty()).isTrue();
    }
}