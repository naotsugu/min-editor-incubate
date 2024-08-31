package com.mammb.code.editor;

import java.util.ArrayList;
import java.util.List;

public interface CaretGroup {

    class CaretGroupImpl implements CaretGroup {
        private final List<Caret> carets = new ArrayList<>();
    }
}
