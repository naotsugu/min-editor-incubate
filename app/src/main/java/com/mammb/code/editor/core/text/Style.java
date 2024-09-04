package com.mammb.code.editor.core.text;

public interface Style {
    record TextColor(String colorString) implements Style {}
}
