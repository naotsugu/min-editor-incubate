package com.mammb.code.editor.core.model;

import com.mammb.code.editor.core.CaretGroup;
import com.mammb.code.editor.core.Content;
import com.mammb.code.editor.core.Draw;
import com.mammb.code.editor.core.EditorModel;
import com.mammb.code.editor.core.FontMetrics;
import com.mammb.code.editor.core.layout.PlainTextLayout;
import com.mammb.code.editor.core.layout.TextLayout;
import com.mammb.code.editor.core.text.Text;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class PlainEditorModel implements EditorModel {
    private final Content content;
    private final FontMetrics fm;
    private final TextLayout layout;
    private final CaretGroup carets = CaretGroup.of();

    public PlainEditorModel(Content content, FontMetrics fm) {
        this.content = content;
        this.fm = fm;
        this.layout = new PlainTextLayout(content, fm);
    }

    @Override
    public void draw(Draw draw) {
        double y = 0;
        for (Text text : layout.viewBuffer()) {
            double x = 0;
            draw.text(text.value(), x, y, text.width(), List.of());
            x += text.width();
            y += text.height();
        }
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
        content.save(path);
    }

}
