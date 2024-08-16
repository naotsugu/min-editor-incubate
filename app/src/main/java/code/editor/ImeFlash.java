package code.editor;

import code.editor.ScreenText.TextRow;
import code.editor.ScreenText.StyleSpan;
import code.editor.Lang.Indexed;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ImeFlash {

    private final List<Pin> pins = new ArrayList<>();
    private String composedText = "";
    public void on(int row, int col) { pins.add(new Pin(row, col)); }
    public void clear() { pins.clear(); composedText = ""; }
    public boolean isEmpty() { return pins.isEmpty(); }
    public void composed(String text) { composedText = (text == null) ? "" : text; }
    public String composedText() { return composedText; }

    public TextRow apply(int row, String source, Function<String, TextRow> fun) {

        if (pins.isEmpty() || composedText.isEmpty()) {
            return fun.apply(source);
        }

        var pinedCols = pins.stream()
                .filter(p -> p.row == row)
                .map(Pin::col)
                .toList();

        if (pinedCols.isEmpty()) {
            return fun.apply(source);
        }

        List<StyleSpan> spans = new ArrayList<>();
        var sb = new StringBuilder();
        int from = 0;
        for (int i = 0; i < pinedCols.size(); i++) {
            int to = pinedCols.get(i);
            sb.append(source, from, to);
            sb.append(composedText);
            spans.add(new StyleSpan(new ScreenText.UnderLine(1.0), to, composedText.length()));
            from = to;
        }
        sb.append(source.substring(from));
        TextRow textRow = fun.apply(sb.toString());
        textRow.styles.putAll(spans);

        return textRow;
    }

    record Pin(int row, int col) { }
}
