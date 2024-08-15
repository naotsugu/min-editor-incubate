package code.editor;

import code.editor.ScreenText.TextRow;
import code.editor.ScreenText.StyleSpan;
import code.editor.ScreenText.UnderLine;
import code.editor.syntax.Syntax;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RowDecorator {

    private static final Decorator TERMINAL = new Decorator() {
        @Override public String name() {  return null; }
        @Override public String text(String text) { return text; }
        @Override public TextRow textRow(TextRow textRow) { return textRow; }
    };

    private final Map<Integer, Decorator> map = new HashMap<>();

    private final List<Composable> ime = new ArrayList<>();

    private Decorator defaultDecorator;

    public RowDecorator(Decorator defaultDecorator) {
        this.defaultDecorator = (defaultDecorator == null) ? TERMINAL : defaultDecorator;
    }

    public Decorator get(int row) {
        return map.getOrDefault(row, defaultDecorator);
    }

    public void put(int row, Decorator decorator) {
        if (decorator instanceof ChainedDecorator cd) {
            cd.next = get(row);
            map.put(row, cd);
        } else {
            map.put(row, decorator);
        }
    }

    public void clear() {
        map.clear();
        ime.clear();
    }

    public void putIme(int row, int col, String text) {
        Composable composable = imeOf(col, text);
        ime.add(composable);
        put(row, composable);
    }
    public List<Composable> ime() {
        return ime;
    }
    public void clearIme() {
        clear();
    }

    public static Decorator syntaxOf(Syntax syntax) {
        return new SyntaxDecorator(syntax);
    }

    public static Composable imeOf(int col, String composedText) {
        return new ImeDecorator(col, composedText);
    }

    public static Decorator accentOf() {
        return new AccentDecorator();
    }

    public interface Decorator {
        String name();
        String text(String text);
        TextRow textRow(TextRow textRow);
    }

    public interface Composable extends Decorator {
        void composed(String text);
    }

    private static abstract class ChainedDecorator implements Decorator {
        Decorator next = TERMINAL;
        @Override public String name() {
            return this.getClass().getSimpleName();
        }
        @Override public final String text(String text) {
            return next.text(textChained(text));
        }
        @Override public final TextRow textRow(TextRow textRow) {
            return next.textRow(textRowChained(textRow));
        }
        protected String textChained(String text) { return text; }
        protected abstract TextRow textRowChained(TextRow textRow);
    }

    private static class SyntaxDecorator extends ChainedDecorator {
        private final Syntax syntax;
        public SyntaxDecorator(Syntax syntax) {
            this.syntax = Objects.requireNonNull(syntax);
        }
        @Override public String name() { return syntax.name(); }
        @Override protected TextRow textRowChained(TextRow textRow) {
            textRow.styles.putAll(syntax.apply(textRow.row, textRow.text));
            return textRow;
        }
    }

    private static class FlashDecorator extends ChainedDecorator implements Composable {
        private int col;
        private String flashText;
        public FlashDecorator(int col) {
            this.col = col;
        }
        public FlashDecorator(int col, String flashText) {
            this.col = col;
            this.flashText = flashText;
        }
        @Override protected String textChained(String text) {
            return text.substring(0, col) + flashText + text.substring(col);
        }
        @Override protected TextRow textRowChained(TextRow textRow) {
            var span = new StyleSpan(new UnderLine(), col, flashText.length());
            textRow.styles.put(span);
            return textRow;
        }

        @Override
        public void composed(String flashText) {
            this.flashText = flashText;
        }
    }

    private static class ImeDecorator extends FlashDecorator {
        public ImeDecorator(int col, String composedText) {
            super(col, composedText);
        }
        @Override public String name() { return "ime"; }
    }

    private static class AccentDecorator extends ChainedDecorator {
        @Override protected TextRow textRowChained(TextRow textRow) {
            // TODO
            return textRow;
        }
    }

}
