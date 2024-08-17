package code.editor;

import code.editor.Lang.*;
import code.editor.syntax.Syntax;
import com.mammb.code.piecetable.Document;
import com.mammb.code.piecetable.TextEdit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import code.editor.Style.*;

public interface ScreenText {

    int MARGIN_TOP = 5;
    int MARGIN_LEFT = 5;
    int TAB_SIZE = 4;

    void draw(Draw draw);
    void setSize(double width, double height);
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
    void input(String text);
    void delete();
    void backspace();
    void undo();
    void redo();
    void click(double x, double y);
    void clickDouble(double x, double y);
    void clickTriple(double x, double y);
    Loc imeOn();
    void imeOff();
    boolean isImeOn();
    void imeComposedInput(String text);

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
        }
        @Override
        public void moveCaretSelectRight() {
            carets.forEach(c -> { if (!c.isMarked()) c.mark(); moveCaretRight(c); });
        }
        private void moveCaretRight(Caret caret) {
            caret.vPos = -1;
            // TODO skip if eof
            TextRow row = textRowAt(caret.row);
            caret.col += row.isHighSurrogate(caret.col) ? 2 : 1;
            if (caret.col > row.textLength()) {
                caret.col = 0;
                caret.row = Math.min(caret.row + 1, ed.rows());
            }
        }
        @Override
        public void moveCaretLeft() {
            carets.forEach(c -> { c.clearMark(); moveCaretLeft(c); });
        }
        @Override
        public void moveCaretSelectLeft() {
            carets.forEach(c -> { if (!c.isMarked()) c.mark(); moveCaretLeft(c); });
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
        }
        @Override
        public void moveCaretSelectDown() {
            carets.forEach(c -> { if (!c.isMarked()) c.mark();  moveCaretDown(c); });
        }
        protected abstract void moveCaretDown(Caret caret);
        @Override
        public void moveCaretUp() {
            carets.forEach(c -> { c.clearMark(); moveCaretUp(c); });
        }
        @Override
        public void moveCaretSelectUp() {
            carets.forEach(c -> {if (!c.isMarked()) c.mark();  moveCaretUp(c); });
        }
        protected abstract void moveCaretUp(Caret caret);
        @Override
        public void moveCaretHome() { carets.forEach(c -> { c.clearMark(); moveCaretHome(c); }); }
        @Override
        public void moveCaretSelectHome() { carets.forEach(c -> { if (!c.isMarked()) c.mark(); moveCaretHome(c); }); }
        private void moveCaretHome(Caret caret) { caret.vPos = -1; caret.col = 0; }
        @Override
        public void moveCaretEnd() { carets.forEach(c -> { c.clearMark(); moveCaretEnd(c); }); }
        @Override
        public void moveCaretSelectEnd() { carets.forEach(c -> { if (!c.isMarked()) c.mark(); moveCaretEnd(c); }); }
        private void moveCaretEnd(Caret caret) { caret.vPos = -1; caret.col = textRowAt(caret.row).textLength(); }

        @Override
        public abstract void input(String text);
        @Override
        public abstract void delete();
        @Override
        public abstract void backspace();
        @Override
        public void undo() {
            carets.clear();
            ed.undo().forEach(p -> carets.add(new Caret(p.row(), p.col())));
            refreshBuffer(carets);
        }
        @Override
        public void redo() {
            carets.clear();
            ed.redo().forEach(p -> carets.add(new Caret(p.row(), p.col())));
            refreshBuffer(carets);
        }

        @Override
        public void click(double x, double y) {
            Pos pos = locToPos(x, y);
            carets.clear();
            carets.add(new Caret(pos.row(), pos.col()));
        }
        @Override
        public void clickDouble(double x, double y) { /* Not yet implemented. */ }
        @Override
        public void clickTriple(double x, double y) { /* Not yet implemented. */ }

        @Override
        public Loc imeOn() {
            Loc top = posToLoc(carets.getFirst().row, carets.getFirst().col);
            return new Loc(top.x(), top.y() + fm.getLineHeight() + 5);
        }
        @Override
        public void imeOff() {
            imeFlash.clear();
            carets.forEach(c -> refreshBuffer(c.row));
        }
        @Override
        public boolean isImeOn() {
            return !imeFlash.isEmpty();
        }
        @Override
        public void imeComposedInput(String text) {
            if (imeFlash.isEmpty()) {
                carets.forEach(c -> imeFlash.on(c.row, c.col));
            }
            imeFlash.composed(text);
            carets.forEach(c -> refreshBuffer(c.row));
        }

        protected void refreshBuffer(List<Caret> carets) {
            carets.stream().mapToInt(c -> c.row).distinct().forEach(this::refreshBuffer);
        }
        protected abstract void refreshBuffer(int row);
        protected abstract void refreshBuffer(int row, int nRow);

        protected TextRow textRowAt(int row) {
            return createStyledRow(row);
        }
        protected TextRow createStyledRow(int row) {
            return rowDecorator.apply(createRow(row));
        }
        protected TextRow createRow(int row) {
            return imeFlash.apply(row, ed.getText(row),
                    text -> new TextRow(row, text, advances(text, fm), fm.getLineHeight()));
        }

        protected abstract Loc posToLoc(int row, int col);
        protected abstract Pos locToPos(double x, double y);
        protected int screenLineSize(double h) {
            return (int) Math.ceil(Math.max(0, h - MARGIN_TOP) / fm.getLineHeight());
        }
    }

    /**
     * PlainScreenText.
     */
    class PlainScreenText extends AbstractScreenText {
        private double width = 0, height = 0;
        private int screenLineSize = 0;
        private final List<TextRow> buffer = new ArrayList<>();

        public PlainScreenText(Document doc, FontMetrics fm, Syntax syntax) {
            super(TextEdit.of(doc), fm, syntax);
        }

        @Override
        public void draw(Draw draw) {
            draw.clear();
            if (buffer.isEmpty()) return;

            for (Caret caret : carets) {
                if (!caret.isMarked()) continue;
                Loc loc1 = posToLoc(caret.markedRow, caret.markedCol);
                Loc loc2 = posToLoc(caret.row, caret.col);
                draw.fillSelection(loc1.x() + MARGIN_LEFT, loc1.y() + MARGIN_TOP,
                        loc2.x() + MARGIN_LEFT, loc2.y() + MARGIN_TOP,
                        MARGIN_LEFT, width);
            }

            double y = 0;
            for (TextRow row : buffer) {
                double x = 0;
                for (StyledText st : row.styledTexts()) {
                    draw.text(st.text(), x + MARGIN_LEFT, y + MARGIN_TOP, st.width(), st.styles());
                    x += st.width();
                }
                y += row.lineHeight;
            }
            for (Caret caret : carets) {
                if (buffer.getFirst().row <= caret.row && caret.row <= buffer.getLast().row) {
                    double x = colToX(caret.row, caret.col + imeFlash.composedText().length());
                    caret.vPos = (caret.vPos < 0) ? x : caret.vPos;
                    draw.caret(
                            (imeFlash.composedText().isEmpty() ? Math.min(x, caret.vPos) : x) + MARGIN_LEFT,
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
        public void scrollNext(int delta) {
            assert delta > 0;

            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            int maxTop = (int) (ed.rows() - screenLineSize * 0.6);
            if (top + delta >= maxTop) {
                delta = maxTop - top;
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
            if (buffer.size() >= screenLineSize) {
                buffer.subList(buffer.size() - delta, buffer.size()).clear();
            }
            for (int i = 1; i <= delta; i++) {
                buffer.addFirst(createStyledRow(top - i));
            }
        }

        @Override
        public void scrollAt(int row) {
            row = Math.clamp(row, 0, ed.rows() - 1);
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
        public void input(String text) {
            for (Caret caret : carets) {
                // TODO multi line insert
                var pos = ed.insert(caret.row, caret.col, text);
                refreshBuffer(caret.row, pos.row() - caret.row + 1);
                caret.at(pos.row(), pos.col());
            }
        }

        @Override
        public void delete() {
            for (Caret caret : carets) {
                caret.vPos = -1;
                char ch = ed.getText(caret.row).charAt(caret.col);
                int len = (Character.isHighSurrogate(ch) || ch == '\r') ? 2 : 1;
                ed.delete(caret.row, caret.col, len);
                if (ch == '\r' || ch == '\n') {
                    refreshBufferAll();
                } else {
                    refreshBuffer(caret.row);
                }
            }
        }

        @Override
        public void backspace() {
            for (Caret caret : carets) {
                caret.vPos = -1;
                if (caret.isZero()) continue;
                if (caret.col == 0) {
                    moveCaretLeft();
                    delete();
                } else {
                    int len = Character.isLowSurrogate(ed.getText(caret.row).charAt(caret.col - 1)) ? 2 : 1;
                    caret.col -= len;
                    ed.delete(caret.row, caret.col, len);
                    refreshBuffer(caret.row);
                }
            }
        }

        private void refreshBufferAll() {
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            buffer.clear();
            for (int i = 0; i <= screenLineSize && i < ed.rows(); i++) {
                buffer.add(createStyledRow(top + i));
            }
        }
        @Override
        protected void refreshBuffer(int row) {
            refreshBuffer(row, 1);
        }
        @Override
        protected void refreshBuffer(int row, int nRow) {
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
            float[] advances = textRowAt(row).advances;
            double x = 0;
            for (int i = 0; i < advances.length && i < col; i++) {
                x += advances[i];
            }
            return x;
        }

        private int xToCol(int row, double x) {
            if (x <= 0) return 0;
            TextRow textRow = textRowAt(row);
            float[] advances = textRow.advances;
            for (int i = 0; i < advances.length; i++) {
                x -= advances[i];
                if (x < 0) return i;
            }
            return textRow.textLength();
        }
        private int yToRow(double y) {
            return (int) (Math.max(0, y - MARGIN_TOP) / fm.getLineHeight());
        }

        @Override
        protected TextRow textRowAt(int row) {
            if (!buffer.isEmpty()) {
                return createStyledRow(row);
            }
            var top = buffer.getFirst();
            if (top.row == row) return top;
            if (top.row <= row && row < top.row + buffer.size()) {
                return buffer.get(row - top.row);
            } else {
                return createStyledRow(row);
            }
        }
    }

    /**
     * WrapScreenText
     */
    class WrapScreenText extends AbstractScreenText {
        private double width = 0, height = 0;
        private double wrap = 0;
        private int topLine = 0;
        private int screenLineSize = 0;
        private final List<TextLine> buffer = new ArrayList<>();
        private final List<RowMap> wrapLayout = new ArrayList<>();

        public WrapScreenText(Document doc, FontMetrics fm, Syntax syntax) {
            super(TextEdit.of(doc), fm, syntax);
        }

        @Override
        public void draw(Draw draw) {
            draw.clear();

            for (Caret caret : carets) {
                if (!caret.isMarked()) continue;
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
        public void scrollNext(int delta) {
            int maxTop = (int) (wrapLayout.size() - screenLineSize * 0.6);
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
            topLine = Math.clamp(line, 0, wrapLayout.size());
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
        public void input(String text) {
            for (Caret caret : carets) {
                caret.vPos = -1;
                ed.insert(caret.row, caret.col, text);
                refreshBuffer(caret.row, countLines(text));
                caret.col += text.length();
            }
        }

        @Override
        public void delete() {
            for (Caret caret : carets) {
                caret.vPos = -1;
                delete(caret.row, caret.col,
                        Character.isHighSurrogate(ed.getText(caret.row).charAt(caret.col)) ? 2 : 1);
            }
        }

        private void delete(int row, int col, int len) {
            var delText = ed.delete(row, col, len);
            refreshBuffer(row, countLines(delText));
        }

        @Override
        public void backspace() {
            for (Caret caret : carets) {
                caret.vPos = -1;
                if (caret.isZero()) continue;
                moveCaretLeft();
                delete();
            }
        }

        @Override
        protected void refreshBuffer(int row) { refreshBuffer(row, 1); }
        @Override
        protected void refreshBuffer(int row, int nRow) {
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
            RowMap map = wrapLayout.get(topLine + (int) (y / fm.getLineHeight()));
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
        float[] advances;
        Styles styles;
        float lineHeight;
        public TextRow(int row, String text, float[] advances, float lineHeight) {
            this.row = row;
            this.text = text;
            this.advances = advances;
            this.styles = new Styles();
            this.lineHeight = lineHeight;
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
                float advance = advances[i];
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

    private static float[] advances(String text, FontMetrics fm) {
        float[] advances = new float[text.length()];
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
        public boolean isMarked() { return markedRow >= 0 && markedCol >= 0; }
        @Override public int compareTo(Caret that) {
            int c = Integer.compare(this.row, that.row);
            return c == 0 ? Integer.compare(this.col, that.col) : c;
        }
    }

}
