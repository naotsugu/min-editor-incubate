package code.editor;

import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.function.Predicate;

import static javafx.scene.input.KeyCode.*;

public interface Action {

    /**
     * Get the action type.
     * @return the type
     */
    Type type();

    /**
     * Get the attribute.
     * @return the attribute
     */
    String attr();

    /**
     * Get occurred at.
     * @return occurred at
     */
    long occurredAt();

    record ActionRecord(Action.Type type, String attr, long occurredAt) implements Action { }

    static Action of(Action.Type type) {
        return new ActionRecord(type, "", System.currentTimeMillis());
    }

    static Action of(Action.Type type, String attr) {
        return new ActionRecord(type, attr, System.currentTimeMillis());
    }

    static Action of(KeyEvent e) {
        if (e.getCode() == RIGHT) return e.isShiftDown()
                ? Action.of(Action.Type.SELECT_CARET_RIGHT)
                : Action.of(Action.Type.CARET_RIGHT);
        else if (e.getCode() == LEFT) return e.isShiftDown()
                ? Action.of(Action.Type.SELECT_CARET_LEFT)
                : Action.of(Action.Type.CARET_LEFT);
        else if (e.getCode() == UP) return e.isShiftDown()
                ? Action.of(Action.Type.SELECT_CARET_UP)
                : Action.of(Action.Type.CARET_UP);
        else if (e.getCode() == DOWN) return e.isShiftDown()
                ? Action.of(Action.Type.SELECT_CARET_DOWN)
                : Action.of(Action.Type.CARET_DOWN);
        else if (e.getCode() == DELETE) return Action.of(Action.Type.DELETE);
        else if (e.getCode() == BACK_SPACE) return Action.of(Action.Type.BACK_SPACE);
        else if (SC_C.match(e)) return Action.of(Action.Type.COPY);
        else if (SC_V.match(e)) return Action.of(Action.Type.PASTE);
        else if (SC_X.match(e)) return Action.of(Action.Type.CUT);
        else if (SC_Z.match(e)) return Action.of(Action.Type.UNDO);
        else if (SC_Y.match(e) || SC_SZ.match(e)) return Action.of(Action.Type.REDO);

        else {
            if (keyInput.test(e)) {
                int ascii = e.getCharacter().getBytes()[0];
                String ch = (ascii == 13) // 13:CR
                        ? "\n"
                        : e.getCharacter();
                return Action.of(Action.Type.TYPED, ch);
            }
        }
        return Action.of(Action.Type.EMPTY);
    }

    /** The action event type.*/
    enum Type {
        TYPED, DELETE, BACK_SPACE,
        CARET_RIGHT, CARET_LEFT, CARET_UP, CARET_DOWN,
        SELECT_CARET_RIGHT, SELECT_CARET_LEFT, SELECT_CARET_UP, SELECT_CARET_DOWN,
        COPY, PASTE, CUT,
        UNDO, REDO,
        EMPTY,
        ;
    }

    KeyCombination SC_C = new KeyCharacterCombination("c", KeyCombination.SHORTCUT_DOWN);
    KeyCombination SC_V = new KeyCharacterCombination("v", KeyCombination.SHORTCUT_DOWN);
    KeyCombination SC_X = new KeyCharacterCombination("x", KeyCombination.SHORTCUT_DOWN);
    KeyCombination SC_Z = new KeyCharacterCombination("z", KeyCombination.SHORTCUT_DOWN);
    KeyCombination SC_Y = new KeyCharacterCombination("y", KeyCombination.SHORTCUT_DOWN);
    KeyCombination SC_SZ= new KeyCharacterCombination("z", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

    Predicate<KeyEvent> controlKeysFilter = e ->
            System.getProperty("os.name").toLowerCase().startsWith("windows")
                    ? !e.isControlDown() && !e.isAltDown() && !e.isMetaDown() && e.getCharacter().length() == 1 && e.getCharacter().getBytes()[0] != 0
                    : !e.isControlDown() && !e.isAltDown() && !e.isMetaDown();
    Predicate<KeyEvent> keyInput = e -> e.getEventType() == KeyEvent.KEY_TYPED &&
        !(e.getCode().isFunctionKey() || e.getCode().isNavigationKey() ||
          e.getCode().isArrowKey()    || e.getCode().isModifierKey() ||
          e.getCode().isMediaKey()    || !controlKeysFilter.test(e) ||
          e.getCharacter().isEmpty());


}