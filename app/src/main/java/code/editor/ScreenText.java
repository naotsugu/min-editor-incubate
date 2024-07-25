package code.editor;

import code.editor.javafx.FontMetrics;
import com.mammb.code.piecetable.Document;
import javafx.scene.text.Font;
import java.util.ArrayList;
import java.util.List;

public class ScreenText {
    double width;
    double height;
    boolean wrap = false;
    List<LineText> lines = new ArrayList<>();
    Document document;
    FontMetrics fm;

    public ScreenText(double width, double height, Document document) {
        this.width = width;
        this.height = height;
        this.document = document;
        this.fm = FontMetrics.of(Font.font("Consolas", 18));

        double h = 0;
        System.out.println(document.rows());
        for (int i = 0; i < document.rows(); i++) {
            for (LineText lineText : RowText.of(i, document.getText(i).toString(), fm)
                    .split(wrap ? width : 0)) {
                lines.add(lineText);
                h += lineText.lineHeight();
                if (h >= height) break;
            }
            if (h >= height) break;
        }
    }

}
