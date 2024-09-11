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
package com.mammb.code.editor.core.text;

public interface Text {

    int row();

    String value();

    double[] advances();

    double width();

    double height();

    default int length() {
        return value().length();
    }

    default int textLength() {
        char ch1 = (value().length() > 1) ? value().charAt(value().length() - 1) : 0;
        char ch2 = (value().length() > 2) ? value().charAt(value().length() - 2) : 0;
        return value().length() - (
                (ch2 == '\r' && ch1  == '\n') ? 2 : (ch1  == '\n') ? 1 : 0
        );
    }

    default boolean isSurrogate(int index) {
        return Character.isSurrogate(value().charAt(index));
    }

    default boolean isHighSurrogate(int index) {
        return Character.isHighSurrogate(value().charAt(index));
    }

    default boolean isLowSurrogate(int index) {
        return Character.isLowSurrogate(value().charAt(index));
    }

    default int right(int index) {
        if (isEmpty()) return index;
        index += isHighSurrogate(index) ? 2 : 1;
        return  (index > textLength()) ? -1 : index;
    }

    default int left(int index) {
        if (index <= 0) return 0;
        index -= isLowSurrogate(index - 1) ? 2 : 1;
        return index;
    }

    default boolean isEmpty() {
        return value().isEmpty();
    }

}
