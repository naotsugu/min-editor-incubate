package com.mammb.code.editor.core;

public interface Layout {

    double width();
    double height();
    void setSize(double width, double height);
    int lineSize();
    double lineHeight();
    double marginTop();
    double marginBottom();
    double marginRight();
    double marginLeft();
    void setMarginRight(double marginRight);

    default double regionWidth() {
        return width() - marginLeft() - marginRight();
    }
    default double regionHeight() {
        return width() - marginTop() - marginBottom();
    }

    static Layout of(FontMetrics fontMetrics) {
        return new LayoutImpl(fontMetrics);
    }


    class LayoutImpl implements Layout {
        private double width = 0, height = 0;
        private double marginTop = 5, marginRight = 5;
        private final FontMetrics fontMetrics;

        public LayoutImpl(FontMetrics fontMetrics) {
            this.fontMetrics = fontMetrics;
        }

        @Override
        public double width() {
            return width;
        }
        @Override
        public double height() {
            return height;
        }

        @Override
        public void setSize(double width, double height) {

        }

        @Override
        public int lineSize() {
            return (int) Math.ceil(Math.max(0, regionHeight()) / lineHeight());
        }

        @Override
        public double lineHeight() {
            return fontMetrics.getLineHeight();
        }

        @Override
        public double marginTop() {
            return marginTop;
        }

        @Override
        public double marginBottom() {
            return 0;
        }

        @Override
        public double marginRight() {
            return marginRight;
        }

        @Override
        public double marginLeft() {
            return 0;
        }

        @Override
        public void setMarginRight(double marginRight) {
            this.marginRight = marginRight;
        }
    }

}
