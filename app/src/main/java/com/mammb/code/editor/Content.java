package com.mammb.code.editor;

import com.mammb.code.piecetable.TextEdit;
import com.mammb.code.editor.Caret.Point;
import com.mammb.code.editor.Caret.PointRec;
import java.nio.file.Path;

public interface Content {

    Point insert(Point point, String text);
    String getText(Point start, Point end);

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
        public String getText(Point start, Point end) {
            return textEdit.getText(start.row(), start.col(), end.row(), end.col());
        }
    }
}
