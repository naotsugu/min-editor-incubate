package code.editor;

public interface StyleSpan {

    sealed interface Style {}
    record TextColor(String colorString) implements Style {}
    record BgColor(String colorString) implements Style {}
    record Selected() implements Style {}
    record Emphasize() implements Style {}

    record StyleSpanRecord(Style style, int length) implements StyleSpan { }

}
