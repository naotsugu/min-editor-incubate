package code.editor;

public interface Lang {
    record Indexed<E>(int index, E value) {}
    record Loc(double x, double y) { }
    record Pos(int row, int col) {
        public static Pos zero = new Pos(0, 0);
    }
}
