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

import com.mammb.code.editor.core.text.Style;
import com.mammb.code.editor.core.text.Style.StyleSpan;
import com.mammb.code.editor.core.text.Style.TextColor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * The Java syntax.
 * @author Naotsugu Kobayashi
 */
public class JavaSyntax implements Syntax {

    private final Trie keywords = new Trie();
    {
        Stream.of("""
        abstract,continue,for,new,switch,assert,default,goto,package,synchronized,boolean,do,if,private,
        this,break,double,implements,protected,throw,byte,else,import,public,throws,case,enum,instanceof,
        return,transient,catch,extends,int,short,try,char,final,interface,static,void,class,finally,long,
        strictfp,volatile,const,float,native,super,while,var,record,sealed,with,yield,to,transitive,uses
        """.split("[,\\s]")).forEach(keywords::put);
    }

    private final TreeMap<Anchor, String> scopes = new TreeMap<>();



    @Override
    public String name() {
        return "java";
    }

    @Override
    public List<StyleSpan> apply(int row, String text) {

        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        scopes.subMap(new Anchor(row, 0), new Anchor(row, Integer.MAX_VALUE)).clear();

        List<StyleSpan> spans = new ArrayList<>();

        LexerSource source = LexerSource.of(text);
        while (source.hasNext()) {
            if (source.match("//")) {
                var s = source.nextRemaining();
                spans.add(new StyleSpan(lineComment, s.index(), s.length()));
            } else {
                source.next();
            }
        }

        return spans;
    }

    static final TextColor lineComment = new TextColor("#888888");
}
