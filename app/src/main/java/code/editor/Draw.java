package code.editor;

import code.editor.javafx.FxFontMetrics;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public interface Draw {

    void text(String text, double x, double y, double w, List<ScreenText.Style> styles);
    void fillSelection(double x1, double y1, double x2, double y2, double l, double r);
    void clear();
    void caret(double x, double y);
    FontMetrics fontMetrics();


    class FxDraw implements Draw {
        private final GraphicsContext gc;
        private final FontMetrics fontMetrics;
        private final Map<String, Color> webColors = new WeakHashMap<>();
        private final TextStyle textStyle = new TextStyle();
        private Color fgColor = Color.web("#C9D7E6");
        private Color bgColor = Color.web("#292929");
        private Color sbgColor = Color.web("#214283");
        private Color caretColor = Color.web("#FFE0B2");

        public FxDraw(GraphicsContext gc) {
            this.gc = gc;
            //gc.setTextBaseline(VPos.TOP);
            gc.setLineCap(StrokeLineCap.BUTT);

            String fontName = System.getProperty("os.name").toLowerCase().startsWith("windows")
                    ? "MS Gothic" : "Consolas";
            Font font = Font.font(fontName, 15);
            fontMetrics = FxFontMetrics.of(font);
            gc.setFont(font);
        }

        @Override
        public void text(String text, double x, double y, double w, List<ScreenText.Style> styles) {
            double h = fontMetrics.getLineHeight();
            y += fontMetrics.getAscent();
            apply(styles);
            gc.setFill(textStyle.backColor == null ? bgColor : textStyle.backColor);
            gc.fillRect(x, y, w, h);
            gc.setStroke(textStyle.textColor == null ? fgColor : textStyle.textColor);
            gc.setFill(textStyle.textColor == null ? fgColor : textStyle.textColor);
            gc.fillText(text, x, y);
            if (textStyle.lineColor != null) {
                gc.setStroke(textStyle.lineColor);
                gc.setLineWidth(1);
                gc.setLineDashes(textStyle.lineDash);
                gc.strokeLine(x, y + h - 4, x + w, y + h - 4);
            }
        }

        @Override
        public void fillSelection(double x1, double y1, double x2, double y2, double l, double r) {
            double lineHeight = fontMetrics().getLineHeight();
            gc.setFill(sbgColor);
            if (y1 == y2) {
                gc.fillRect(x1, y1, x2 - x1, lineHeight);
            } else {
                //                    0:(x1, y1)
                //                     _______________________  1:(r, y1)
                // 6:(l, y1 + h) _____|                      |
                //               |   7:(x1, y1 + h)          |
                //               |                           |
                //               |     3:(x2, y2)  __________| 2:(r, y2)
                //               |________________|
                // 5:(l, y2 + h)                4:(x2, y2 + h)
                double[] x = new double[8];
                double[] y = new double[8];
                x[0] = x1; y[0]= y1;
                x[1] = r;  y[1]= y1;
                x[2] = r;  y[2]= y2;
                x[3] = x2; y[3]= y2;
                x[4] = x2; y[4]= y2 + lineHeight;
                x[5] = l;  y[5]= y2 + lineHeight;
                x[6] = l;  y[6]= y1 + lineHeight;
                x[7] = x1; y[7]= y1 + lineHeight;
                gc.fillPolygon(x, y, 8);
            }
        }

        @Override
        public void clear() {
            Canvas canvas = gc.getCanvas();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
        @Override
        public void caret(double x, double y) {
            gc.setLineDashes(0);
            gc.setStroke(caretColor);
            gc.setLineWidth(1.5);
            gc.strokeLine(x - 1, y, x - 1, y + fontMetrics.getLineHeight());
        }

        @Override
        public FontMetrics fontMetrics() {
            return fontMetrics;
        }

        private void apply(List<ScreenText.Style> styles) {
            textStyle.clear();
            for (ScreenText.Style style : styles) {
                switch (style) {
                    case ScreenText.TextColor s -> textStyle.textColor = webColors.computeIfAbsent(s.colorString(), Color::web);
                    case ScreenText.BgColor   s -> textStyle.backColor = webColors.computeIfAbsent(s.colorString(), Color::web);
                    case ScreenText.UnderLine s -> {
                        textStyle.lineColor = webColors.computeIfAbsent(s.colorString(), Color::web);
                        textStyle.lineDash = s.dash();
                    }
                    default -> { }
                }
            }
        }

        private static class TextStyle {
            public Color textColor;
            public Color backColor;
            public Color lineColor;
            public double lineDash;
            public void clear() {
                textColor = backColor = lineColor = null;
                lineDash = 0;
            }
        }

    }

}
