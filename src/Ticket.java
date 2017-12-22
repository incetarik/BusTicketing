import org.codehaus.jackson.annotate.JsonIgnore;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * The type Ticket.
 */
public class Ticket implements Serializable, Savable {
    private Bus bus;
    private long ownerId;
    private int seatNumber;
    private String id;
    private Location from, to;
    private Baggage baggage;

    @JsonIgnore
    private Date date;

    /**
     * Instantiates a new Ticket.
     */
    public Ticket() {
        id = String.valueOf(UUID.randomUUID().toString().hashCode());
    }

    /**
     * Loads a ticket from a file by id.
     *
     * @param id ID of the ticket to load.
     * @return Ticket if successfully loaded, null otherwise.
     */
    public static Ticket loadFromFile(int id) {
        return loadFromFile(String.valueOf(id));
    }

    /**
     * Loads a ticket from a file by id.
     *
     * @param id ID of the ticket to load.
     * @return Ticket if successfully loaded, null otherwise.
     */
    public static Ticket loadFromFile(String id) {
        try {
            if (!id.endsWith(".ticket")) id += ".ticket";
            if (!id.startsWith("ticket-")) id = "ticket-" + id;

            File file = new File(Helpers.ROOT_FOLDER_PATH + id);
            return Helpers.JSON_MAPPER.readValue(file, Ticket.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Gets the related bus.
     *
     * @return Bus for the ticket.
     */
    public Bus getBus() {
        return bus;
    }

    /**
     * Sets the related bus for the ticket.
     *
     * @param bus New bus for the ticket.
     */
    public void setBus(Bus bus) {
        this.bus = bus;
    }

    /**
     * Gets the owner of the ticket.
     * If is not saved here normally, hence tries to find the owner by looking up the passengers of the related bus.
     *
     * @return Owner of the ticket if there is.
     */
    @JsonIgnore
    public Passenger getOwner() {
        for (Passenger passenger : bus.getPassengers()) {
            if (passenger.getId() == ownerId) {
                return passenger;
            }
        }

        return null;
    }

    /**
     * Sets the owner of the ticket.
     *
     * @param passenger New owner of the ticket.
     */
    public void setOwner(Passenger passenger) {
        ownerId = passenger.getId();
    }

    /**
     * Gets the owner id of the ticket.
     *
     * @return The id of the owner.
     */
    public long getOwnerId() {
        return ownerId;
    }

    /**
     * Sets the owner id of the ticket.
     *
     * @param ownerId New owner id of the ticket.
     */
    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * Gets the seat number associated.
     *
     * @return The seat number associated.
     */
    public int getSeatNumber() {
        return seatNumber;
    }

    /**
     * Sets the seat number of the ticket for the owner.
     *
     * @param seatNumber New seat number for the ticket.
     */
    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    /**
     * Gets the id of the ticket.
     *
     * @return The id of the ticket.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the date of the ticket.
     *
     * @return The date of the ticket.
     */
    @JsonIgnore
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date of the ticket.
     *
     * @param date New date of the ticket.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Gets the movement location of the ticket (related bus).
     *
     * @return The related movement location.
     */
    public Location getFrom() {
        return from;
    }

    /**
     * Sets the movement location of the ticket.
     *
     * @param from New movement location of the ticket.
     */
    public void setFrom(Location from) {
        this.from = from;
    }

    /**
     * Gets the landing location of the ticket (related bus).
     *
     * @return The related landing location.
     */
    public Location getTo() {
        return to;
    }

    /**
     * Sets the landing location of the ticket.
     *
     * @param to New landing location of the ticket.
     */
    public void setTo(Location to) {
        this.to = to;
    }

    public boolean trySave() {
        try {
            String json = toJson().toString();

            Files.write(
                    new File(Helpers.ROOT_FOLDER_PATH + "ticket-" + id + ".ticket").toPath(),
                    json.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public JSONObject toJson() {

        JSONObject object = new JSONObject(this);
        object.remove("owner");

        JSONObject busObj = object.getJSONObject("bus");
        busObj.put("movementDate", Helpers.ISO_DATE_FORMAT.format(bus.getMovementDate()));
        busObj.put("landingDate", Helpers.ISO_DATE_FORMAT.format(bus.getLandingDate()));

        return object;
    }

    /**
     * Gets the baggage associated with this ticket.
     *
     * @return The baggage associated with this ticket.
     */
    public Baggage getBaggage() {
        return baggage;
    }

    /**
     * Sets the baggage associated with this ticket.
     *
     * @param baggage New baggage to associate with this ticket.
     */
    public void setBaggage(Baggage baggage) {
        this.baggage = baggage;
    }

    @Override
    public String toString() {
        return this.id;
    }
}