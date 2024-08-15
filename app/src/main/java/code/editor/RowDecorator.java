package code.editor;

import code.editor.ScreenText.TextRow;
import code.editor.ScreenText.StyleSpan;
import code.editor.ScreenText.UnderLine;
import code.editor.syntax.Syntax;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RowDecorator {

    private static final Decorator TERMINAL = new Decorator() {
        @Override public String name() {  return null; }
        @Override public String text(String text) { return text; }
        @Override public TextRow textRow(TextRow textRow) { return textRow; }
    };

    private final Map<Integer, Decorator> map = new HashMap<>();

    private Decorator defaultDecorator;

    public RowDecorator(Decorator defaultDecorator) {
        this.defaultDecorator = (defaultDecorator == null) ? TERMINAL : defaultDecorator;
    }

    public Decorator get(int row) {
        return map.getOrDefault(row, defaultDecorator);
    }

    public void put(int row, Decorator decorator) {
        map.put(row, decorator);
    }

    public void clear() {
        map.clear();
    }

    public static Decorator syntaxOf(Syntax syntax) {
        return new SyntaxDecorator(syntax);
    }

    public static Decorator flashOf() {
        return new FlashDecorator();
    }
    public static Decorator accentOf() {
        return new AccentDecorator();
    }

    public interface Decorator {
        String name();
        String text(String text);
        TextRow textRow(TextRow textRow);
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

    private static class FlashDecorator extends ChainedDecorator {
        private int col;
        private String flash;
        public FlashDecorator() {
        }
        @Override protected String textChained(String text) {
            return text.substring(0, col) + flash + text.substring(col);
        }
        @Override protected TextRow textRowChained(TextRow textRow) {
            var span = new StyleSpan(new UnderLine(), col, flash.length());
            textRow.styles.put(span);
            return textRow;
        }
    }

    private static class AccentDecorator extends ChainedDecorator {
        @Override protected TextRow textRowChained(TextRow textRow) {
            // TODO
            return textRow;
        }
    }

}
