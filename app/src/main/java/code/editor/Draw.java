package code.editor;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Draw {

    void text(String text, double x, double y);
    void text(String text, double x, double y, List<ScreenText.Style> styles);
    void text(String text, double x, double y, double w, double h, List<ScreenText.Style> styles);
    void clear();
    void caret(double x, double y, double height);


    class FxDraw implements Draw {
        final GraphicsContext gc;
        Color fgColor = Color.web("#C9D7E6");
        Color bgColor = Color.web("#292929");
        Color caretColor = Color.web("#FFE0B2");

        Map<String, Color> colorMap = new HashMap<>();
        public FxDraw(GraphicsContext gc) {
            this.gc = gc;
            gc.setTextBaseline(VPos.TOP);
            gc.setLineCap(StrokeLineCap.BUTT);
        }
        @Override
        public void text(String text, double x, double y) {
            gc.setStroke(fgColor);
            gc.setFill(fgColor);
            gc.fillText(text, x, y);
        }
        @Override
        public void text(String text, double x, double y, List<ScreenText.Style> styles) {
            Optional<String> colorString = styles.stream().filter(ScreenText.TextColor.class::isInstance)
                    .map(ScreenText.TextColor.class::cast)
                    .map(ScreenText.TextColor::colorString)
                    .findFirst();
            Color color = colorString.isPresent() ? colorMap.computeIfAbsent(colorString.get(), Color::web) : fgColor;
            gc.setStroke(color);
            gc.setFill(color);
            gc.fillText(text, x, y);
        }
        @Override
        public void text(String text, double x, double y, double w, double h, List<ScreenText.Style> styles) {
            Optional<String> colorString = styles.stream().filter(ScreenText.BgColor.class::isInstance)
                    .map(ScreenText.BgColor.class::cast)
                    .map(ScreenText.BgColor::colorString)
                    .findFirst();
            Color color = colorString.isPresent() ? colorMap.computeIfAbsent(colorString.get(), Color::web) : bgColor;
            gc.setFill(color);
            gc.fillRect(x, y, w, h);
            text(text, x, y, styles);
        }

        @Override
        public void clear() {
            Canvas canvas = gc.getCanvas();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
        @Override
        public void caret(double x, double y, double height) {
            gc.setLineDashes(0);
            gc.setStroke(caretColor);
            gc.setLineWidth(1.5);
            gc.strokeLine(x - 1.5, y + 1, x - 1.5, y + height - 1);
        }

    }

}
