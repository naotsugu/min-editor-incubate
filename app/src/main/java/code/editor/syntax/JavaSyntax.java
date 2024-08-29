package code.editor.syntax;

import code.editor.Style.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;

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

        scopes.tailMap(new Anchor(row, 0), true).clear();
        boolean inBlockComment = false;
        for (var e : scopes.entrySet()) {
            if (inBlockComment && e.getValue().equals("*/")) {
                inBlockComment = false;
            } else if (!inBlockComment && e.getValue().equals("/*")) {
                inBlockComment = true;
            }
        }

        List<StyleSpan> spans = new ArrayList<>();

        int i = 0;

        if (inBlockComment && !text.contains("*/")) {
            spans.add(new StyleSpan(
                    new TextColor(Palette.DEEP_GREEN.colorString), 0, text.length()));
            return spans;
        }

        if (inBlockComment && text.contains("*/")) {
            i = text.indexOf("*/") + 2;
            scopes.put(new Anchor(row, i), "*/");
            spans.add(new StyleSpan(
                    new TextColor(Palette.DEEP_GREEN.colorString), 0, i));
        }


        for (; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '/' && i + 1 < text.length() && text.charAt(i + 1) == '*') {
                scopes.put(new Anchor(row, i), "/*");
                int e = text.substring(i).indexOf("*/");
                if (e > 0) {
                    e += 2;
                    spans.add(new StyleSpan(
                            new TextColor(Palette.DEEP_GREEN.colorString), i, i + e));
                    scopes.put(new Anchor(row, i + e), "*/");
                    i += e;
                } else {
                    spans.add(new StyleSpan(
                            new TextColor(Palette.DEEP_GREEN.colorString), i, text.length()));
                    return spans;
                }
            } else if (ch == '"') {
                i = Syntax.read('"', '\\', Palette.DEEP_GREEN.colorString, text, i, spans);
            } else if (Character.isAlphabetic(ch)) {
                i = Syntax.read(keywords, Palette.ORANGE.colorString, text, i, spans);
            }
        }
        return spans;
    }

}
