module code.editor {
    requires javafx.graphics;
    requires javafx.controls;
    requires com.mammb.code.piecetable;
    exports code.editor;
    exports code.editor.syntax;

    exports com.mammb.code.editor;
    exports com.mammb.code.editor.javafx;
}