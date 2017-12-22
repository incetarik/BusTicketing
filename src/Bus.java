import org.codehaus.jackson.annotate.JsonIgnore;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;

/**
 * The type Bus.
 */
public class Bus implements Comparable<Bus>, Serializable, Savable {
    private String code;
    private ArrayList<Passenger> passengers;
    private int seatNumber, soldSeat;
    private Location from, to;
    private Date movementDate, landingDate;
    private double price;

    /**
     * Instantiates a new Bus.
     */
    public Bus() {
    }

    /**
     * Instantiates a new Bus.
     *
     * @param code      the code
     * @param seatCount the seat count
     * @param from      the from
     * @param to        the to
     */
    public Bus(String code, int seatCount, Location from, Location to) {
        this.passengers = new ArrayList<>();
        this.code = code;
        this.from = from;
        this.to = to;
        this.seatNumber = seatCount;
    }

    /**
     * Loads a bus from a file.
     *
     * @param fileName The path of a bus file.
     * @return Bus, loaded from the file or null if an error occurred.
     */
    public static Bus loadFromFile(String fileName) {
        try {
            if (!fileName.endsWith(".bus")) fileName += ".bus";
            if (!fileName.startsWith("bus-")) fileName = "bus-" + fileName;

            File file = new File(Helpers.ROOT_FOLDER_PATH + fileName);
            return Helpers.JSON_MAPPER.readValue(file, Bus.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Gets the bus-specific unique code.
     *
     * @return The bus code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the list of the passengers in this bus.
     *
     * @return The list of the passengers in the bus.
     */
    public ArrayList<Passenger> getPassengers() {
        return passengers;
    }

    /**
     * Sets the passengers of this bus at once (with a list).
     *
     * @param passengers Passengers
     */
    public void setPassengers(ArrayList<Passenger> passengers) {
        this.passengers = passengers;
    }

    /**
     * Gets the seat number of the bus.
     *
     * @return Seat number of the bus.
     */
    public int getSeatNumber() {
        return seatNumber;
    }

    /**
     * Gets the movement location of this bus.
     *
     * @return The movement location of the bus.
     */
    public Location getFrom() {
        return from;
    }

    /**
     * Gets the landing location of this bus.
     *
     * @return The landing location of the bus.
     */
    public Location getTo() {
        return to;
    }

    /**
     * Gets the movement date of this bus.
     *
     * @return The movement date of the bus.
     */
    public Date getMovementDate() {
        return movementDate;
    }

    /**
     * Sets the movement date of this bus.
     *
     * @param movementDate New movement date of the bus.
     */
    public void setMovementDate(Date movementDate) {
        this.movementDate = movementDate;
    }

    /**
     * Gets the remaining seat number of this bus.
     *
     * @return The remaining seat number of the bus.
     */
    @JsonIgnore
    public int getRemainingSeatNumber() {
        int sold = 0;
        if (Helpers.tickets != null) {
            for (Ticket ticket : Helpers.tickets) {
                if (this.equals(ticket.getBus())) {
                    sold++;
                }
            }
        }

        return seatNumber - soldSeat - sold;
    }

    /**
     * Gets the sold seat number of this bus.
     *
     * @return The sold seat number of the bus.
     */
    public int getSoldSeat() {
        return soldSeat;
    }

    /**
     * Sets the sold seat number of this bus.
     *
     * @param soldSeat New sold seat of the bus.
     */
    public void setSoldSeat(int soldSeat) {
        this.soldSeat = soldSeat;
    }

    /**
     * Gets the price of a seat.
     *
     * @return The price of a seat.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the price of a seat.
     *
     * @param price New price of a seat.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Gets the landing date of this bus.
     *
     * @return The landing date of the bus.
     */
    public Date getLandingDate() {
        return landingDate;
    }

    /**
     * Sets the landing date of this bus.
     *
     * @param landingDate New landing date of the bus.
     */
    public void setLandingDate(Date landingDate) {
        this.landingDate = landingDate;
    }

    @Override
    public String toString() {
        return String.format("%s → %s", from.getName(), to.getName());
    }

    @Override
    public int compareTo(Bus o) {
        if (o.passengers != null && o.passengers.size() > this.passengers.size()) {
            this.passengers = o.passengers;
        }

        int i = this.getMovementDate().compareTo(o.getMovementDate());
        if (i == 0) return Double.compare(this.price, o.price);
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Bus)) return false;
        if (!((Bus) obj).code.equals(code)) return false;
        if (!((Bus) obj).from.equals(from)) return false;
        if (!((Bus) obj).to.equals(to)) return false;
        if (((Bus) obj).soldSeat != soldSeat) return false;
        if (!((Bus) obj).landingDate.equals(landingDate)) return false;
        if (!((Bus) obj).movementDate.equals(movementDate)) return false;

        if (((Bus) obj).passengers != null) {
            if (((Bus) obj).passengers.size() > passengers.size()) {
                this.passengers = ((Bus) obj).passengers;
            }
        }

        return true;
    }

    @Override
    public boolean trySave() {
        try {
            JSONObject object = this.toJson();

            Files.write(
                    new File(Helpers.ROOT_FOLDER_PATH + "bus-" + code + ".bus").toPath(),
                    object.toString().getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING
            );

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public JSONObject toJson() {
        ArrayList<Passenger> passengers = this.passengers;
        this.passengers = null;
        JSONObject object = new JSONObject(this);
        object.remove("passengers");
        object.put("movementDate", Helpers.ISO_DATE_FORMAT.format(movementDate));
        object.put("landingDate", Helpers.ISO_DATE_FORMAT.format(landingDate));
        this.passengers = passengers;

        return object;
    }
}
