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
import java.util.Optional;

public class LexerSource {
    private String source;
    private int index = 0;
    private int peek = 0;

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
                Objects.equals(source.substring(index, index + cs.length()), cs.toString());
    }

    public Indexed next() {
        var ret = new Indexed(index, source.charAt(index), source.length());
        index++;
        peek = 0;
        return ret;
    }

    public Indexed next(int n) {
        var ret = new Indexed(index, source.substring(index, index + n), source.length());
        index += n;
        peek = 0;
        return ret;
    }

    public Indexed nextRemaining() {
        var ret = new Indexed(index, source.substring(index), source.length());
        index = source.length();
        peek = 0;
        return ret;
    }

    public Optional<Indexed> nextMatch(String until) {
        int n = source.substring(index).indexOf(until);
        if (n < 0) {
            index = source.length();
            peek = 0;
            return Optional.empty();
        }
        var ret = new Indexed(index + n, until, source.length());
        index = ret.index + until.length();
        peek = 0;
        return Optional.of(ret);
    }

    public Indexed peek() {
        var ret = new Indexed(index + peek, source.charAt(index + peek), source.length());
        peek++;
        return ret;
    }

    public Indexed nextAlphabeticToken() {
        int i = index;
        for (; i < source.length(); i++) {
            if (!Character.isAlphabetic(source.charAt(i))) break;
        }
        var ret = new Indexed(index, source.substring(index, i), source.length());
        index = i;
        peek = 0;
        return ret;
    }

    public void rollbackPeek() {
        peek = 0;
    }

    public void commitPeek() {
        index += peek;
        peek = 0;
    }

    public record Indexed(int index, String string, int parentLength) {
        private Indexed(int index, char ch, int parentLength) {
            this(index, String.valueOf(ch), parentLength);
        }
        char ch() {
            return length() == 0 ? 0 : string.charAt(0);
        }
        int lastIndex() {
            return index + string.length() - 1;
        }
        int length() { return string.length(); }
        boolean isFirst() { return index == 0; }
        boolean isLast() { return index == parentLength - 1; }
    }

}
