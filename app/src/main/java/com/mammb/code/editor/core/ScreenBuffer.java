package com.mammb.code.editor.core;

import java.util.ArrayList;
import java.util.List;

public interface ScreenBuffer<E extends ScreenLine> {

    int topLine();
    boolean isEmpty();
    void setCapacity(int capacity);

    static ScreenBuffer<TextRow> of() {
        return new ScreenBufferImpl();
    }

    class ScreenBufferImpl implements ScreenBuffer<TextRow> {
        private final List<TextRow> buffer = new ArrayList<>();
        private int capacity;

        @Override
        public int topLine() {
            return buffer.isEmpty() ? 0 : buffer.getFirst().row();
        }

        @Override
        public boolean isEmpty() {
            return buffer.isEmpty();
        }

        @Override
        public void setCapacity(int capacity) {
            this.capacity = capacity;

        }

    }

}
