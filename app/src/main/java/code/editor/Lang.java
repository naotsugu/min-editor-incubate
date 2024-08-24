package code.editor;

public interface Lang {
    record Indexed<E>(int index, E value) {}
    record Loc(double x, double y) { }
}
