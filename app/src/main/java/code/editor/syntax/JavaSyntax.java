package code.editor.syntax;

import code.editor.Style.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class JavaSyntax implements Syntax {

    final Trie keywords = new Trie();
    {
        Stream.of("""
        abstract,continue,for,new,switch,assert,default,goto,package,synchronized,boolean,do,if,private,
        this,break,double,implements,protected,throw,byte,else,import,public,throws,case,enum,instanceof,
        return,transient,catch,extends,int,short,try,char,final,interface,static,void,class,finally,long,
        strictfp,volatile,const,float,native,super,while,var,record,sealed,with,yield,to,transitive,uses"""
                .split("[,\\s]")).forEach(keywords::put);
    }

    @Override
    public String name() {
        return "java";
    }

    @Override
    public List<StyleSpan> apply(int row, String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        List<StyleSpan> spans = new ArrayList<>();

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '"') {
                i = Syntax.read('"', '\\', Palette.DEEP_GREEN.colorString, text, i, spans);
            } else if (Character.isAlphabetic(ch)) {
                i = Syntax.read(keywords, Palette.ORANGE.colorString, text, i, spans);
            }
        }
        return spans;
    }

}
