package code.editor;

import code.editor.javafx.FontMetrics;
import com.mammb.code.piecetable.Document;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import java.nio.file.Path;

public class EditorPane extends StackPane {

    /** The canvas. */
    private final Canvas canvas;
    /** The graphics context. */
    private final GraphicsContext gc;

    public EditorPane() {
        canvas = new Canvas(640, 480);
        getChildren().add(canvas);

        gc = canvas.getGraphicsContext2D();
        Document doc = Document.of(Path.of("build.gradle.kts"));
        FontMetrics fm = FontMetrics.of(Font.font("Consolas", 16));
        gc.setFont(fm.getFont());
        gc.setTextBaseline(VPos.TOP);

        ScreenText st = new ScreenText.PlainScreenText(640, 480, doc, fm);
        st.draw(new Draw.FxDraw(gc));
    }

}
