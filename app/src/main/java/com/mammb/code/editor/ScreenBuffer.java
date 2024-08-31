package com.mammb.code.editor;

import java.util.ArrayList;
import java.util.List;

public interface ScreenBuffer<E extends ScreenLine> {

    int firstRow();
    int screenLines();

    class ScreenBufferImpl<E extends ScreenLine> implements ScreenBuffer<E> {
        private final List<E> buffer = new ArrayList<>();

        @Override
        public int firstRow() {
            return buffer.isEmpty() ? 0 : buffer.getFirst().row();
        }

        @Override
        public int screenLines() {
            return 0;
        }
    }

}
