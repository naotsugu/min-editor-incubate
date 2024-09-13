package com.mammb.code.editor.core.syntax;

import com.mammb.code.editor.core.text.Style.StyleSpan;
import java.util.List;

public interface Syntax {
    String name();
    List<StyleSpan> apply(int row, String text);
    static Syntax of(String name) {
        return switch (name.toLowerCase()) {
            case "java" -> new JavaSyntax();
            case "md" -> new MarkdownSyntax();
            default -> new PassThrough(name);
        };
    }
    record PassThrough(String name) implements Syntax {
        @Override public List<StyleSpan> apply(int row, String text) {
            return List.of();
        }
    }
}
