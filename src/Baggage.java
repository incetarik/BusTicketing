import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;

/**
 * The type Baggage.
 */
public class Baggage implements Serializable {
    private long ownerId;
    private float weight;

    /**
     * Gets the owner of the baggage.
     *
     * @return The owner of the baggage.
     */
    @JsonIgnore
    public Passenger getOwner() {
        for (Passenger passenger : Helpers.passengers) {
            if (passenger.getId() == ownerId) {
                return passenger;
            }
        }

        return null;
    }

    /**
     * Sets the owner of the baggage.
     *
     * @param owner Owner of the baggage.
     */
    public void setOwner(Passenger owner) {
        this.ownerId = owner.getId();
    }

    /**
     * Gets the ID of the owner.
     *
     * @return ID of the owner.
     */
    public long getOwnerId() {
        return ownerId;
    }

    /**
     * Gets the weight of this baggage.
     *
     * @return Weight of this baggage.
     */
    public float getWeight() {
        return weight;
    }

    /**
     * Sets the weight of this baggage.
     *
     * @param weight New weight of this baggage.
     */
    public void setWeight(float weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return String.valueOf(getWeight());
    }
}
