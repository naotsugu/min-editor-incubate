package code.editor;

import javafx.scene.input.KeyEvent;

import static javafx.scene.input.KeyCode.*;

public interface Action {

    /** The action event type.*/
    enum Type {
        TYPED, DELETE, BACK_SPACE,
        CARET_RIGHT, CARET_LEFT, CARET_UP, CARET_DOWN,
        SELECT_CARET_RIGHT, SELECT_CARET_LEFT, SELECT_CARET_UP, SELECT_CARET_DOWN,
        EMPTY,
        ;
    }

    record ActionRecord(Action.Type type, String attr, long occurredAt) implements Action { }

    static Action of(Action.Type type) {
        return new ActionRecord(type, "", System.currentTimeMillis());
    }

    static Action of(Action.Type type, String attr) {
        return new ActionRecord(type, attr, System.currentTimeMillis());
    }

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

    static Action of(KeyEvent e) {
        if (e.getCode() == RIGHT) return e.isShiftDown() ? Action.of(Action.Type.SELECT_CARET_RIGHT) : Action.of(Action.Type.CARET_RIGHT);
        else if (e.getCode() == LEFT) return e.isShiftDown() ? Action.of(Action.Type.SELECT_CARET_LEFT) : Action.of(Action.Type.CARET_LEFT);
        else if (e.getCode() == UP) return e.isShiftDown() ? Action.of(Action.Type.SELECT_CARET_UP) : Action.of(Action.Type.CARET_UP);
        else if (e.getCode() == DOWN) return e.isShiftDown() ? Action.of(Action.Type.SELECT_CARET_DOWN) : Action.of(Action.Type.CARET_DOWN);
        else if (e.getCode() == DELETE) return Action.of(Action.Type.DELETE);
        else if (e.getCode() == BACK_SPACE) return Action.of(Action.Type.BACK_SPACE);
        else {
            if (e.getEventType() == KeyEvent.KEY_TYPED && !e.getCharacter().isEmpty()) {
                int ascii = e.getCharacter().getBytes()[0];
                String ch = (ascii == 13) // 13:CR
                        ? "\n"
                        : e.getCharacter();
                return Action.of(Action.Type.TYPED, ch);
            }
            return Action.of(Action.Type.EMPTY);
        }
    }
}
