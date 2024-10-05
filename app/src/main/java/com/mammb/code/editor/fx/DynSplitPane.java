package com.mammb.code.editor.fx;

import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.layout.StackPane;

public class DynSplitPane extends Control {

    public DynSplitPane() {
    }


    static class Content extends StackPane {
        private Node content;
        public Content(Node n) {
            this.content = n;
            if (n != null) {
                getChildren().add(n);
            }
        }
    }
}
