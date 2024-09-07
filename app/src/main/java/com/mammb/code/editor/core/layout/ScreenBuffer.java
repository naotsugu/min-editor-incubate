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
package com.mammb.code.editor.core.layout;

import com.mammb.code.editor.core.Content;
import com.mammb.code.editor.core.FontMetrics;
import com.mammb.code.editor.core.text.Text;
import java.util.ArrayList;
import java.util.List;

public interface ScreenBuffer {

    void setSize(double width, double height);
    List<Text> texts();

    static ScreenBuffer of(Content content, FontMetrics fm) {
        Layout layout = new RowLayout(content, fm);
        return new BasicScreenBuffer(layout, content);
    }

    static ScreenBuffer wrapOf(Content content, FontMetrics fm) {
        Layout layout = new WrapLayout(content, fm);
        return new BasicScreenBuffer(layout, content);
    }

    class BasicScreenBuffer implements ScreenBuffer {
        private double width = 0, height = 0;
        private double xShift = 0;
        private int topLine = 0;
        private final List<Text> buffer = new ArrayList<>();
        private final Layout layout;
        private final Content content;

        public BasicScreenBuffer(Layout layout, Content content) {
            this.layout = layout;
            this.content = content;
        }

        @Override
        public void setSize(double width, double height) {
            layout.setWidth(width);
            this.width = width;
            this.height = height;
            fillBuffer();
        }

        @Override
        public List<Text> texts() {
            return buffer;
        }

        private void fillBuffer() {
            buffer.clear();
            for (int i = topLine; i < topLine + lineSize(); i++) {
                buffer.add(layout.text(i));
            }
        }

        public int lineSize() {
            return (int) Math.ceil(Math.max(0, height) / layout.lineHeight());
        }

    }
}
