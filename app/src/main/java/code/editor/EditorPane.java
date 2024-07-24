package code.editor;

import com.mammb.code.piecetable.Document;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import java.nio.file.Path;

public class EditorPane extends StackPane {

    /** The canvas. */
    private final Canvas canvas;
    /** The graphics context. */
    private final GraphicsContext gc;

    private final Document document;
    private final ScreenText screenText;

    public EditorPane() {
        canvas = new Canvas(640, 480);
        gc = canvas.getGraphicsContext2D();
        document = Document.of(Path.of("build.gradle.kts"));
        screenText = new ScreenText(canvas.getWidth(), canvas.getHeight(), document);
        getChildren().add(canvas);

        gc.setFont(screenText.fm.getFont());
        double x = 20;
        double y = 20;
        for (LineText lineText : screenText.lines) {
            gc.fillText(lineText.text(), x, y);
            y += lineText.lineHeight();
        }
    }

}
