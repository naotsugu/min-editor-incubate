package com.mammb.code.editor.core.model;

import com.mammb.code.editor.core.Content;
import com.mammb.code.editor.core.Draw;
import com.mammb.code.editor.core.EditorModel;
import com.mammb.code.editor.core.FontMetrics;
import com.mammb.code.editor.core.layout.PlainTextLayout;
import com.mammb.code.editor.core.layout.TextLayout;
import java.nio.file.Path;
import java.util.Optional;

public class PlainEditorModel implements EditorModel {
    private final Content content;
    private final FontMetrics fm;
    private final TextLayout layout;

    public PlainEditorModel(Content content, FontMetrics fm) {
        this.content = content;
        this.fm = fm;
        this.layout = new PlainTextLayout(content, fm);
    }

    @Override
    public void draw(Draw draw) {

    }

    @Override
    public void setSize(double width, double height) {
        layout.setSize(width, height);
    }

    @Override
    public Optional<Path> path() {
        return content.path();
    }

    @Override
    public void save(Path path) {

    }
}
