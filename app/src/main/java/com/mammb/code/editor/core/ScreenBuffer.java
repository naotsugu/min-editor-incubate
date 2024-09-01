package com.mammb.code.editor.core;

import java.util.ArrayList;
import java.util.List;

public interface ScreenBuffer<E extends ScreenLine> {

    int firstRow();
    boolean isEmpty();

    static <E extends ScreenLine> ScreenBuffer<E> of() {
        return new ScreenBufferImpl<>();
    }

    class ScreenBufferImpl<E extends ScreenLine> implements ScreenBuffer<E> {
        private final List<E> buffer = new ArrayList<>();

        @Override
        public int firstRow() {
            return buffer.isEmpty() ? 0 : buffer.getFirst().row();
        }

        @Override
        public boolean isEmpty() {
            return buffer.isEmpty();
        }

    }

}
