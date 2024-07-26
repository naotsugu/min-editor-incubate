package code.editor;

import code.editor.javafx.FontMetrics;
import com.mammb.code.piecetable.Document;
import java.util.ArrayList;
import java.util.List;

public interface ScreenText {

    void draw(Draw draw);

    class PlainScreenText implements ScreenText {
        double width;
        double height;
        List<TextRow> buffer = new ArrayList<>();
        public PlainScreenText(double width, double height, Document doc, FontMetrics fm) {
            this.width = width;
            this.height = height;
            for (int i = 0; i <= height / fm.getLineHeight() && i < doc.rows(); i++) {
                buffer.add(new TextRow(i, doc, fm));
            }
        }

        @Override
        public void draw(Draw draw) {
            double y = 0;
            for (TextRow row : buffer) {
                draw.fillText(row.text, 0, y);
                y += row.lineHeight;
            }
        }
    }

    class WrapScreenText implements ScreenText {
        double width;
        double height;
        double wrap;
        List<TextLine> buffer = new ArrayList<>();
        List<TextMap> wrapLayout = new ArrayList<>();

        public WrapScreenText(double width, double height, Document doc, FontMetrics fm) {
            this.width = width;
            this.height = height;
            this.wrap = width - fm.getLineHeight() / 3;
            for (int i = 0; i < doc.rows(); i++) {
                for (TextLine line : new TextRow(i, doc, fm).wrap(wrap)) {
                    wrapLayout.add(line.map);
                    if (buffer.size() < height / fm.getLineHeight()) {
                        buffer.add(line);
                    }
                }
            }
        }

        @Override
        public void draw(Draw draw) {
            double y = 0;
            for (TextLine line : buffer) {
                draw.fillText(line.text(), 0, y);
                y += line.lineHeight();
            }
        }
    }

    record TextMap(int row, int subLine, int fromIndex, int toIndex) {}

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
