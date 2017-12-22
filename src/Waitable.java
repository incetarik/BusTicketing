/**
 * Indicates whether an object is waitable.
 * A waitable is an object which has the waiting state for any reason.
 * This is useful for windows to indicate user that the related window is waiting for
 * something, like an input or a data from the internet.
 */
public interface Waitable {
    /**
     * Sets the new waiting state.
     *
     * @param value The new state of waiting.
     */
    void setWaiting(boolean value);
}
