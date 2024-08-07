package code.editor;

import code.editor.javafx.FontMetrics;
import code.editor.syntax.Syntax;
import com.mammb.code.piecetable.Document;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
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

    private final Draw draw;

    public EditorPane() {
        setCursor(Cursor.TEXT);
        setBackground(new Background(new BackgroundFill(
                Color.web("#292929"),
                CornerRadii.EMPTY, Insets.EMPTY)));

        canvas = new Canvas(640, 480);
        canvas.setFocusTraversable(true);
        getChildren().add(canvas);

        gc = canvas.getGraphicsContext2D();
        var doc = Document.of(Path.of("build.gradle.kts"));
        var fm = FontMetrics.of(Font.font("Consolas", 16));
        gc.setFont(fm.getFont());

        draw = new Draw.FxDraw(gc);

        var st = ScreenText.wrapOf(doc, fm, Syntax.of("java"));

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

        setOnKeyPressed((KeyEvent e) -> execute(st, Action.of(e)));
        setOnKeyTyped((KeyEvent e) -> execute(st, Action.of(e)));

    }

    private Action execute(ScreenText st, Action action) {
        switch (action.type()) {
            case TYPED       -> { st.input(action.attr()); st.draw(draw); }
            case DELETE      -> { st.delete(); st.draw(draw); }
            case BACK_SPACE  -> { st.backSpace(); st.draw(draw); }
            case CARET_RIGHT -> { st.moveCaretRight(); st.draw(draw); }
            case CARET_LEFT  -> { st.moveCaretLeft(); st.draw(draw); }
            case CARET_DOWN  -> { st.moveCaretDown(); st.draw(draw); }
            case CARET_UP    -> { st.moveCaretUp(); st.draw(draw); }
        }
        return action;
    }
}
