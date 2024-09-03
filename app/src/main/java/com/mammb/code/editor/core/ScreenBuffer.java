package com.mammb.code.editor.core;

import com.mammb.code.editor.core.text.RowText;
import java.util.ArrayList;
import java.util.List;

public interface ScreenBuffer<E extends ScreenLine> {

    int topLine();
    boolean isEmpty();
    void setCapacity(int capacity);

    static ScreenBuffer<RowText> of() {
        return new ScreenBufferImpl();
    }

    class ScreenBufferImpl implements ScreenBuffer<RowText> {
        private final List<RowText> buffer = new ArrayList<>();
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
