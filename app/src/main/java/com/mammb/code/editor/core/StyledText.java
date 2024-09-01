package com.mammb.code.editor.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record StyledText(String text, double width, List<Style> styles) {


    static class Builder {
        private final Set<Integer> bounds = new HashSet<>();
        private final List<StyleSpan> spans = new ArrayList<>();

        void putAll(List<StyleSpan> spans) {
            spans.forEach(this::put);
        }

        void put(StyleSpan span) {
            bounds.add(span.offset);
            bounds.add(span.offset + span.length);
            spans.add(span);
        }

        List<StyledText> build() {
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

}
