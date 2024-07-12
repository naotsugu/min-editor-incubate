package code.editor;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;

public class EditorPane extends StackPane {

    /** The canvas. */
    private final Canvas canvas;
    /** The graphics context. */
    private final GraphicsContext gc;

    public EditorPane() {
        canvas = new Canvas(640, 480);
        gc = canvas.getGraphicsContext2D();
    }

}
