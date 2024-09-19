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
package com.mammb.code.editor.core.syntax;

import java.util.Objects;

public class LexerSource {
    private String source;
    private int index = 0;
    private int

    private LexerSource(String source) {
        this.source = source;
    }

    public static LexerSource of(String source) {
        return new LexerSource(source);
    }


    public boolean hasNext() {
        return index < source.length();
    }

    public boolean match(char ch) {
        return source.charAt(index) == ch;
    }
    public boolean match(CharSequence cs) {
        return index + cs.length() < source.length() &&
                Objects.equals(source.substring(index, cs.length()), cs.toString());
    }


    public char next() {
        return source.charAt(index++);
    }

    public String peek() {

    }
}
