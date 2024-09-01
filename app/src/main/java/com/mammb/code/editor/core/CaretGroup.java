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

import com.mammb.code.editor.core.Caret.Range;
import java.util.ArrayList;
import java.util.List;

/**
 * The caret group.
 * @author Naotsugu Kobayashi
 */
public interface CaretGroup {

    List<Range> marked();

    static CaretGroup of() {
        return new CaretGroupImpl();
    }

    class CaretGroupImpl implements CaretGroup {
        private final List<Caret> carets = new ArrayList<>();

        public CaretGroupImpl() {
            carets.add(Caret.of());
        }

        @Override
        public List<Range> marked() {
            return carets.stream().filter(Caret::isMarked).map(Caret::markedRange).toList();
        }
    }
}
