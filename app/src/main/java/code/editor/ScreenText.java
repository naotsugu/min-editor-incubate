package code.editor;

import code.editor.javafx.FontMetrics;
import com.mammb.code.piecetable.Document;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                draw.text(row.text, 5, y);
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
                    buffer.add(new TextRow(top + i, doc, fm));
                }
            }
            this.width = width;
            this.height = height;
        }

        @Override
        public void scrollNext(int delta) {
            if (delta <= 0) return;
            int next = buffer.isEmpty() ? 0 : buffer.getLast().row + 1;
            buffer.subList(0, Math.min(delta, buffer.size())).clear();
            for (int i = next; i < (next + delta) && i < doc.rows(); i++) {
                buffer.add(new TextRow(i, doc, fm));
            }
        }

        @Override
        public void scrollPrev(int delta) {
            if (delta <= 0) return;
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            delta = Math.min(top, delta);
            if (delta == 0) return;
            buffer.subList(buffer.size() - delta, buffer.size()).clear();
            for (int i = 1; i <= delta; i++) {
                buffer.addFirst(new TextRow(top - i, doc, fm));
            }
        }

        @Override
        public void scrollAt(int row) {
            row = Math.clamp(row, 0, doc.rows() - 1);
            buffer.clear();
            for (int i = row; i < doc.rows(); i++) {
                buffer.add(new TextRow(i, doc, fm));
                if (buffer.size() >= Math.ceil(height / fm.getLineHeight())) break;
            }
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
                draw.text(line.text(), 0, y);
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
                    for (TextLine line : new TextRow(i, doc, fm).wrap(wrap)) {
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
                        List<TextLine> lines = new TextRow(i, doc, fm).wrap(wrap);
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
                List<TextLine> lines = new TextRow(i, doc, fm).wrap(wrap);
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
        public TextRow(int row, Document document, FontMetrics fm) {
            this.row = row;
            this.text = document.getText(row).toString();
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

        List<StyledText> styledText() {
            return null;
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
        List<StyledText> styledText() { return null; }
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

    sealed interface Style {}
    record TextColor(String colorString) implements Style {}
    record BgColor(String colorString) implements Style {}
    record Selected() implements Style {}
    record Emphasize() implements Style {}

    record StyleSpan(Style style, int offset, int length) { }
    record StyledText(String text, List<Style> styles) { }
    class Styles {
        private final Set<Integer> bounds = new HashSet<>();
        private final List<StyleSpan> spans = new ArrayList<>();
        void put(StyleSpan span) {
            bounds.add(span.offset);
            bounds.add(span.offset + span.length);
            spans.add(span);
        }
        List<StyledText> apply(String text) {
            List<StyledText> ret = new ArrayList<>();
            bounds.add(0);
            bounds.add(text.length());
            List<Integer> list = bounds.stream().sorted().toList();
            for (int i = 0; i < list.size() - 1; i++) {
                ret.add(new StyledText(
                        text.substring(list.get(i), list.get(i + 1)),
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
