/**
 * Action is an interface which implements/enables a runnable to have a typed argument.
 * Hence, using this as runnable will enable developer to pass any object they want from place to place.
 *
 * @param <T> Type of the argument will be passed.
 */
public interface Action<T> {
    /**
     * Invokes the function with a value.
     *
     * @param value Value to pass as parameter.
     */
    void call(T value);
}