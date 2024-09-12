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
package com.mammb.code.editor.javafx;

import com.mammb.code.editor.core.Theme;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Parameters params = getParameters();
        var editorPane = new EditorPane();
        Scene scene = new Scene(editorPane, 640, 480);
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.setTitle("min-editor");
        stage.show();
    }

    private static String css = String.join(",", "data:text/css;base64",
            Base64.getEncoder().encodeToString("""
            .root {
              -fx-base:app-base;
              -fx-accent:app-accent;
              -fx-background:-fx-base;
              -fx-control-inner-background:app-back;
              -fx-control-inner-background-alt: derive(-fx-control-inner-background,-2%);
              -fx-focus-color: -fx-accent;
              -fx-faint-focus-color:app-accent22;
              -fx-light-text-color:app-text;
              -fx-mark-color: -fx-light-text-color;
              -fx-mark-highlight-color: derive(-fx-mark-color,20%);
              -fx-background-color:app-back;
            }
            .text-input, .label {
              -fx-font: 14px "Consolas";
            }
            .menu-bar {
              -fx-use-system-menu-bar:true;
              -fx-background-color:derive(-fx-control-inner-background,20%);
            }
            """
            .replaceAll("app-base", Theme.dark.baseColor())
            .replaceAll("app-text", Theme.dark.fgColor())
            .replaceAll("app-back", Theme.dark.baseColor())
            .replaceAll("app-accent", Theme.dark.paleHighlightColor())
            .getBytes(StandardCharsets.UTF_8)));
}
