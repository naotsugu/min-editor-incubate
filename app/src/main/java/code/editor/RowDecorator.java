package code.editor;

import code.editor.ScreenText.TextRow;
import code.editor.syntax.Syntax;
import java.util.HashMap;
import java.util.Map;

public class RowDecorator {

    private static final Decorator TERMINAL = textRow -> textRow;

    private final Map<Integer, Decorator> map = new HashMap<>();
    private Decorator defaultDecorator;

    public RowDecorator(Decorator defaultDecorator) {
        this.defaultDecorator = (defaultDecorator == null) ? TERMINAL : defaultDecorator;
    }
    public static RowDecorator of(Syntax syntax) {
        return new RowDecorator(new SyntaxDecorator(syntax));
    }

    public TextRow apply(TextRow textRow) {
        return map.getOrDefault(textRow.row, defaultDecorator).textRow(textRow);
    }
    public void clear() { map.clear(); }

    public interface Decorator {
        TextRow textRow(TextRow textRow);
    }

    private static abstract class ChainedDecorator implements Decorator {
        private Decorator self;
        private Decorator next = TERMINAL;
        public ChainedDecorator(Decorator self) {
            this.self = self;
        }
        @Override public final TextRow textRow(TextRow textRow) {
            return next.textRow(self.textRow(textRow));
        }
        public void add(Decorator that) {
            if (that instanceof ChainedDecorator chain) {
                chain.next = this.next;
                this.next = chain;
            } else {
                this.next = that;
            }
        }
    }

    private static class SyntaxDecorator extends ChainedDecorator {
        public SyntaxDecorator(Syntax syntax) {
            super(textRow -> {
                textRow.styles.putAll(syntax.apply(textRow.row, textRow.text));
                return textRow;
            });
        }
    }

    private static class AccentDecorator extends ChainedDecorator {
        public AccentDecorator() {
            super(TERMINAL);
        }
    }

}
