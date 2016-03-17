package de.wilson.wdtreelistlibrary.exception;

/**
 * Created by Wilhelm Dewald on 16/07/15.
 * <p/>
 * Stay cool, stay calm.
 */
public class WDException extends RuntimeException {

    public WDException(WDExceptionType type) {
        super(type.message);
    }

    public enum WDExceptionType {
        ITEM_OBJECT_CALLBACK_NULL_OBJECT("Returned object is NULL. It is necessary to return a valid object."),
        TOUCH_HELPER_LISTENER_CANNOT_BE_NULL("The touch helper listener must be set."),
        NO_PARENT_LEAF_FOR_GIVEN_POSITION("There is no parent leaf for the given position"),
        NO_LEAF_FOR_GIVEN_POSITION("There is no leaf for the given position"),
        PARENT_OR_CHILD_LEAF_ARE_NULL("Parent or child are null and that is not allowed"),
        FORBIDDEN_POSITION("This position is forbidden.");

        public String message;

        WDExceptionType(String message) {
            this.message = message;
        }
    }
}
