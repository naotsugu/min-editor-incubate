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
package com.mammb.code.editor.fx;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * The application pane.
 * @author Naotsugu Kobayashi
 */
public class AppPane extends BorderPane {

    private final GridPane gridPane = new GridPane();
    private final TabPane tabPane = new TabPane();

    public AppPane() {
        tabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
    }

    public void setMain(EditorPane editorPane) {
        var tab = new Tab();
        tab.setText(editorPane.fileNameProperty().get());
        tab.setContent(editorPane);
        tabPane.getTabs().add(tab);
        gridPane.add(tabPane, 0, 0);
        setCenter(gridPane);
        editorPane.fileNameProperty().addListener((ob, o, n) -> tab.setText(n));
        GridPane.setHgrow(tabPane, Priority.ALWAYS);
        GridPane.setVgrow(tabPane, Priority.ALWAYS);
    }

}
