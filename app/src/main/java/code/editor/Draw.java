package code.editor;

import javafx.scene.canvas.GraphicsContext;

public interface Draw {

    void fillText(String text, double x, double y);

    class FxDraw implements Draw {
        GraphicsContext gc;
        public FxDraw(GraphicsContext gc) {
            this.gc = gc;
        }
        public void fillText(String text, double x, double y) {
            gc.fillText(text, x, y);
        }

    }

}
