package com.mammb.code.editor.core;

public interface Style {
    record TextColor(String colorString) implements code.editor.Style {}
}
