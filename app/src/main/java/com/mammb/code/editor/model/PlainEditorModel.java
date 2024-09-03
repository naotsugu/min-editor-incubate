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
package com.mammb.code.editor.model;

import com.mammb.code.editor.core.EditorModel;
import com.mammb.code.editor.core.CaretGroup;
import com.mammb.code.editor.core.Content;
import com.mammb.code.editor.core.Draw;
import com.mammb.code.editor.core.FontMetrics;
import com.mammb.code.editor.core.Layout;
import com.mammb.code.editor.core.ScreenBuffer;
import com.mammb.code.editor.core.text.RowText;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class PlainEditorModel implements EditorModel {
    private final Content content;
    private final Layout layout;
    private final ScreenBuffer<RowText> buffer = ScreenBuffer.of();
    private final CaretGroup carets = CaretGroup.of();

    public PlainEditorModel(FontMetrics fm) {
        this(null, fm);
    }

    public PlainEditorModel(Path path, FontMetrics fm) {
        this.content = Objects.isNull(path) ? Content.of() : Content.of(path);
        this.layout = Layout.of(fm);
    }

    @Override
    public void draw(Draw draw) {
        draw.clear();
        carets.marked().forEach(range -> {

        });
    }

    @Override
    public void setSize(double width, double height) {
        layout.setSize(width, height);
        int line = buffer.topLine();
        buffer.setCapacity(layout.lineSize());
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
