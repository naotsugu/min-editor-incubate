package code.editor.syntax;

import code.editor.Style.*;
import com.mammb.code.piecetable.TextEdit;
import java.util.List;

public interface Syntax {

    String name();
    List<StyleSpan> apply(int row, String text);

    record PassThrough(String name) implements Syntax {
        @Override public List<StyleSpan> apply(int row, String text) {
            return List.of();
        }
    }

    static Syntax of(String name) {
        return switch (name.toLowerCase()) {
            case "java" -> new JavaSyntax();
            case "md" -> new MarkdownSyntax();
            default -> new PassThrough(name);
        };
    }

    record Anchor(int row, int col) implements Comparable<Anchor> {
        @Override
        public int compareTo(Anchor that) {
            int c = Integer.compare(this.row, that.row);
            return c == 0 ? Integer.compare(this.col, that.col) : c;
        }
    }
    enum Palette {
        DEEP_GREEN("#6A8759"),
        ORANGE("#CC7832"),
        GRAY("#808080"),
        ;

        Palette(String colorString) {
            this.colorString = colorString;
        }

        final String colorString;
    }

    static int read(Trie keywords, String colorString, String text, int offset, List<StyleSpan> spans) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isJavaIdentifierPart(ch)) {
                sb.append(ch);
            } else {
                break;
            }
        }
        if (!sb.isEmpty() && keywords.match(sb.toString())) {
            spans.add(new StyleSpan(
                    new TextColor(colorString), offset, sb.length()));
        }
        return offset + sb.length();
    }

    static int read(char to, char esc, String colorString, String text, int offset, List<StyleSpan> spans) {
        char prev = 0;
        for (int i = offset + 1; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == to && prev != esc) {
                spans.add(new StyleSpan(
                        new TextColor(colorString), offset, i - offset + 1));
                return i;
            }
            prev = ch;
        }
        return offset;
    }

}
