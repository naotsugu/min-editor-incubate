package com.mammb.code.editor.fx;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import java.nio.file.Path;

public class AppPane extends BorderPane {

    public AppPane() {
    }

    public void setMain(EditorPane editorPane) {
        var gridPane = new GridPane();
        var tabPane = new TabPane();
        tabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        var tab = new Tab();
        tab.setText(editorPane.path().map(Path::getFileName).map(Path::toString).orElse("Untitled"));
        tab.setContent(editorPane);
        tabPane.getTabs().add(tab);
        gridPane.add(tabPane, 0, 0);
        setCenter(gridPane);
    }

}
