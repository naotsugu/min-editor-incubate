package code.editor.syntax;

public interface Syntax {

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
