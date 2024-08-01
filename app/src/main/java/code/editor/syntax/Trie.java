package code.editor.syntax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trie {

    private final TrieNode root;

    public Trie() {
        root = new TrieNode(null);
    }

    public void put(String word) {
        TrieNode node = root;
        for (int i = 0; i < word.length();) {
            int cp = word.codePointAt(i);
            node = node.createIfAbsent(cp);
            i += Character.charCount(cp);
        }
        node.setEndOfWord();
    }

    public void remove(String word) {
        TrieNode node = searchPrefix(word);
        if (node == null || !node.isEndOfWord()) {
            return;
        }
        node.setEndOfWord(false);
        node.removeIfEmpty();
    }

    public boolean match(String word) {
        TrieNode node = searchPrefix(word);
        return node != null && node.isEndOfWord();
    }

    public boolean startsWith(String prefix) {
        return searchPrefix(prefix) != null;
    }

    public List<String> suggestion(String word) {
        TrieNode node = root;
        for (int i = 0; i < word.length();) {
            int cp = word.codePointAt(i);
            if (node.contains(cp)) {
                node = node.get(cp);
            } else {
                break;
            }
            i += Character.charCount(cp);
        }
        return node.childKeys();
    }

    private TrieNode searchPrefix(String word) {
        TrieNode node = root;
        for (int i = 0; i < word.length();) {
            int cp = word.codePointAt(i);
            if (node.contains(cp)) {
                node = node.get(cp);
            } else {
                return null;
            }
            i += Character.charCount(cp);
        }
        return node;
    }

    class TrieNode {

        private final TrieNode parent;
        private final Map<Integer, TrieNode> children;
        private boolean endOfWord;

        TrieNode(TrieNode parent) {
            this.parent = parent;
            this.children = new HashMap<>();
        }

        TrieNode createIfAbsent(Integer key) {
            return children.computeIfAbsent(key, k -> new TrieNode(this));
        }

        boolean contains(int codePoint) {
            return children.containsKey(codePoint);
        }

        TrieNode get(int codePoint) {
            return children.get(codePoint);
        }

        void put(int codePoint, TrieNode node) {
            children.put(codePoint, node);
        }

        void removeIfEmpty() {
            if (parent == null) {
                return;
            }
            if (children.isEmpty()) {
                parent.children.remove(key());
                parent.removeIfEmpty();
            }
        }

        private Integer key() {
            if (parent == null) {
                return null;
            }
            return parent.children.entrySet().stream()
                    .filter(e -> e.getValue() == this)
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }

        List<String> childKeys() {
            return children.keySet().stream().map(Character::toString).toList();
        }

        void setEndOfWord() { endOfWord = true; }

        void setEndOfWord(boolean endOfWord) { this.endOfWord = endOfWord; }

        boolean isEndOfWord() { return endOfWord; }
    }

}