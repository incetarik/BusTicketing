import org.json.JSONObject;

/**
 * Indicates an object is savable to the system.
 */
public interface Savable {
    /**
     * Tries to save this object to the system.
     *
     * @return True if successfully saved.
     */
    boolean trySave();

    /**
     * Gets the JSON Object representation of the object.
     * This format is used for saving the object.
     *
     * @return The JSON representation of the object.
     */
    JSONObject toJson();
}
