package code.javafx;

import com.sun.javafx.tk.Toolkit;

public class TextLayout {

    private com.sun.javafx.scene.text.TextLayout textLayout;

    public TextLayout() {
        textLayout = Toolkit.getToolkit().getTextLayoutFactory().getLayout();
    }

}
