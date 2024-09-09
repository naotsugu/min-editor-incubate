package com.mammb.code.editor.core;

import com.mammb.code.piecetable.TextEdit;
import com.mammb.code.piecetable.TextEdit.Pos;
import com.mammb.code.editor.core.Caret.Point;
import com.mammb.code.editor.core.Caret.PointRec;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface Content {

    Point insert(Point point, String text);

    /**
     * Undo.
     * @return the undo position
     */
    List<Point> undo();

    /**
     * Redo.
     * @return the redo position
     */
    List<Point> redo();


    String getText(int row);
    String getText(Point start, Point end);
    int rows();
    Optional<Path> path();
    void save(Path path);

    static Content of() {
        return new ContentImpl();
    }
    static Content of(Path path) {
        return new ContentImpl(path);
    }

    class ContentImpl implements Content {
        private final TextEdit textEdit;
        public ContentImpl() {
            this.textEdit = TextEdit.of();
        }
        public ContentImpl(Path path) {
            this.textEdit = TextEdit.of(path);
        }

        @Override
        public Point insert(Point point, String text) {
            var p = textEdit.insert(point.row(), point.col(), text);
            return new PointRec(p.row(), p.col());
        }

        @Override
        public List<Point> undo() {
            return textEdit.undo().stream().map(p -> Point.of(p.row(), p.col())).toList();
        }

        @Override
        public List<Point> redo() {
            return textEdit.redo().stream().map(p -> Point.of(p.row(), p.col())).toList();
        }

        @Override
        public String getText(int row) {
            return textEdit.getText(row);
        }

        @Override
        public String getText(Point start, Point end) {
            return textEdit.getText(start.row(), start.col(), end.row(), end.col());
        }

        @Override
        public int rows() {
            return textEdit.rows();
        }

        @Override
        public Optional<Path> path() {
            return Optional.ofNullable(textEdit.path());
        }

        @Override
        public void save(Path path) {
            textEdit.save(path);
        }
    }
}
