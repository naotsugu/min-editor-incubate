package com.mammb.code.editor.model;

import com.mammb.code.editor.core.EditorModel;
import com.mammb.code.editor.core.Draw;
import java.nio.file.Path;
import java.util.Optional;

public class WrapEditorModel implements EditorModel {
    @Override
    public void draw(Draw draw) {

    }

    @Override
    public void setSize(double width, double height) {

    }

    @Override
    public Optional<Path> path() {
        return Optional.empty();
    }

    @Override
    public void save(Path path) {

    }
}
