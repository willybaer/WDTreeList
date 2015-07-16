package de.wilson.wdtreelistlibrary.exception;

/**
 * Created by Wilhelm Dewald on 16/07/15.
 * <p/>
 * Stay cool, stay calm.
 */
public class WDException extends RuntimeException {

    public enum WDExceptionType{

        NO_PARENT_LEAF_FOR_GIVEN_POSITION("There is no parent leaf for the given position"),
        NO_LEAF_FOR_GIVEN_POSITION("There is no leaf for the given position"),
        FORBIDDEN_POSITION("This position is forbidden.");

        public String message;

        WDExceptionType(String message) {
            this.message = message;
        }
    }

    public WDException(WDExceptionType type) {
        super(type.message);
    }
}
