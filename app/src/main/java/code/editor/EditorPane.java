package code.editor;

import code.javafx.FontMetrics;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class EditorPane extends StackPane {

    /** The canvas. */
    private final Canvas canvas;
    /** The graphics context. */
    private final GraphicsContext gc;

    public EditorPane() {
        canvas = new Canvas(640, 480);
        gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);

        var fm = FontMetrics.of(Font.font("Consolas", 15));
        double x = 20;
        double y = 20;

        for (char ch : "This is a text".toCharArray()) {
            gc.fillText(ch + "", x, y);
            x += fm.getAdvance(ch);

        }
    }

}
