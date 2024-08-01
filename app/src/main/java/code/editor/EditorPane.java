package code.editor;

import code.editor.javafx.FontMetrics;
import com.mammb.code.piecetable.Document;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.nio.file.Path;

public class EditorPane extends StackPane {

    /** The canvas. */
    private final Canvas canvas;
    /** The graphics context. */
    private final GraphicsContext gc;


    public EditorPane() {

        setCursor(Cursor.TEXT);
        setBackground(new Background(new BackgroundFill(
                Color.web("#292929"),
                CornerRadii.EMPTY, Insets.EMPTY)));

        canvas = new Canvas(640, 480);
        getChildren().add(canvas);

        gc = canvas.getGraphicsContext2D();
        var doc = Document.of(Path.of("build.gradle.kts"));
        var fm = FontMetrics.of(Font.font("Consolas", 16));
        gc.setFont(fm.getFont());

        Draw draw = new Draw.FxDraw(gc);

        var st = ScreenText.of(doc, fm);

        layoutBoundsProperty().addListener((ob, o, n) -> {
            canvas.setWidth(n.getWidth());
            canvas.setHeight(n.getHeight());
            st.size(n.getWidth(), n.getHeight());
            st.draw(draw);
        });

        setOnScroll((ScrollEvent e) -> {
            if (e.getEventType() == ScrollEvent.SCROLL && e.getDeltaY() != 0) {
                if (e.getDeltaY() < 0) {
                    st.scrollNext(1);
                } else {
                    st.scrollPrev(1);
                }
                st.draw(draw);
            }
        });
    }

}
