package code.editor.syntax;

import code.editor.ScreenText;
import java.util.List;

public class MarkdownSyntax implements Syntax {
    @Override
    public String name() {
        return "md";
    }
    @Override
    public List<ScreenText.StyleSpan> apply(int row, String text) {
        return List.of();
    }

}
