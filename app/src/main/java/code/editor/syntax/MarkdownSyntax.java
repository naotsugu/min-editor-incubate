package code.editor.syntax;

import code.editor.Style.*;
import java.util.List;

public class MarkdownSyntax implements Syntax {
    @Override
    public String name() {
        return "md";
    }
    @Override
    public List<StyleSpan> apply(int row, String text) {
        return List.of();
    }

}
