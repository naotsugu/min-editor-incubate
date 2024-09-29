package com.mammb.code.editor.fx;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class EditorTabPane extends TabPane {
    public EditorTabPane() {
    }
    public void addTab(EditorPane editorPane) {
        var tab = new Tab("xxx");
        tab.setContent(editorPane);
        getTabs().add(tab);
    }
}
