package code.editor;

import code.editor.javafx.FontMetrics;
import com.mammb.code.piecetable.Document;
import java.util.ArrayList;
import java.util.List;

public interface ScreenText {

    void draw(Draw draw);
    void size(double width, double height);
    void scrollNext(int n);
    void scrollPrev(int n);


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
                int fromIndex = (int) (height / fm.getLineHeight()) + 1;
                if (fromIndex < buffer.size() - 1) {
                    buffer.subList(fromIndex, buffer.size()).clear();
                }
            } else if (this.height < height) {
                int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
                for (int i = buffer.size(); i <= height / fm.getLineHeight() && i < doc.rows(); i++) {
                    buffer.add(new TextRow(top + i, doc, fm));
                }
            }
            this.width = width;
            this.height = height;
        }

        @Override
        public void scrollNext(int n) {
            int next = buffer.isEmpty() ? 0 : buffer.getLast().row + 1;
            buffer.subList(0, Math.min(n, buffer.size())).clear();
            for (int i = next; i < (next + n) && i < doc.rows(); i++) {
                buffer.add(new TextRow(i, doc, fm));
            }
        }

        @Override
        public void scrollPrev(int n) {
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            n = Math.max(0, top - n);
            if (n == 0) return;
            buffer.subList(buffer.size() - n, buffer.size()).clear();
            for (int i = 1; i <= n; i++) {
                buffer.addFirst(new TextRow(top - i, doc, fm));
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
            double y = 0;
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
        public void scrollNext(int n) {
        }

        @Override
        public void scrollPrev(int n) {

        }
    }

    record TextMap(int row, int subLine, int fromIndex, int toIndex) {
        static TextMap empty = new TextMap(0, 0, 0, 0);
    }

    class TextRow {
        int row;
        String text;
        float[] advances;
        float lineHeight;
        public TextRow(int row, Document document, FontMetrics fm) {
            this.row = row;
            this.text = document.getText(row).toString();
            this.advances = advances(text, fm);
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

}
