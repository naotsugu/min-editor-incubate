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

import com.mammb.code.editor.core.syntax.Syntax;
import com.mammb.code.editor.core.text.Style;
import com.mammb.code.editor.core.text.Style.StyleSpan;
import com.mammb.code.editor.core.text.Text;
import java.util.List;

public interface Decorate {

    List<StyleSpan> apply(Text text);
    void add(List<Caret.Range> ranges, Style style);
    void clear();

    static Decorate of(Syntax syntax) {
        return new DecorateImpl(syntax);
    }

    class DecorateImpl implements Decorate {
        private final Syntax syntax;


        public DecorateImpl(Syntax syntax) {
            this.syntax = syntax;
        }

        @Override
        public List<StyleSpan> apply(Text text) {
            var spans = syntax.apply(text.row(), text.value());
            return spans;
        }

        @Override
        public void add(List<Caret.Range> ranges, Style style) {

        }

        @Override
        public void clear() {

        }

    }
}
