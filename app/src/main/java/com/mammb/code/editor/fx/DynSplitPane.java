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

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;

public class DynSplitPane extends StackPane {

    private SplitPane pane = new SplitPane();

    public DynSplitPane() {
        getChildren().add(pane);
        pane.setOrientation(Orientation.HORIZONTAL);
    }

    private DynSplitPane(Node node) {
        this();
        pane.getItems().add(node);
    }


    public void addRight(Node node) {
        if (pane.getItems().isEmpty()) {
            pane.getItems().add(node);
            return;
        }
        var item = pane.getItems().getFirst();
        pane.getItems().clear();
        pane.setOrientation(Orientation.HORIZONTAL);
        var left = new DynSplitPane(item);
        var right = new DynSplitPane(node);
        pane.getItems().addAll(left, right);
    }

    public void addBottom(Node node) {
        if (pane.getItems().isEmpty()) {
            pane.getItems().add(node);
            return;
        }
        var item = pane.getItems().getFirst();
        pane.getItems().clear();
        pane.setOrientation(Orientation.VERTICAL);
        var top = new DynSplitPane(item);
        var bottom = new DynSplitPane(node);
        pane.getItems().addAll(top, bottom);
    }

}
