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

import com.mammb.code.editor.EditorModel;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class PlainEditorModel implements EditorModel {
    protected double width = 0, height = 0;
    private final Content content;
    private final ScreenBuffer<TextRow> buffer = ScreenBuffer.of();
    private final CaretGroup carets = CaretGroup.of();

    public PlainEditorModel(FontMetrics fm) {
        this(null, fm);
    }

    public PlainEditorModel(Path path, FontMetrics fm) {
        this.content = Objects.isNull(path) ? Content.of() : Content.of(path);
    }

    @Override
    public void draw(Draw draw) {
        draw.clear();
        carets.marked().forEach(range -> {

        });
    }

    @Override
    public void setSize(double width, double height) {
        if (width < 0 || height < 0) return;
        this.width = width;
        this.height = height;
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
