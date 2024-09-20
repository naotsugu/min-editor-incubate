package com.mammb.code.editor.core.syntax;

import java.util.TreeMap;

public class BlockScope {

    private final TreeMap<Anchor, Token> scopes = new TreeMap<>();

    public void putStart(int row, int col, BlockType type) {
        scopes.put(new Anchor(row, col), new StartToken(type));
    }

    public void putEnd(int row, int col, BlockType type) {
        scopes.put(new Anchor(row, col), new EndToken(type));
    }

    public void putOpen(int row, int col, BlockType type) {
        scopes.put(new Anchor(row, col), new OpenToken(type));
    }

    record Anchor(int row, int col) implements Comparable<Anchor> {
        @Override
        public int compareTo(Anchor that) {
            int c = Integer.compare(this.row, that.row);
            return c == 0 ? Integer.compare(this.col, that.col) : c;
        }
    }

    private interface Token { BlockType type(); }
    private record OpenToken(BlockType type) implements Token { }
    private record StartToken(BlockType type) implements Token { }
    private record EndToken(BlockType type) implements Token { }

    public interface BlockType {
        static BlockType open(String token) {
            record OpenType(String token) implements BlockType { }
            return new OpenType(token);
        }
        static BlockType close(String startToken, String closeToken) {
            record CloseType(String startToken, String closeToken) implements BlockType { }
            return new CloseType(startToken, closeToken);
        }
    }

}
