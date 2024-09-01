package com.mammb.code.editor.core;

public interface TextLayout {
    int TAB_SIZE = 4;
    int MARGIN_TOP = 5;
    int MARGIN_LEFT = 5;
    void setSize(double width, double height);
    Loc loc(int row, int col);
    double lineHeight();
    double width();
    double height();
    double marginTop();
    double marginLeft()

    static TextLayout of(FontMetrics fm) {
        return new PlainTextLayout(fm);
    }

    record Loc(double x, double y) { }

    class PlainTextLayout implements TextLayout {
        private double width = 0, height = 0;
        private final FontMetrics fm;

        public PlainTextLayout(FontMetrics fm) {
            this.fm = fm;
        }

        @Override
        public void setSize(double width, double height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public Loc loc(int row, int col) {
            return ;
        }

        @Override
        public double lineHeight() {
            return fm.getLineHeight();
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
        public double marginTop() {
            return 5;
        }

        @Override
        public double marginLeft() {
            return 5;
        }

    }

//    class WrapTextLayout implements TextLayout {
//        double width = 0, height = 0;
//        private FontMetrics fm;
//
//        @Override
//        public double lineHeight() {
//            return fm.getLineHeight();
//        }
//
//        @Override
//        public double width() {
//            return width;
//        }
//
//        @Override
//        public double height() {
//            return height;
//        }
//    }

    private static double[] advances(String text, code.editor.FontMetrics fm) {
        double[] advances = new double[text.length()];
        for (int i = 0; i < text.length(); i++) {
            char ch1 = text.charAt(i);
            if (Character.isHighSurrogate(ch1)) {
                advances[i] = fm.getAdvance(ch1, text.charAt(i + 1));
                i++;
            } else if (Character.isISOControl(ch1)) {
                i++;
            } else if (ch1 == '\t') {
                advances[i] = fm.getAdvance(" ".repeat(TAB_SIZE));
            } else {
                advances[i] = fm.getAdvance(ch1);
            }
        }
        return advances;
    }

}
