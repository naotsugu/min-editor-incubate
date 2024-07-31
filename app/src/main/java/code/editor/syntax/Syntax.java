package code.editor.syntax;

import code.editor.ScreenText;
import java.util.List;

public interface Syntax {

    List<ScreenText.StyleSpan> apply(String text);
    String name();

    static Syntax of(String name) {
        return switch (name) {
            case "java" -> new JavaSyntax();
            default -> new Syntax() {
                @Override public List<ScreenText.StyleSpan> apply(String text) {
                    return List.of();
                }
                @Override public String name() {
                    return name;
                }
            };
        };
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


    interface Lexer {

    }

    class LexerSource {
        private CharSequence text;
        private int position = -1;
        private int peekCount = 0;
        public LexerSource(CharSequence text) {
            this.text = text;
        }
        public char readChar() {
            position++;
            peekCount = 0;
            if (position >= text.length()) {
                position = text.length();
                return 0;
            }
            return text.charAt(position);
        }
        public char currentChar() {
            return (position < 0 || position >= text.length()) ? 0 : text.charAt(position);
        }
        public char peekChar() {
            peekCount++;
            int index = position + peekCount;
            if (index >= text.length()) {
                peekCount = Math.min(text.length() - position, peekCount);
                return 0;
            }
            return text.charAt(index);
        }
        public void commitPeek() {
            position += peekCount;
            peekCount = 0;
        }
        public void commitPeekBefore() {
            position += Math.max(peekCount - 1, 0);
            peekCount = 0;
        }
    }

}
