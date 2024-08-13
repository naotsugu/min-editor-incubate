package code.editor.syntax;

import code.editor.ScreenText;
import java.util.List;

public interface Syntax {

    String name();
    List<ScreenText.StyleSpan> apply(int row, String text);

    record PassThrough(String name) implements Syntax {
        @Override public List<ScreenText.StyleSpan> apply(int row, String text) {
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

    /**
     *
     */
    enum Palette {
        DEEP_GREEN("#6A8759"),
        ORANGE("#CC7832"),
        ;

        Palette(String colorString) {
            this.colorString = colorString;
        }

        final String colorString;
    }

    static int read(Trie keywords, String colorString, String text, int offset, List<ScreenText.StyleSpan> spans) {
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
            spans.add(new ScreenText.StyleSpan(
                    new ScreenText.TextColor(colorString), offset, sb.length()));
        }
        return offset + sb.length();
    }

    static int read(char to, char esc, String colorString, String text, int offset, List<ScreenText.StyleSpan> spans) {
        for (int i = offset + 1; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == esc) continue;
            if (ch == to) {
                spans.add(new ScreenText.StyleSpan(
                        new ScreenText.TextColor(colorString), offset, i - offset + 1));
                return i;
            }
        }
        return offset;
    }

}
