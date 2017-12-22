import java.io.Serializable;

/**
 * The type Passenger.
 */
public class Passenger implements Serializable {
    private long id;
    private String name, surname, phone;
    private boolean isMale;

    /**
     * Gets the name of the passenger.
     *
     * @return The name of the passenger.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the passenger.
     *
     * @param name New name of the passenger.
     */
    public void setName(String name) {
        this.name = name.trim();
    }

    /**
     * Gets the surname of the passenger.
     *
     * @return The surname of the passenger.
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Sets the surname of the passenger.
     *
     * @param surname New surname of the passenger.
     */
    public void setSurname(String surname) {
        this.surname = surname.trim();
    }

    /**
     * Gets the phone number of the passenger.
     *
     * @return The phone number of the passenger.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number of the passenger.
     *
     * @param phone New phone number of the passenger.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the id of the passenger.
     *
     * @return The id of the passenger.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the id of the passenger.
     *
     * @param id The new id of the passenger.
     * @return True if successfully set. (By default)
     */
    public boolean setId(long id) {
        this.id = id;
        return true;
    }

    @Override
    public String toString() {
        return this.getName() + " " + this.getSurname();
    }

    /**
     * Gets the value of whether the passenger is male.
     *
     * @return True if the passenger is male.
     */
    public boolean isMale() {
        return isMale;
    }

    /**
     * Sets the value of whether the passenger is male.
     *
     * @param male Value.
     */
    public void setMale(boolean male) {
        isMale = male;
    }
}
