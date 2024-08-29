package code.editor;

import code.editor.Lang.*;
import code.editor.syntax.Syntax;
import com.mammb.code.piecetable.Document;
import com.mammb.code.piecetable.TextEdit;
import com.mammb.code.piecetable.TextEdit.Pos;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import code.editor.Style.*;
import javafx.scene.input.DataFormat;

public interface ScreenText {

    int MARGIN_TOP = 5;
    int MARGIN_LEFT = 5;
    int TAB_SIZE = 4;

    void draw(Draw draw);
    void setSize(double width, double height);
    int getScrollableMaxLine();
    int getScrolledLineValue();
    double getScrollableMaxX();
    double getScrolledXValue();
    void scrollX(double x);
    void scrollNext(int delta);
    void scrollPrev(int delta);
    void scrollAt(int n);
    void moveCaretRight();
    void moveCaretSelectRight();
    void moveCaretLeft();
    void moveCaretSelectLeft();
    void moveCaretDown();
    void moveCaretSelectDown();
    void moveCaretUp();
    void moveCaretSelectUp();
    void moveCaretHome();
    void moveCaretEnd();
    void moveCaretSelectHome();
    void moveCaretSelectEnd();
    void moveCaretPageUp();
    void moveCaretPageDown();
    void moveCaretSelectPageUp();
    void moveCaretSelectPageDown();
    void input(String text);
    void delete();
    void backspace();
    void undo();
    void redo();
    void click(double x, double y);
    void clickDouble(double x, double y);
    void clickTriple(double x, double y);
    void moveDragged(double x, double y);
    Loc imeOn();
    void imeOff();
    boolean isImeOn();
    void inputImeComposed(String text);
    void pasteFromClipboard();
    void copyToClipboard();
    void cutToClipboard();
    Path path();
    void save(Path path);
    int screenLineSize();

    static ScreenText of(Document doc, FontMetrics fm, Syntax syntax) {
        return new PlainScreenText(doc, fm, syntax);
    }

    static ScreenText wrapOf(Document doc, FontMetrics fm, Syntax syntax) {
        return new WrapScreenText(doc, fm, syntax);
    }

    /**
     * AbstractScreenText.
     */
    abstract class AbstractScreenText implements ScreenText {
        protected double width = 0, height = 0;
        protected int screenLineSize = 0;
        protected final TextEdit ed;
        protected final FontMetrics fm;
        protected final RowDecorator rowDecorator;
        protected final ImeFlash imeFlash = new ImeFlash();
        protected final List<Caret> carets = new ArrayList<>();

        public AbstractScreenText(TextEdit ed, FontMetrics fm, Syntax syntax) {
            this.ed = ed;
            this.fm = fm;
            this.rowDecorator = RowDecorator.of(syntax);
            this.carets.add(new Caret(0, 0));
        }

        @Override
        public void moveCaretRight() {
            carets.forEach(c -> { c.clearMark(); moveCaretRight(c); });
            scrollToCaret();
        }
        @Override
        public void moveCaretSelectRight() {
            carets.forEach(c -> { if (!c.hasMark()) c.mark(); moveCaretRight(c); });
            scrollToCaret();
        }
        private void moveCaretRight(Caret caret) {
            caret.vPos = -1;
            TextRow row = textRowAt(caret.row);
            if (row.text.isEmpty()) return;
            caret.col += row.isHighSurrogate(caret.col) ? 2 : 1;
            if (caret.col > row.textLength()) {
                caret.col = 0;
                caret.row = Math.min(caret.row + 1, ed.rows());
            }
        }
        @Override
        public void moveCaretLeft() {
            carets.forEach(c -> { c.clearMark(); moveCaretLeft(c); });
            scrollToCaret();
        }
        @Override
        public void moveCaretSelectLeft() {
            carets.forEach(c -> { if (!c.hasMark()) c.mark(); moveCaretLeft(c); });
            scrollToCaret();
        }
        private void moveCaretLeft(Caret caret) {
            caret.vPos = -1;
            if (caret.isZero()) return;

            if (caret.col > 0) {
                TextRow textRow = textRowAt(caret.row);
                caret.col -= textRow.isLowSurrogate(caret.col - 1) ? 2 : 1;
            } else {
                caret.row = Math.max(0, caret.row - 1);
                TextRow textRow = textRowAt(caret.row);
                caret.col = textRow.textLength();
            }
        }
        @Override
        public void moveCaretDown() {
            carets.forEach(c -> { c.clearMark(); moveCaretDown(c); });
            scrollToCaret();
        }
        @Override
        public void moveCaretSelectDown() {
            carets.forEach(c -> { if (!c.hasMark()) c.mark();  moveCaretDown(c); });
            scrollToCaret();
        }
        protected abstract void moveCaretDown(Caret caret);
        @Override
        public void moveCaretUp() {
            carets.forEach(c -> { c.clearMark(); moveCaretUp(c); });
            scrollToCaret();
        }
        @Override
        public void moveCaretSelectUp() {
            carets.forEach(c -> {if (!c.hasMark()) c.mark();  moveCaretUp(c); });
            scrollToCaret();
        }
        protected abstract void moveCaretUp(Caret caret);
        @Override
        public void moveCaretHome() { carets.forEach(c -> { c.clearMark(); moveCaretHome(c); }); }
        @Override
        public void moveCaretSelectHome() { carets.forEach(c -> { if (!c.hasMark()) c.mark(); moveCaretHome(c); }); }
        private void moveCaretHome(Caret caret) { caret.vPos = -1; caret.col = 0; }
        @Override
        public void moveCaretEnd() { carets.forEach(c -> { c.clearMark(); moveCaretEnd(c); }); }
        @Override
        public void moveCaretSelectEnd() { carets.forEach(c -> { if (!c.hasMark()) c.mark(); moveCaretEnd(c); }); }
        private void moveCaretEnd(Caret caret) { caret.vPos = -1; caret.col = textRowAt(caret.row).textLength(); }

        @Override
        public void moveCaretPageUp() { carets.forEach(Caret::clearMark); moveCaretPageUp(carets.getFirst()); }
        @Override
        public void moveCaretPageDown() { carets.forEach(Caret::clearMark); moveCaretPageDown(carets.getFirst()); }
        @Override
        public void moveCaretSelectPageUp() {
            carets.stream().skip(1).forEach(Caret::clearMark);
            Caret c = carets.getFirst();
            if (!c.hasMark()) c.mark();
            moveCaretPageUp(c);
        }
        @Override
        public void moveCaretSelectPageDown() {
            carets.stream().skip(1).forEach(Caret::clearMark);
            Caret c = carets.getFirst();
            if (!c.hasMark()) c.mark();
            moveCaretPageDown(c);
        }
        private void moveCaretPageUp(Caret caret) {
            scrollToCaret();
            Loc loc = posToLocInParent(caret.row, caret.col);
            scrollPrev(screenLineSize - 1);
            click(loc.x(), loc.y());
        }
        private void moveCaretPageDown(Caret caret) {
            scrollToCaret();
            Loc loc = posToLocInParent(caret.row, caret.col);
            scrollNext(screenLineSize - 1);
            click(loc.x(), loc.y());
        }

        public void scrollToCaret() {
            Caret caret = carets.getFirst();
            Loc loc = posToLoc(caret.row, caret.col);
            if (loc.y() < 0) {
                scrollPrev((int) Math.ceil(Math.abs(loc.y()) / fm.getLineHeight()));
            } else if (loc.y() + fm.getLineHeight() * 2 > height) {
                scrollNext((int) Math.ceil((loc.y() + fm.getLineHeight() * 2 - height) / fm.getLineHeight()));
            }
        }

        @Override
        public void input(String text) {
            if (carets.size() == 1) {
                Caret caret = carets.getFirst();
                var pos = ed.insert(caret.row, caret.col, text);
                if (caret.row == pos.row()) {
                    refreshBufferAt(caret.row);
                } else {
                    refreshBufferRange(caret.row);
                }
                caret.at(pos.row(), pos.col());
            } else {
                Collections.sort(carets);
                var poss = ed.insert(carets.stream().map(c -> new Pos(c.row, c.col)).toList(), text);
                refreshBufferRange(poss.getFirst().row());
                for (int i = 0; i < poss.size(); i++) {
                    var pos = poss.get(i);
                    carets.get(i).at(pos.row(), pos.col());
                }
            }
            scrollToCaret();
        }

        @Override
        public void delete() {
            if (carets.size() == 1) {
                Caret caret = carets.getFirst();
                var del = ed.delete(caret.row, caret.col);
                if (!del.contains("\n")) {
                    refreshBufferAt(caret.row);
                } else {
                    refreshBufferRange(caret.row);
                }
            } else {
                Collections.sort(carets);
                var poss = ed.delete(carets.stream().map(c -> new Pos(c.row, c.col)).toList());
                refreshBufferRange(poss.getFirst().row());
                for (int i = 0; i < poss.size(); i++) {
                    var pos = poss.get(i);
                    carets.get(i).at(pos.row(), pos.col());
                }
            }
            scrollToCaret();
        }

        @Override
        public void backspace() {
            if (carets.size() == 1) {
                Caret caret = carets.getFirst();
                if (caret.isZero()) return;
                var pos = ed.backspace(caret.row, caret.col);
                if (caret.row == pos.row()) {
                    refreshBufferAt(caret.row);
                } else {
                    refreshBufferRange(pos.row());
                }
                caret.at(pos.row(), pos.col());
            } else {
                Collections.sort(carets);
                var poss = ed.backspace(carets.stream().map(c -> new Pos(c.row, c.col)).toList());
                refreshBufferRange(poss.getFirst().row());
                for (int i = 0; i < poss.size(); i++) {
                    var pos = poss.get(i);
                    carets.get(i).at(pos.row(), pos.col());
                }
            }
            scrollToCaret();
        }

        @Override
        public void undo() {
            var newCarets = ed.undo().stream().map(p-> new Caret(p.row(), p.col())).toList();
            if (!newCarets.isEmpty()) {
                carets.clear();
                carets.addAll(newCarets);
                scrollToCaret();
                refreshBufferRange(carets.getFirst().row);
            }
        }
        @Override
        public void redo() {
            var newCarets = ed.redo().stream().map(p-> new Caret(p.row(), p.col())).toList();
            if (!newCarets.isEmpty()) {
                carets.clear();
                carets.addAll(newCarets);
                scrollToCaret();
                refreshBufferRange(carets.getFirst().row);
            }
        }

        @Override
        public void clickDouble(double x, double y) { /* Not yet implemented. */ }
        @Override
        public void clickTriple(double x, double y) { /* Not yet implemented. */ }
        @Override
        public void moveDragged(double x, double y) {}
        @Override
        public Loc imeOn() {
            Loc top = posToLoc(carets.getFirst().row, carets.getFirst().col);
            return new Loc(top.x(), top.y() + fm.getLineHeight() + 5);
        }
        @Override
        public void imeOff() {
            imeFlash.clear();
            carets.forEach(c -> refreshBufferAt(c.row));
        }
        @Override
        public boolean isImeOn() {
            return !imeFlash.isEmpty();
        }
        @Override
        public void inputImeComposed(String text) {
            if (imeFlash.isEmpty()) {
                carets.forEach(c -> imeFlash.on(c.row, c.col));
            }
            imeFlash.composed(text);
            carets.forEach(c -> refreshBufferAt(c.row));
        }

        protected void refreshBufferAt(List<Caret> carets) {
            carets.stream().mapToInt(c -> c.row).distinct().forEach(this::refreshBufferAt);
        }
        protected abstract void refreshBufferAt(int row);
        protected abstract void refreshBufferRange(int fromRow);
        protected abstract void refreshBufferRange(int row, int nRow);

        protected TextRow textRowAt(int row) {
            return createRow(row);
        }
        protected TextRow createStyledRow(int row) {
            return rowDecorator.apply(createRow(row));
        }
        protected TextRow createRow(int row) {
            return imeFlash.apply(row, ed.getText(row),
                    text -> new TextRow(row, text, advances(text, fm), fm.getLineHeight()));
        }

        protected abstract Loc posToLoc(int row, int col);
        protected abstract Loc posToLocInParent(int row, int col);
        protected abstract Pos locToPos(double x, double y);
        protected int screenLineSize(double h) {
            return (int) Math.ceil(Math.max(0, h - MARGIN_TOP) / fm.getLineHeight());
        }
        @Override
        public void pasteFromClipboard() {
            var clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            var text = clipboard.hasString() ? clipboard.getString() : "";
            if (text == null || text.isEmpty()) {
                return;
            }
            input(text);
        }
        @Override
        public void copyToClipboard() {
            String copy = carets.stream().sorted().filter(Caret::hasMark)
                    .map(c -> ed.getText(c.markedMin(), c.markedMax()))
                    .flatMap(Collection::stream)
                    .collect(Collectors.joining());
            javafx.scene.input.Clipboard.getSystemClipboard()
                    .setContent(Map.of(DataFormat.PLAIN_TEXT, copy));
        }
        @Override
        public void cutToClipboard() {
            List<Caret> select = carets.stream().sorted().filter(Caret::hasMark).toList();
            List<String> texts = select.stream()
                    .map(c -> String.join("", ed.getText(c.markedMin(), c.markedMax())))
                    .toList();
            for (int i = 0; i < select.size(); i++) {
                Caret c = select.get(i);
                ed.delete(c.markedMin().row(), c.markedMin().col(), texts.get(i).length());
                // TODO transaction delete
            }
            javafx.scene.input.Clipboard.getSystemClipboard()
                    .setContent(Map.of(DataFormat.PLAIN_TEXT, String.join("", texts)));
        }
        @Override
        public Path path() { return ed.path(); }
        @Override
        public void save(Path path) { ed.save(path); }
        public int screenLineSize() { return screenLineSize; }
    }

    /**
     * PlainScreenText.
     */
    class PlainScreenText extends AbstractScreenText {
        private double maxRowWidth = 0;
        private double xShift = 0;
        private final List<TextRow> buffer = new ArrayList<>();

        public PlainScreenText(Document doc, FontMetrics fm, Syntax syntax) {
            super(TextEdit.of(doc), fm, syntax);
        }

        @Override
        public void draw(Draw draw) {
            draw.clear();
            if (buffer.isEmpty()) return;

            for (Caret caret : carets) {
                if (!caret.hasMark()) continue;
                Loc mLoc = posToLoc(caret.markedRow, caret.markedCol);
                Loc cLoc = posToLoc(caret.row, caret.col);
                draw.fillSelection(
                        mLoc.x() + MARGIN_LEFT - xShift, mLoc.y() + MARGIN_TOP,
                        cLoc.x() + MARGIN_LEFT - xShift, cLoc.y() + MARGIN_TOP,
                        MARGIN_LEFT, width);
            }

            double y = 0;
            maxRowWidth = 0;
            for (TextRow row : buffer) {
                double x = 0;
                for (StyledText st : row.styledTexts()) {
                    draw.text(st.text(), x + MARGIN_LEFT - xShift, y + MARGIN_TOP, st.width(), st.styles());
                    x += st.width();
                }
                y += row.lineHeight;
                maxRowWidth = Math.max(maxRowWidth, row.width);
            }
            for (Caret caret : carets) {
                if (buffer.getFirst().row <= caret.row && caret.row <= buffer.getLast().row) {
                    double x = colToX(caret.row, caret.col + imeFlash.composedText().length());
                    caret.vPos = (caret.vPos < 0) ? x : caret.vPos;
                    draw.caret(
                            (imeFlash.composedText().isEmpty() ? Math.min(x, caret.vPos) : x) + MARGIN_LEFT - xShift,
                            rowToY(caret.row) + MARGIN_TOP);
                }
            }
        }

        @Override
        public void setSize(double width, double height) {
            if (width <= 0 || height <= 0) return;
            int newScreenLineSize = screenLineSize(height);
            if (this.height > height) {
                // shrink height
                int fromIndex = newScreenLineSize + 1;
                if (fromIndex < buffer.size() - 1) {
                    buffer.subList(fromIndex, buffer.size()).clear();
                }
            } else if (this.height < height) {
                // grow height
                int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
                for (int i = buffer.size(); i <= newScreenLineSize && i < ed.rows(); i++) {
                    buffer.add(createStyledRow(top + i));
                }
            }
            this.width = width;
            this.height = height;
            this.screenLineSize = newScreenLineSize;
        }

        @Override
        public int getScrollableMaxLine() { return (int) Math.max(0, ed.rows() - screenLineSize * 0.6); }
        @Override
        public int getScrolledLineValue() { return buffer.isEmpty() ? 0 : buffer.getFirst().row; }
        @Override
        public double getScrollableMaxX() {
            return  (maxRowWidth > width) ? Math.max(0, maxRowWidth + MARGIN_LEFT - width * 0.6) : 0;
        }
        @Override
        public double getScrolledXValue() { return xShift; }
        @Override
        public void scrollX(double x) { xShift = x; }

        @Override
        public void scrollNext(int delta) {
            assert delta > 0;
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            int maxTop = getScrollableMaxLine();
            if (top + delta >= maxTop) {
                delta = maxTop - top;
            }

            if (delta == 0) return;
            if (delta >= screenLineSize) {
                scrollAt(top + delta);
                return;
            }

            int next = buffer.isEmpty() ? 0 : buffer.getLast().row + 1;
            buffer.subList(0, Math.min(delta, buffer.size())).clear();
            for (int i = next; i < (next + delta) && i < ed.rows(); i++) {
                buffer.add(createStyledRow(i));
            }
        }

        @Override
        public void scrollPrev(int delta) {
            assert delta > 0;
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            delta = Math.clamp(delta, 0, top);

            if (delta == 0) return;
            if (delta >= screenLineSize) {
                scrollAt(top - delta);
                return;
            }

            if (buffer.size() >= screenLineSize) {
                buffer.subList(Math.max(0, buffer.size() - delta), buffer.size()).clear();
            }
            for (int i = 1; i <= delta; i++) {
                buffer.addFirst(createStyledRow(top - i));
            }
        }

        @Override
        public void scrollAt(int row) {
            row = Math.clamp(row, 0, getScrollableMaxLine());
            buffer.clear();
            for (int i = row; i < ed.rows(); i++) {
                buffer.add(createStyledRow(i));
                if (buffer.size() >= screenLineSize) break;
            }
        }

        @Override
        protected void moveCaretDown(Caret caret) {
            if (caret.row == ed.rows()) return;
            caret.vPos = (caret.vPos < 0) ? colToX(caret.row, caret.col) : caret.vPos;
            caret.row++;
            caret.col = xToCol(caret.row, caret.vPos);
        }

        @Override
        protected void moveCaretUp(Caret caret) {
            if (caret.row == 0) return;
            caret.vPos = (caret.vPos < 0) ? colToX(caret.row, caret.col) : caret.vPos;
            caret.row--;
            caret.col = xToCol(caret.row, caret.vPos);
        }

        @Override
        public void click(double x, double y) {
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            Pos pos = locToPos(x + xShift, y + top * fm.getLineHeight());
            carets.clear();
            carets.add(new Caret(pos.row(), pos.col()));
        }

        @Override
        protected void refreshBufferRange(int fromRow) {
            int bufferIndex = bufferIndexOf(fromRow);
            for (int i = bufferIndex; i <= screenLineSize && i < ed.rows(); i++) {
                if (i >= buffer.size()) {
                    buffer.add(createStyledRow(fromRow++));
                } else {
                    buffer.set(i, createStyledRow(fromRow++));
                }
            }
        }
        @Override
        protected void refreshBufferAt(int row) {
            refreshBufferRange(row, 1);
        }
        @Override
        protected void refreshBufferRange(int row, int nRow) {
            assert row >= 0 && nRow > 0;
            for (int i = row; i < row + nRow; i++) {
                int bufferIndex = bufferIndexOf(i);
                if (bufferIndex >= 0) buffer.set(bufferIndex, createStyledRow(row));
            }
        }
        private int bufferIndexOf(int row) {
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            int index = row - top;
            return (0 <= index && index < buffer.size()) ? index : -1;
        }

        @Override
        protected Loc posToLoc(int row, int col) {
            return new Loc(colToX(row, col), rowToY(row));
        }
        @Override
        protected Loc posToLocInParent(int row, int col) {
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            double y = (row - top) * fm.getLineHeight() + MARGIN_TOP;
            return new Loc(colToX(row, col) - xShift + MARGIN_LEFT, y);
        }
        @Override
        protected Pos locToPos(double x, double y) {
            int row = yToRow(y);
            int col = xToCol(row, x);
            return new Pos(row, col);
        }
        private double rowToY(int row) {
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            return (row - top) * fm.getLineHeight();
        }
        private double colToX(int row, int col) {
            double[] advances = textRowAt(row).advances;
            double x = 0;
            for (int i = 0; i < advances.length && i < col; i++) {
                x += advances[i];
            }
            return x;
        }
        private int xToCol(int row, double x) {
            if (x <= 0) return 0;
            TextRow textRow = textRowAt(row);
            double[] advances = textRow.advances;
            for (int i = 0; i < advances.length; i++) {
                x -= advances[i];
                if (x < 0) return i;
            }
            return textRow.textLength();
        }
        private int yToRow(double y) {
            return Math.clamp((int) (Math.max(0, y - MARGIN_TOP) / fm.getLineHeight()), 0, ed.rows() - 1);
        }

        @Override
        protected TextRow textRowAt(int row) {
            if (buffer.isEmpty()) {
                return createRow(row);
            }
            var top = buffer.getFirst();
            if (top.row == row) return top;
            if (top.row <= row && row < top.row + buffer.size()) {
                return buffer.get(row - top.row);
            } else {
                return createRow(row);
            }
        }
    }

    /**
     * WrapScreenText
     */
    class WrapScreenText extends AbstractScreenText {
        private double wrap = 0;
        private int topLine = 0;
        private final List<TextLine> buffer = new ArrayList<>();
        private final List<RowMap> wrapLayout = new ArrayList<>();

        public WrapScreenText(Document doc, FontMetrics fm, Syntax syntax) {
            super(TextEdit.of(doc), fm, syntax);
        }

        @Override
        public void draw(Draw draw) {
            draw.clear();

            for (Caret caret : carets) {
                if (!caret.hasMark()) continue;
                Loc loc1 = posToLoc(caret.markedRow, caret.markedCol);
                Loc loc2 = posToLoc(caret.row, caret.col);
                draw.fillSelection(
                        loc1.x() + MARGIN_LEFT, loc1.y() + MARGIN_TOP,
                        loc2.x() + MARGIN_LEFT, loc2.y() + MARGIN_TOP,
                        MARGIN_LEFT, width);
            }

            double y = 0;
            for (TextLine line : buffer) {
                double x = 0;
                for (StyledText st : line.styledTexts()) {
                    draw.text(st.text(),
                            x + MARGIN_LEFT, y + MARGIN_TOP,
                            st.width(),
                            st.styles());
                    x += st.width();
                }
                y += line.lineHeight();
            }
            for (Caret caret : carets) {
                for (int i = 0; i < buffer.size(); i++) {
                    TextLine textLine = buffer.get(i);
                    if (textLine.contains(caret.row, caret.col)) {
                        double cy = i * textLine.lineHeight();
                        double cx = 0;
                        for (int j = textLine.map.fromIndex; j < textLine.map.toIndex && j < caret.col; j++) {
                            cx += textLine.parent.advances[j];
                        }
                        draw.caret(cx + MARGIN_LEFT  + fm.getAdvance(imeFlash.composedText()), cy + MARGIN_TOP);
                        break;
                    }
                }
            }
        }

        @Override
        public void setSize(double width, double height) {

            if (width <= 0 || height <= 0 ||
                    (this.width == width && this.height == height)) return;

            int newScreenLineSize = screenLineSize(height);

            if (this.width != width) {
                RowMap top = buffer.isEmpty() ? RowMap.empty : buffer.getFirst().map;
                this.wrap = width - MARGIN_LEFT - fm.getLineHeight() / 3;
                wrapLayout.clear();
                buffer.clear();
                for (int i = 0; i < ed.rows(); i++) {
                    for (TextLine line : createStyledRow(i).wrap(wrap)) {
                        wrapLayout.add(line.map);
                        if (top.row <= line.map.row) {
                            if (top.row == line.map.row && top.subLine > line.map.subLine) continue;
                            if (buffer.size() < newScreenLineSize) {
                                buffer.add(line);
                            }
                        }
                    }
                }
            } else {
                if (this.height > height) {
                    int fromIndex = newScreenLineSize + 1;
                    if (fromIndex < buffer.size() - 1) {
                        buffer.subList(fromIndex, buffer.size()).clear();
                    }
                } else if (this.height < height) {
                    RowMap bottom = buffer.isEmpty() ? RowMap.empty : buffer.getLast().map;
                    for (int i = bottom.row; i < ed.rows(); i++) {
                        for (TextLine line : createStyledRow(i).wrap(wrap)) {
                            if (bottom.row == line.map.row && bottom.subLine <= line.map.subLine) continue;
                            buffer.add(line);
                        }
                    }
                }
            }
            this.width = width;
            this.height = height;
            this.screenLineSize = newScreenLineSize;
        }

        @Override
        public int getScrollableMaxLine() {
            return (int) Math.max(0, wrapLayout.size() - screenLineSize * 0.6);
        }

        @Override
        public int getScrolledLineValue() {
            return topLine;
        }

        @Override
        public double getScrollableMaxX() { return 0; }

        @Override
        public double getScrolledXValue() { return 0; }

        @Override
        public void scrollX(double x) {
            // nothing to do
        }

        @Override
        public void scrollNext(int delta) {
            int maxTop = getScrollableMaxLine();
            if (topLine + delta >= maxTop) {
                delta = maxTop - topLine;
            }
            scrollAt(topLine + delta);
        }

        @Override
        public void scrollPrev(int delta) {
            scrollAt(topLine - delta);
        }

        @Override
        public void scrollAt(int line) {
            topLine = Math.clamp(line, 0, getScrollableMaxLine());
            buffer.clear();
            RowMap map = wrapLayout.get(topLine);
            for (int i = map.row; i < ed.rows(); i++) {
                List<TextLine> lines = createStyledRow(i).wrap(wrap);
                int start = (i == map.row) ? map.subLine : 0;
                for (int j = start; j < lines.size(); j++) {
                    buffer.add(lines.get(j));
                    if (buffer.size() >= screenLineSize) break;
                }
            }
        }

        @Override
        public void click(double x, double y) {
            Pos pos = locToPos(x, y + topLine * fm.getLineHeight());
            carets.clear();
            carets.add(new Caret(pos.row(), pos.col()));
        }

        @Override
        protected void moveCaretDown(Caret caret) {
            Loc loc = posToLoc(caret.row, caret.col);
            caret.vPos = (caret.vPos < 0) ? loc.x() : caret.vPos;
            Pos pos = locToPos(caret.vPos, loc.y() + fm.getLineHeight());
            caret.row = pos.row();
            caret.col = pos.col();
        }

        @Override
        protected void moveCaretUp(Caret caret) {
            Loc loc = posToLoc(caret.row, caret.col);
            caret.vPos = (caret.vPos < 0) ? loc.x() : caret.vPos;
            Pos pos = locToPos(caret.vPos,  loc.y() - fm.getLineHeight());
            caret.row = pos.row();
            caret.col = pos.col();
        }

        @Override
        protected void refreshBufferRange(int fromRow) {
            refreshBufferRange(fromRow, buffer.getLast().row() - fromRow + 1);
        }
        @Override
        protected void refreshBufferAt(int row) { refreshBufferRange(row, 1); }
        @Override
        protected void refreshBufferRange(int row, int nRow) {
            assert row >= 0 && nRow > 0;
            List<TextLine> lines = new ArrayList<>();
            for (int i = row; i < row + nRow; i++) {
                lines.addAll(createStyledRow(i).wrap(wrap));
            }
            int[] bufferIndex = bufferIndexOf(row, nRow);
            if (bufferIndex.length > 1) {
                buffer.subList(bufferIndex[0], bufferIndex[1]).clear();
                buffer.addAll(bufferIndex[0], lines);
                clampBuffer();
                wrapLayout.subList(topLine + bufferIndex[0], topLine + bufferIndex[1]).clear();
                wrapLayout.addAll(topLine + bufferIndex[0], lines.stream().map(l -> l.map).toList());
            } else {
                int[] wrapIndex = wrapLayoutIndexOf(row, nRow);
                wrapLayout.subList(wrapIndex[0], wrapIndex[1]).clear();
                wrapLayout.addAll(wrapIndex[0], lines.stream().map(l -> l.map).toList());
            }
        }

        private int[] bufferIndexOf(int row, int nLine) {
            int from = -1, to = -1;
            for (int i = 0; i < buffer.size(); i++) {
                TextLine line = buffer.get(i);
                if (line.map.row == row && from < 0) {
                    from = i;
                } else if (line.map.row > (row + nLine - 1)) {
                    to = i;
                    break;
                }
            }
            if (to < 0) to = buffer.size();
            return (from < 0) ? new int[0] : new int[] { from, to };
        }

        private int[] wrapLayoutIndexOf(int row, int len) {
            int from = Collections.binarySearch(wrapLayout, new RowMap(row, 0, 0, 0), Comparator.comparing(RowMap::row));
            if (from < 0) return new int[0];
            int to = -1;
            for (int i = from; i < wrapLayout.size(); i++) {
                RowMap map = wrapLayout.get(i);
                if (map.row > (row + len - 1)) {
                    to = i;
                    break;
                }
            }
            if (to < 0) to = wrapLayout.size();
            return new int[] { from, to };
        }

        private void clampBuffer() {
            int screenLineSize = screenLineSize(height);
            if (buffer.size() > screenLineSize) {
                buffer.subList(screenLineSize, buffer.size()).clear();
            }
        }

        @Override
        protected Loc posToLoc(int row, int col) {
            Indexed<TextLine> line = posToLine(row, col);
            double y = (line.index() - topLine) * fm.getLineHeight();
            double x = 0;
            TextLine textLine = line.value();
            for (int j = textLine.map.fromIndex; j < textLine.map.toIndex && j < col; j++) {
                x += textLine.parent.advances[j];
            }
            return new Loc(x, y);
        }

        @Override
        protected Loc posToLocInParent(int row, int col) {
            Loc loc = posToLoc(row, col);
            return new Loc(loc.x() + MARGIN_LEFT, loc.y() - topLine * fm.getLineHeight() + MARGIN_TOP);
        }

        private Indexed<TextLine> posToLine(int row, int col) {
            // calc loc from screen buffer
            for (int i = 0; i < buffer.size(); i++) {
                TextLine textLine = buffer.get(i);
                if (textLine.contains(row, col)) {
                    return new Indexed<>(topLine + i, textLine);
                }
            }
            // calc loc from wrapLayout
            for (int i = 0; i < wrapLayout.size(); i++) {
                RowMap map = wrapLayout.get(i);
                if (map.contains(row, col)) {
                    return new Indexed<>(i, createRow(map.row).wrap(wrap).get(map.subLine));
                }
            }
            return new Indexed<>(wrapLayout.size(), createRow(ed.rows()).wrap(wrap).getLast());
        }

        @Override
        protected Pos locToPos(double x, double y) {
            int layoutIndex = Math.clamp(topLine + (int) (y / fm.getLineHeight()), 0, wrapLayout.size() - 1);
            RowMap map = wrapLayout.get(layoutIndex);
            TextLine textLine = posToLine(map.row, map.fromIndex).value();
            int col = textLine.map.fromIndex;
            for (int i = 0; i < textLine.textLength(); i++) {
                if (x <= 0) break;
                x -= textLine.parent.advances[col++];
            }
            return new Pos(map.row, col);
        }
    }

    record RowMap(int row, int subLine, int fromIndex, int toIndex) {
        static RowMap empty = new RowMap(0, 0, 0, 0);
        int length() { return toIndex - fromIndex; }
        boolean contains(int row, int col) {
            return this.row == row && this.fromIndex <= col && col < this.toIndex;
        }
    }

    class TextRow {
        int row;
        String text;
        double[] advances;
        Styles styles;
        float lineHeight;
        double width;
        public TextRow(int row, String text, double[] advances, float lineHeight) {
            this.row = row;
            this.text = text;
            this.advances = advances;
            this.styles = new Styles();
            this.lineHeight = lineHeight;
            this.width = Arrays.stream(advances).sum();
        }
        private List<TextLine> wrap(double width) {
            if (width <= 0) {
                return List.of(new TextLine(this,
                        new RowMap(row, 0, 0, text.length())));
            }
            double w = 0;
            int fromIndex = 0;
            List<TextLine> wrapped = new ArrayList<>();
            for (int i = 0; i < text.length(); i++) {
                double advance = advances[i];
                if (advance <= 0) continue;
                if (w + advance > width) {
                    wrapped.add(new TextLine(this,
                            new RowMap(row, wrapped.size(), fromIndex, i)));
                    w = 0;
                    fromIndex = i;
                }
                w += advance;
            }
            wrapped.add(new TextLine(this,
                    new RowMap(row, wrapped.size(), fromIndex, text.length())));
            return wrapped;
        }

        List<StyledText> styledTexts() {
            return styles.apply(text, advances);
        }
        boolean isSurrogate(int index) { return Character.isSurrogate(text.charAt(index)); }
        boolean isHighSurrogate(int index) { return Character.isHighSurrogate(text.charAt(index)); }
        boolean isLowSurrogate(int index) { return Character.isLowSurrogate(text.charAt(index)); }
        int length() { return text.length(); }
        int textLength() {
            if (text.length() >= 2 && text.charAt(text.length() - 2) == '\r' && text.charAt(text.length() - 1) == '\n') {
                return text.length() - 2;
            }
            if (!text.isEmpty() && text.charAt(text.length() - 1) == '\n') {
                return text.length() - 1;
            }
            return text.length();
        }
    }

    class TextLine {
        TextRow parent;
        RowMap map;
        public TextLine(TextRow parent, RowMap map) {
            this.parent = parent;
            this.map = map;
        }
        int row() { return map.row; }
        int subLine() { return map.subLine; }
        float lineHeight() { return parent.lineHeight; }
        String text() { return parent.text.substring(map.fromIndex, map.toIndex); }
        List<StyledText> styledTexts() { return parent.styles.apply(map.fromIndex, map.toIndex, parent.text, parent.advances); }
        boolean isSurrogate(int index) { return parent.isSurrogate(map.fromIndex + index); }
        boolean isHighSurrogate(int index) { return parent.isHighSurrogate(map.fromIndex + index); }
        boolean isLowSurrogate(int index) { return parent.isLowSurrogate(map.fromIndex + index); }
        int length() { return map.length(); }
        boolean contains(int row, int col) { return map.contains(row, col); }
        int textLength() {
            if (map.length() >= 2 && parent.text.charAt(map.toIndex - 2) == '\r' && parent.text.charAt(map.toIndex - 1) == '\n') {
                return map.length() - 2;
            }
            if (map.length() >= 1 && parent.text.charAt(map.toIndex - 1) == '\n') {
                return map.length() - 1;
            }
            return map.length();
        }
    }

    private static double[] advances(String text, FontMetrics fm) {
        double[] advances = new double[text.length()];
        for (int i = 0; i < text.length(); i++) {
            char ch1 = text.charAt(i);
            if (Character.isHighSurrogate(ch1)) {
                advances[i] = fm.getAdvance(ch1, text.charAt(i + 1));
                i++;
            } else if (Character.isISOControl(ch1)) {
                i++;
            } else if (ch1 == '\t') {
                advances[i] = fm.getAdvance(" ".repeat(TAB_SIZE));
            } else {
                advances[i] = fm.getAdvance(ch1);
            }
        }
        return advances;
    }

    private static int countLines(CharSequence text) {
        return 1 + (int) text.codePoints().filter(c -> c == '\n').count();
    }

    class Caret implements Comparable<Caret> {
        int row = 0, col = 0;
        double vPos = 0; // not contains margin
        int markedRow = -1, markedCol = -1;
        Caret(int row, int col) { this.row = row; this.col = col; this.vPos = -1; }
        public void at(int row, int col) { this.row = row; this.col = col; this.vPos = -1; }
        public void mark(int row, int col) { markedRow = row; markedCol = col; }
        public void mark() { markedRow = row; markedCol = col; }
        public void clearMark() { markedRow = -1; markedCol = -1; }
        public boolean isZero() { return row == 0 && col == 0; }
        public boolean hasMark() { return markedRow >= 0 && markedCol >= 0 && !(row == markedRow && col == markedCol); }
        public boolean isMarkForward() { return hasMark() && ((row == markedRow && col > markedCol) || (row > markedRow)); }
        public boolean isMarkBackward() { return hasMark() && !isMarkForward(); }
        public Pos markedMin() { return isMarkForward() ? new Pos(markedRow, markedCol) : new Pos(row, col); }
        public Pos markedMax() { return isMarkBackward() ? new Pos(markedRow, markedCol) : new Pos(row, col);}
        @Override public int compareTo(Caret that) {
            int c = Integer.compare(this.row, that.row);
            return c == 0 ? Integer.compare(this.col, that.col) : c;
        }
    }

}
