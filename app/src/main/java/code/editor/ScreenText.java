package code.editor;

import code.editor.javafx.FontMetrics;
import code.editor.syntax.Syntax;
import com.mammb.code.piecetable.Document;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface ScreenText {

    void draw(Draw draw);
    void size(double width, double height);
    void scrollNext(int delta);
    void scrollPrev(int delta);
    void scrollAt(int n);


    static ScreenText of(Document doc, FontMetrics fm) {
        return new PlainScreenText(doc, fm);
    }

    static ScreenText wrapOf(Document doc, FontMetrics fm) {
        return new WrapScreenText(doc, fm);
    }

    /**
     * PlainScreenText.
     */
    class PlainScreenText implements ScreenText {
        double width = 0;
        double height = 0;
        final Document doc;
        final FontMetrics fm;
        Syntax syntax = Syntax.of("java");
        List<TextRow> buffer = new ArrayList<>();
        public PlainScreenText(Document doc, FontMetrics fm) {
            this.doc = doc;
            this.fm = fm;
        }
        @Override
        public void draw(Draw draw) {
            draw.clear();
            double y = 5;
            for (TextRow row : buffer) {
                double x = 1;
                for (StyledText st : row.styledTexts()) {
                    draw.text(st.text, x, y, st.styles);
                    x += st.width;
                }
                y += row.lineHeight;
            }
        }

        @Override
        public void size(double width, double height) {
            if (width <= 0 || height <= 0) return;
            if (this.height > height) {
                int fromIndex = (int) Math.ceil(height / fm.getLineHeight()) + 1;
                if (fromIndex < buffer.size() - 1) {
                    buffer.subList(fromIndex, buffer.size()).clear();
                }
            } else if (this.height < height) {
                int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
                for (int i = buffer.size(); i <= Math.ceil(height / fm.getLineHeight()) && i < doc.rows(); i++) {
                    buffer.add(createRow(top + i));
                }
            }
            this.width = width;
            this.height = height;
        }

        @Override
        public void scrollNext(int delta) {
            assert delta > 0;

            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            int maxTop = (int) (doc.rows() - Math.ceil(height / fm.getLineHeight()) * 0.6);
            if (top + delta >= maxTop) {
                delta = maxTop - top;
            }

            int next = buffer.isEmpty() ? 0 : buffer.getLast().row + 1;
            buffer.subList(0, Math.min(delta, buffer.size())).clear();
            for (int i = next; i < (next + delta) && i < doc.rows(); i++) {
                buffer.add(createRow(i));
            }
        }

        @Override
        public void scrollPrev(int delta) {
            assert delta > 0;
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            delta = Math.clamp(delta, 0, top);
            if (delta == 0) return;
            if (buffer.size() >= Math.ceil(height / fm.getLineHeight())) {
                buffer.subList(buffer.size() - delta, buffer.size()).clear();
            }
            for (int i = 1; i <= delta; i++) {
                buffer.addFirst(createRow(top - i));
            }
        }

        @Override
        public void scrollAt(int row) {
            row = Math.clamp(row, 0, doc.rows() - 1);
            buffer.clear();
            for (int i = row; i < doc.rows(); i++) {
                buffer.add(createRow(i));
                if (buffer.size() >= Math.ceil(height / fm.getLineHeight())) break;
            }
        }

        private TextRow createRow(int i) {
            var row = new TextRow(i, doc.getText(i).toString(), fm);
            row.styles.putAll(syntax.apply(row.text));
            return row;
        }

    }

    /**
     * WrapScreenText
     */
    class WrapScreenText implements ScreenText {
        double width = 0;
        double height = 0;
        double wrap = 0;
        int topLine = 0;
        private final Document doc;
        private final FontMetrics fm;
        List<TextLine> buffer = new ArrayList<>();
        List<TextMap> wrapLayout = new ArrayList<>();

        public WrapScreenText(Document doc, FontMetrics fm) {
            this.doc = doc;
            this.fm = fm;
        }

        @Override
        public void draw(Draw draw) {
            draw.clear();
            double y = 5;
            for (TextLine line : buffer) {
                double x = 1;
                for (StyledText st : line.styledTexts()) {
                    draw.text(st.text, x, y);
                    x += st.width;
                }
                y += line.lineHeight();
            }
        }

        @Override
        public void size(double width, double height) {

            if (width <= 0 || height <=0 ||
                    (this.width == width && this.height == height)) return;

            if (this.width != width) {
                TextMap top = buffer.isEmpty() ? TextMap.empty : buffer.getFirst().map;
                this.wrap = width - fm.getLineHeight() / 3;
                wrapLayout.clear();
                buffer.clear();
                for (int i = 0; i < doc.rows(); i++) {
                    for (TextLine line : new TextRow(i, doc.getText(i).toString(), fm).wrap(wrap)) {
                        wrapLayout.add(line.map);
                        if (top.row <= line.map.row) {
                            if (top.row == line.map.row && top.subLine > line.map.subLine) continue;
                            if (buffer.size() < height / fm.getLineHeight()) {
                                buffer.add(line);
                            }
                        }
                    }
                }
            } else {
                if (this.height > height) {
                    int fromIndex = (int) (height / fm.getLineHeight()) + 1;
                    if (fromIndex < buffer.size() - 1) {
                        buffer.subList(fromIndex, buffer.size()).clear();
                    }
                } else if (this.height < height) {
                    TextMap bottom = buffer.isEmpty() ? TextMap.empty : buffer.getLast().map;
                    for (int i = bottom.row; i < doc.rows(); i++) {
                        List<TextLine> lines = new TextRow(i, doc.getText(i).toString(), fm).wrap(wrap);
                        for (TextLine line : lines) {
                            if (bottom.row == line.map.row && bottom.subLine <= line.map.subLine) continue;
                            buffer.add(line);
                        }
                    }
                }
            }
            this.width = width;
            this.height = height;
        }

        @Override
        public void scrollNext(int delta) {
            int maxTop = (int) (wrapLayout.size() - Math.ceil(height / fm.getLineHeight()) * 0.6);
            if (topLine + delta >= maxTop) {
                delta = maxTop - topLine;
            }
            scrollAt(topLine + delta);
        }

        @Override
        public void scrollPrev(int delta) {
            scrollAt(topLine - delta);
        }

        @Override
        public void scrollAt(int line) {
            topLine = Math.clamp(line, 0, wrapLayout.size() - 1);
            buffer.clear();
            TextMap map = wrapLayout.get(topLine);
            for (int i = map.row; i < doc.rows(); i++) {
                List<TextLine> lines = new TextRow(i, doc.getText(i).toString(), fm).wrap(wrap);
                int start = (i == map.row) ? map.subLine : 0;
                for (int j = start; j < lines.size(); j++) {
                    buffer.add(lines.get(j));
                    if (buffer.size() >= Math.ceil(height / fm.getLineHeight())) break;
                }
            }
        }
    }

    record TextMap(int row, int subLine, int fromIndex, int toIndex) {
        static TextMap empty = new TextMap(0, 0, 0, 0);
    }

    class TextRow {
        int row;
        String text;
        float[] advances;
        Styles styles;
        float lineHeight;
        public TextRow(int row, String text, FontMetrics fm) {
            this.row = row;
            this.text = text;
            this.advances = advances(text, fm);
            this.styles = new Styles();
            this.lineHeight = fm.getLineHeight();
        }
        private List<TextLine> wrap(double width) {
            if (width <= 0) {
                return List.of(new TextLine(this,
                        new TextMap(row, 0, 0, text.length())));
            }
            double w = 0;
            int fromIndex = 0;
            List<TextLine> wrapped = new ArrayList<>();
            for (int i = 0; i < text.length(); i++) {
                float advance = advances[i];
                if (advance <= 0) continue;
                if (w + advance > width) {
                    wrapped.add(new TextLine(this,
                            new TextMap(row, wrapped.size(), fromIndex, i)));
                    w = 0;
                    fromIndex = i;
                }
                w += advance;
            }
            wrapped.add(new TextLine(this,
                    new TextMap(row, wrapped.size(), fromIndex, text.length())));
            return wrapped;
        }

        List<StyledText> styledTexts() {
            return styles.apply(text, advances);
        }
    }

    class TextLine {
        TextRow parent;
        TextMap map;
        public TextLine(TextRow parent, TextMap map) {
            this.parent = parent;
            this.map = map;
        }
        float lineHeight() { return parent.lineHeight; }
        String text() { return parent.text.substring(map.fromIndex, map.toIndex); }
        boolean hasNextLine() { return map.toIndex < parent.text.length(); }
        boolean hasPrevLine() { return map.fromIndex > 0; }
        List<StyledText> styledTexts() { return parent.styles.apply(map.fromIndex, map.toIndex, parent.text, parent.advances); }
    }

    private static float[] advances(String text, FontMetrics fm) {
        float[] advances = new float[text.length()];
        for (int i = 0; i < text.length(); i++) {
            char ch1 = text.charAt(i);
            if (Character.isHighSurrogate(ch1)) {
                advances[i] = fm.getAdvance(ch1, text.charAt(i + 1));
                i++;
            } else {
                advances[i] = fm.getAdvance(ch1, (char) 0);
            }
        }
        return advances;
    }

    private static float width(float[] advances, int from, int to) {
        float ret = 0;
        for (int i = from; i < to; i++) ret += advances[i];
        return ret;
    }

    sealed interface Style {}
    record TextColor(String colorString) implements Style {}
    record BgColor(String colorString) implements Style {}
    record Selected() implements Style {}
    record Emphasize() implements Style {}

    record StyleSpan(Style style, int offset, int length) { }
    record StyledText(String text, float width, List<Style> styles) { }
    class Styles {
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
        List<StyledText> apply(String text, float[] advances) {
            return apply(0, text.length(), text, advances);
        }
        List<StyledText> apply(int from, int to, String text, float[] advances) {
            assert text.length() == advances.length;
            if (bounds.isEmpty()) {
                return List.of(new StyledText(
                        text.substring(from, to),
                        width(advances, from, to),
                        List.of()));
            }
            List<Integer> list = bounds.stream()
                    .filter(i -> from <= i && i <= to)
                    .sorted()
                    .collect(Collectors.toList());
            if (list.getFirst() != from) {
                list.addFirst(from);
            }
            if (list.getLast() != to) {
                list.addLast(to);
            }
            List<StyledText> ret = new ArrayList<>();
            for (int i = 0; i < list.size() - 1; i++) {
                int start = list.get(i);
                int end   = list.get(i + 1);
                ret.add(new StyledText(
                        text.substring(start, end),
                        width(advances, start, end),
                        stylesOf(list.get(i))));
            }
            return ret;

        }
        private List<Style> stylesOf(int index) {
            return spans.stream()
                    .filter(span -> span.offset <= index && index < (span.offset + span.length))
                    .map(span -> span.style)
                    .toList();
        }
    }

}
