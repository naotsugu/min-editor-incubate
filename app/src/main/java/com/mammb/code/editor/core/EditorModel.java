/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mammb.code.editor.core;

import com.mammb.code.editor.core.model.PlainEditorModel;
import java.nio.file.Path;
import java.util.Optional;

/**
 * The Facade of editor.
 */
public interface EditorModel {

    static EditorModel of(FontMetrics fm) {
        return new PlainEditorModel(Content.of(), fm);
    }
    static EditorModel of(Path path, FontMetrics fm) {
        return new PlainEditorModel(Content.of(path), fm);
    }

    void draw(Draw draw);
    void setSize(double width, double height);

    void scrollNext(int delta);
    void scrollPrev(int delta);
    void scrollAt(int line);
    void moveCaretRight(boolean withSel);
    void moveCaretLeft(boolean withSel);
    void moveCaretDown(boolean withSel);
    void moveCaretUp(boolean withSel);
    void click(double x, double y);
    void clickDouble(double x, double y);
    void clickTriple(double x, double y);
    void moveDragged(double x, double y);

    void input(String text);
    void delete();
    void backspace();
    void undo();
    void redo();

    boolean isModified();
    Optional<Path> path();
    void save(Path path);
}
