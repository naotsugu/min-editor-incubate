package code.editor;

import code.editor.syntax.Syntax;
import com.mammb.code.piecetable.Document;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.input.InputMethodTextRun;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

public class EditorPane extends StackPane {

    /** The canvas. */
    private final Canvas canvas;
    /** The draw. */
    private final Draw draw;

    private ScreenText st;
    private final ScrollBar vs = new ScrollBar();
    private final ScrollBar hs = new ScrollBar();

    public EditorPane() {
        setCursor(Cursor.TEXT);
        setBackground(new Background(new BackgroundFill(
                Color.web("#292929"),
                CornerRadii.EMPTY, Insets.EMPTY)));

        canvas = new Canvas(640, 480);
        canvas.setFocusTraversable(true);
        getChildren().add(canvas);
        draw = new Draw.FxDraw(canvas.getGraphicsContext2D());

        var doc = Document.of(Path.of("build.gradle.kts"));
        st = ScreenText.of(doc, draw.fontMetrics(), Syntax.of("java"));

        // scroll bar
        applyCss(scrollBarCss, vs, hs);
        vs.setCursor(Cursor.DEFAULT);
        vs.setOrientation(Orientation.VERTICAL);
        vs.setMin(0);
        StackPane.setAlignment(vs, Pos.TOP_RIGHT);
        vs.valueProperty().addListener((ob, o, n) -> {
            if (st.getScrolledLineValue() != n.intValue()) {
                st.scrollAt(n.intValue());
                st.draw(draw);
            }
        });
        hs.setCursor(Cursor.DEFAULT);
        hs.setOrientation(Orientation.HORIZONTAL);
        StackPane.setAlignment(hs, Pos.BOTTOM_LEFT);
        hs.setMin(0);
        hs.valueProperty().addListener((ob, o, n) -> {
            if (st.getScrolledXValue() != n.doubleValue()) {
                st.scrollX(n.doubleValue());
                st.draw(draw);
            }
        });
        getChildren().addAll(vs, hs);

        layoutBoundsProperty().addListener((ob, o, n) -> {
            canvas.setWidth(n.getWidth());
            canvas.setHeight(n.getHeight());
            st.setSize(n.getWidth(), n.getHeight());
            draw();
        });

        setOnScroll((ScrollEvent e) -> {
            if (e.getEventType() == ScrollEvent.SCROLL && e.getDeltaY() != 0) {
                if (e.getDeltaY() < 0) {
                    st.scrollNext((int) Math.min(5, Math.abs(e.getDeltaY())));
                } else {
                    st.scrollPrev((int) Math.min(5, e.getDeltaY()));
                }
                draw();
            }
        });
        setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getTarget() == canvas) {
                switch (e.getClickCount()) {
                    case 1 -> st.click(e.getX(), e.getY());
                    case 2 -> st.clickDouble(e.getX(), e.getY());
                    case 3 -> st.clickTriple(e.getX(), e.getY());
                }
                draw();
            }
        });

        setOnKeyPressed((KeyEvent e) -> execute(st, Action.of(e)));
        setOnKeyTyped((KeyEvent e) -> execute(st, Action.of(e)));

        // IME
        canvas.setInputMethodRequests(new InputMethodRequests() {
            @Override public Point2D getTextLocation(int offset) {
                Lang.Loc loc = st.imeOn();
                return canvas.localToScreen(loc.x(), loc.y());
            }
            @Override public int getLocationOffset(int x, int y) { return 0; }
            @Override public void cancelLatestCommittedText() { st.imeOff(); }
            @Override public String getSelectedText() { return ""; }
        });
        canvas.setOnInputMethodTextChanged((InputMethodEvent e) -> {
            if (!e.getCommitted().isEmpty()) {
                st.imeOff();
                execute(st, Action.of(Action.Type.TYPED, e.getCommitted()));
            } else if (!e.getComposed().isEmpty()) {
                if (!st.isImeOn()) st.imeOn();
                st.inputImeComposed(e.getComposed().stream()
                        .map(InputMethodTextRun::getText)
                        .collect(Collectors.joining()));
            } else {
                st.imeOff();
            }
            draw();
        });

    }

    private Action execute(ScreenText st, Action action) {
        if (st.isImeOn()) return Action.EMPTY;
        switch (action.type()) {
            case TYPED              -> { st.input(action.attr()); draw(); }
            case DELETE             -> { st.delete(); draw(); }
            case BACK_SPACE         -> { st.backspace(); draw(); }
            case CARET_RIGHT        -> { st.moveCaretRight(); draw(); }
            case CARET_LEFT         -> { st.moveCaretLeft(); draw(); }
            case CARET_DOWN         -> { st.moveCaretDown(); draw(); }
            case CARET_UP           -> { st.moveCaretUp(); draw(); }
            case HOME               -> { st.moveCaretHome(); draw(); }
            case END                -> { st.moveCaretEnd(); draw(); }
            case PAGE_UP            -> { st.moveCaretPageUp(); draw(); }
            case PAGE_DOWN          -> { st.moveCaretPageDown(); draw(); }
            case SELECT_CARET_RIGHT -> { st.moveCaretSelectRight(); draw(); }
            case SELECT_CARET_LEFT  -> { st.moveCaretSelectLeft(); draw(); }
            case SELECT_CARET_DOWN  -> { st.moveCaretSelectDown(); draw(); }
            case SELECT_CARET_UP    -> { st.moveCaretSelectUp(); draw(); }
            case SELECT_HOME        -> { st.moveCaretSelectHome(); draw(); }
            case SELECT_END         -> { st.moveCaretSelectEnd(); draw(); }
            case SELECT_PAGE_UP     -> { st.moveCaretSelectPageUp(); draw(); }
            case SELECT_PAGE_DOWN   -> { st.moveCaretSelectPageDown(); draw(); }
            case UNDO               -> { st.undo(); draw(); }
            case REDO               -> { st.redo(); draw(); }
            case COPY               -> { st.copyToClipboard(); }
            case PASTE              -> { st.pasteFromClipboard(); draw(); }
            case CUT                -> { st.cutToClipboard(); draw(); }
        }
        return action;
    }

    private void draw() {
        st.draw(draw);
        vs.setMax(st.getScrollableMaxLine());
        vs.setValue(st.getScrolledLineValue());
        vs.setVisibleAmount(st.screenLineSize());
        if (st.getScrollableMaxX() > 0) {
            hs.setMax(st.getScrollableMaxX());
            hs.setPrefWidth(getWidth() - vs.getWidth());
            hs.setVisibleAmount((canvas.getWidth() - vs.getWidth()) *
                    (canvas.getWidth() / (st.getScrollableMaxX() + canvas.getWidth())));
            hs.setVisible(true);
        } else {
            hs.setVisible(false);
        }
    }

    private static void applyCss(String css, Parent... parents) {
        String s = Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
        Arrays.stream(parents).forEach(p -> p.getStylesheets().add(
                String.join(",", "data:text/css;base64", s)));
    }

    private static String scrollBarCss = """
            .scroll-bar:horizontal .track,
            .scroll-bar:vertical .track {
                -fx-background-color :rgba(64,64,64,10);
                -fx-border-color :transparent;
                -fx-background-radius : 0.0em;
                -fx-border-radius :2.0em;
            }
            .scroll-bar:horizontal .increment-button ,
            .scroll-bar:horizontal .decrement-button {
                -fx-background-color :transparent;
                -fx-background-radius : 0.0em;
                -fx-padding :0.0 0.0 10.0 0.0;
            }
            .scroll-bar:vertical .increment-button ,
            .scroll-bar:vertical .decrement-button {
                -fx-background-color :transparent;
                -fx-background-radius : 0.0em;
                -fx-padding :0.0 10.0 0.0 0.0;
            }
            .scroll-bar .increment-arrow,
            .scroll-bar .decrement-arrow {
                -fx-shape : " ";
                -fx-padding :0.15em 0.0;
            }
            .scroll-bar:vertical .increment-arrow,
            .scroll-bar:vertical .decrement-arrow {
                -fx-shape : " ";
                -fx-padding :0.0 0.15em;
            }
            .scroll-bar:horizontal .thumb,
            .scroll-bar:vertical .thumb {
                -fx-background-color :derive(black,90.0%);
                -fx-background-insets : 2.0, 0.0, 0.0;
                -fx-background-radius : 2.0em;
            }
            .scroll-bar:horizontal .thumb:hover,
            .scroll-bar:vertical .thumb:hover {
                -fx-background-color :derive(#4D4C4F,10.0%);
                -fx-background-insets : 2.0, 0.0, 0.0;
                -fx-background-radius : 2.0em;
            }
            """;
}
