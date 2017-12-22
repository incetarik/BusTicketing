import java.io.Serializable;

/**
 * The type Location.
 */
public class Location implements Serializable {
    private String name;
    private String htmlValue;

    /**
     * Instantiates a new Location.
     */
    public Location() {
    }

    /**
     * Instantiates a new Location.
     *
     * @param name      the name
     * @param htmlValue the html value
     */
    public Location(String name, String htmlValue) {
        this.name = name;
        this.htmlValue = htmlValue;
    }

    /**
     * Gets the name of the location.
     *
     * @return The name of the location.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the location.
     *
     * @param name New name of the location.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("%s", name);
    }

    /**
     * Gets the HTML value of the location.
     * Shall not be used externally, this is a helper to select correctly and prevent duplications in requests.
     *
     * @return The HTML value of the location.
     */
    public String getHtmlValue() {
        return htmlValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Location)) return false;

        if (!((Location) obj).name.equals(name)) return false;
        return ((Location) obj).htmlValue.equals(htmlValue);
    }
}
