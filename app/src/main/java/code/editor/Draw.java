package code.editor;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public interface Draw {

    void text(String text, double x, double y);

    void clear();

    class FxDraw implements Draw {
        final GraphicsContext gc;
        Color fgColor = Color.web("#C9D7E6");
        Color bgColor = Color.web("#292929");
        public FxDraw(GraphicsContext gc) {
            this.gc = gc;
            gc.setTextBaseline(VPos.TOP);
            gc.setLineCap(StrokeLineCap.BUTT);
        }
        public void text(String text, double x, double y) {
            gc.setStroke(fgColor);
            gc.setFill(fgColor);
            gc.fillText(text, x, y);
        }

        @Override
        public void clear() {
            Canvas canvas = gc.getCanvas();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

    }

}
