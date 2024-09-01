package com.mammb.code.editor.core;

public interface Draw {
    void clear();
    void fillRange(double x1, double y1, double x2, double y2, double l, double r);
    FontMetrics fontMetrics();
}
