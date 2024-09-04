package com.mammb.code.editor.core.text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface StyledText {

    List<Style> styles();

    static Builder of() {
        return new Builder();
    }

    class Builder {
        private final Set<Integer> bounds = new HashSet<>();
        private final List<StyleSpan> spans = new ArrayList<>();

        public void putAll(List<StyleSpan> spans) {
            spans.forEach(this::put);
        }

        public void put(StyleSpan span) {
            bounds.add(span.offset);
            bounds.add(span.offset + span.length);
            spans.add(span);
        }

        public void put(Style style, int offset, int length) {
            put(new StyleSpan(style, offset, length));
        }

        public List<StyledText> build() {
            return null;
        }

        private List<Style> styles(int index) {
            return spans.stream()
                    .filter(span -> span.offset() <= index && index < (span.offset() + span.length()))
                    .map(StyleSpan::style)
                    .toList();
        }
    }

    record StyleSpan(Style style, int offset, int length) { }

    record StyledTextRecord(String text, double width, List<Style> styles) { };

}
