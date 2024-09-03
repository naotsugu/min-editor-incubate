package com.mammb.code.editor.core.text;

public interface Text {

    int row();

    String text();

    double width();

    double height();

    default int length() {
        return text().length();
    }

    default int textLength() {
        char ch1 = (text().length() > 1) ? text().charAt(text().length() - 1) : 0;
        char ch2 = (text().length() > 2) ? text().charAt(text().length() - 2) : 0;
        return text().length() - (
                (ch2 == '\r' && ch1  == '\n') ? 2 : (ch1  == '\n') ? 1 : 0
        );
    }

    default boolean isSurrogate(int index) {
        return Character.isSurrogate(text().charAt(index));
    }

    default boolean isHighSurrogate(int index) {
        return Character.isHighSurrogate(text().charAt(index));
    }

    default boolean isLowSurrogate(int index) {
        return Character.isLowSurrogate(text().charAt(index));
    }
}
