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
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * The application pane.
 * @author Naotsugu Kobayashi
 */
public class AppPane extends BorderPane {

    private final TabPane tabPane = new TabPane();


    public AppPane() {

        setCenter(tabPane);
        tabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        tabPane.focusedProperty().addListener((ob, o, focused) -> {
            if (focused && !tabPane.getTabs().isEmpty())
                ((EditorPane) tabPane.getSelectionModel().getSelectedItem().getContent()).focus();
        });
        EditorPane editorPane = new EditorPane(consumer());
        tabPane.getTabs().add(createTab(editorPane));
    }

    public Consumer<Path> consumer() {
        return path -> {
            var editorPane = new EditorPane();
            var tab = createTab(editorPane);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        };
    }

    private Tab createTab(EditorPane editorPane) {
        var tab = new Tab();
        tab.setText(editorPane.fileNameProperty().get());
        tab.setContent(editorPane);
        tab.setOnClosed(e -> {
            if (tabPane.getTabs().isEmpty())
                tabPane.getTabs().add(createTab(new EditorPane()));
        });
        editorPane.fileNameProperty().addListener((ob, o, n) -> tab.setText(n));
        return tab;
    }

}
