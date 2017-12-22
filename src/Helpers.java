import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * The type Helpers.
 */
public class Helpers {
    /**
     * ISO Date Format, for parsing and converting to string.
     * yyyy-MM-dd'T'HH:mm:ss.SSSZ
     */
    public static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    /**
     * Normal date format.
     * HH:mm:ss dd.MM.yyyy
     */
    public static final SimpleDateFormat NORMAL_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");

    /**
     * Root folder path of the application.
     * This is used for storing data.
     * Normally, set as ~/BusTicketing/
     */
    public static final String ROOT_FOLDER_PATH = System.getProperty("user.home") + File.separator + "BusTicketing" + File.separator;

    /**
     * JSON Mapper for Java language.
     * By using this, we can create/convert from a JSON object to a Java class.
     */
    public static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final SimpleDateFormat BOARDING_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat MOVEMENT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    /**
     * General message handler. Any event may execute/send a message/notification to the user through this, by calling
     * {@link Helpers#message(String)} function. This shall not be used externally, except for handler assignment.
     */
    public static Action<String> messageHandler;
    /**
     * List of registered passengers.
     */
    public static ArrayList<Passenger> passengers;
    /**
     * List of buses used for direct accessibility.
     */
    public static ArrayList<Bus> buses;
    /**
     * List of baggages for direct accessibility.
     */
    public static ArrayList<Baggage> baggages;
    /**
     * List of bought tickets.
     */
    public static ArrayList<Ticket> tickets;
    private static boolean onlyFromDifferentLocations = false;
    private static ArrayList<Action<Ticket>> onTicketSavedListeners;
    private static ArrayList<Action<Ticket>> onTicketRemovedListeners;
    private static ArrayList<Action<Boolean>> onLocationConditionChangedListeners;

    /**
     * Adds a location condition change listener, which will be fired when selecting between the same locations
     * for "FROM" and "TO" are allowed.
     *
     * @param action An action, has state boolean.
     */
    public static void addOnLocationConditionChangedListener(Action<Boolean> action) {
        if (onLocationConditionChangedListeners == null) onLocationConditionChangedListeners = new ArrayList<>();
        onLocationConditionChangedListeners.add(action);
    }

    /**
     * Sets the location condition.
     * Note that this function should be used for the setting, since this fires the events.
     *
     * @param value New value of the condition.
     */
    public static void setLocationCondition(boolean value) {
        onlyFromDifferentLocations = value;
        if (onLocationConditionChangedListeners != null)
            onLocationConditionChangedListeners.forEach(a -> a.call(value));
    }

    /**
     * Adds a ticket saved listener.
     *
     * @param action An action with Ticket parameter.
     */
    public static void addOnTicketSavedListener(Action<Ticket> action) {
        if (onTicketSavedListeners == null) onTicketSavedListeners = new ArrayList<>();
        onTicketSavedListeners.add(action);
    }

    /**
     * Adds a ticket removed listener.
     *
     * @param action An action with Ticket parameter.
     */
    public static void addOnTicketRemoveListener(Action<Ticket> action) {
        if (onTicketRemovedListeners == null) onTicketRemovedListeners = new ArrayList<>();
        onTicketRemovedListeners.add(action);
    }

    /**
     * Removes a ticket from the system.
     * And also removes the related file, if exists.
     *
     * @param ticket Ticket to remove.
     */
    public static void removeTicket(Ticket ticket) {
        boolean value = Helpers.tickets.remove(ticket);
        if (value) {
            File ticketFile = new File(Helpers.ROOT_FOLDER_PATH + "ticket-" + ticket.getId() + ".ticket");
            if (ticketFile.exists()) {
                ticketFile.delete();
            }
            message("Ticket " + ticket.toString() + " has been removed");
            if (onTicketRemovedListeners != null) onTicketRemovedListeners.forEach(it -> it.call(ticket));
        }
    }

    private static String tryLoadPreSavedLocations() {
        try {
            File file = new File(ROOT_FOLDER_PATH);
            Files.createDirectories(file.toPath());
            file = new File(ROOT_FOLDER_PATH + "locations.json");
            if (file.exists()) {
                byte[] arr = Files.readAllBytes(file.toPath());
                String result = new String(arr, "UTF-8");
                return result;
            }
        } catch (Exception e) {
            // ignored
        }

        return null;
    }

    /**
     * Gets the available terminals asynchronously. Thus, an action is required to handle.
     *
     * @param onCompleted An action with the array list of locations.
     */
    public static void getAvailableTerminals(Action<ArrayList<Location>> onCompleted) {
        String value = tryLoadPreSavedLocations();
        if (value != null) {
            parseLocations(value, onCompleted);
            return;
        }

        new Thread(() -> {
            try {
                Document document = Jsoup
                        .connect("https://www.metroturizm.com.tr/DataProvider/GetTerminals")
                        .timeout(5000)
                        .userAgent("Mozilla")
                        .post();

                String text = document.body().text();
                trySaveLocationsText(text);
                parseLocations(text, onCompleted);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void trySaveLocationsText(String text) {
        try {
            Files.write(
                    new File(ROOT_FOLDER_PATH + "locations.json").toPath(),
                    text.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseLocations(String source, Action<ArrayList<Location>> onCompleted) {
        ArrayList<Location> locations = new ArrayList<>();
        JSONObject response = new JSONObject(source);
        JSONArray array = response.getJSONArray("rc1");
        for (int i = 0, limit = array.length(); i < limit; i++) {
            JSONObject o = array.getJSONObject(i);
            Location location = new Location(o.getString("BRANCHNAME"), o.getString("BRANCHCODE"));
            locations.add(location);
        }

        if (onCompleted != null) onCompleted.call(locations);
    }

    /**
     * Adds a new ticket into the system.
     *
     * @param ticket Ticket to add.
     */
    public static void addTicket(Ticket ticket) {
        tickets.add(ticket);
        if (onTicketSavedListeners != null) onTicketSavedListeners.forEach(it -> it.call(ticket));
        message("New ticket has been added");
    }

    /**
     * Gets the journey list asynchronously. Thus, an action should be used.
     *
     * @param ticket      Ticket to get journey list for. (At this point, not all ticket information are filled)
     * @param onCompleted An action to handle list, with an array list of buses
     */
    public static void getJourneyList(Ticket ticket, Action<ArrayList<Bus>> onCompleted) {
        new Thread(() -> {
            try {
                Document document = Jsoup.connect("https://www.metroturizm.com.tr/DataProvider/GetJourneyList")
                        .timeout(10000)
                        .userAgent("Mozilla")
                        .data("boarding", ticket.getFrom().getHtmlValue())
                        .data("landing", ticket.getTo().getHtmlValue())
                        .data("boardingDate", BOARDING_DATE_FORMAT.format(ticket.getDate()))
                        .data("returnDate", BOARDING_DATE_FORMAT.format(new Date(ticket.getDate().getTime() + 1000 * 60 * 60 * 24)))
                        .data("isBothWay", "false")
                        .post();

                JSONObject response = new JSONObject(document.body().text());
                JSONObject bothWay = response.getJSONObject("bothWay");
                String connectedCity = response.getString("connectedCity");
                String connectedJourneyList = response.optString("connectedJourneyList");
                String depSuggestion = response.optString("depSuggestion");
                JSONObject oneWay = response.getJSONObject("oneWay");
                JSONArray rc1 = oneWay.getJSONArray("rc1");

                ArrayList<Bus> buses = new ArrayList<>();
                Date now = new Date();
                for (int i = buses.size() - 1; i >= 0; i--) {
                    Bus bus = buses.get(i);
                    if (bus.getLandingDate().before(now) || bus.getRemainingSeatNumber() == 0) {
                        buses.remove(i);
                        continue;
                    }
                }

                for (int i = 0, limit = rc1.length(); i < limit; i++) {
                    JSONObject object = rc1.getJSONObject(i);
                    Bus bus = new Bus(
                            object.optString("PLAKA", object.getString("UNIQUE_KEY")),
                            object.getInt("TOTALSEAT"),
                            ticket.getFrom(),
                            ticket.getTo()
                    );

                    String time = object.getString("BINIS_SAATI");

                    String dateStr = object.optString("SEFERDATE");
                    if (dateStr == null) dateStr = object.optString("B_DA_SEFERDATE");

                    Date date = MOVEMENT_DATE_FORMAT.parse(dateStr);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, 2)));
                    calendar.set(Calendar.MINUTE, Integer.parseInt(time.substring(3)));

                    bus.setMovementDate(calendar.getTime());
                    bus.setSoldSeat(object.getInt("GEN_SOLDSEAT"));
                    bus.setPrice(object.getDouble("GEN_WEBPRICE"));

                    dateStr = object.optString("INIS_ANAYOLDATE");
                    if (dateStr != null) dateStr = object.optString("INIS_ARAYOLDATE");
                    date = MOVEMENT_DATE_FORMAT.parse(dateStr);
                    time = object.getString("INIS_SAATI");

                    calendar.setTime(date);
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, 2)));
                    calendar.set(Calendar.MINUTE, Integer.parseInt(time.substring(3)));

                    bus.setLandingDate(calendar.getTime());

                    if (bus.getRemainingSeatNumber() > 0) {
                        buses.add(bus);
                    }
                }

                if (onCompleted != null) onCompleted.call(buses);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Extracts digits from a number.
     *
     * @param number Number to extract digits from.
     * @return Number array, for performance reasons, since non of element can be greater than 9, byte type is used.
     */
    public static byte[] getDigits(long number) {
        String value = String.valueOf(number);
        byte[] digits = new byte[value.length()];
        for (int i = 0, limit = value.length(); i < limit; i++) {
            digits[i] = Byte.parseByte(String.valueOf(value.charAt(i)));
        }

        return digits;
    }

    /**
     * Try load pre saved tickets boolean.
     *
     * @return the boolean
     */
    public static boolean tryLoadPreSavedTickets() {
        return tryLoadPreSavedTickets(null);
    }

    /**
     * Try load pre saved buses boolean.
     *
     * @return the boolean
     */
    public static boolean tryLoadPreSavedBuses() {
        return tryLoadPreSavedBuses(null);
    }

    /**
     * Emit a message to the status bar.
     *
     * @param value Message to show.
     */
    public static void message(String value) {
        if (messageHandler != null) messageHandler.call(value);
    }

    private static boolean tryLoadPreSavedBuses(Action<ArrayList<Bus>> onDone) {
        try {
            if (buses == null) buses = new ArrayList<>();
            File file = new File(ROOT_FOLDER_PATH);
            if (file.listFiles() == null) return false;

            for (File busFile : file.listFiles()) {
                if (!busFile.getName().startsWith("bus-")) continue;
                Bus bus = Bus.loadFromFile(busFile.getName());

                if (buses.contains(bus)) continue;
                buses.add(bus);
            }

            if (onDone != null) onDone.call(buses);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static boolean tryLoadPreSavedTickets(Action<ArrayList<Ticket>> onDone) {
        try {
            passengers = new ArrayList<>();
            tickets = new ArrayList<>();
            if (buses == null) buses = new ArrayList<>();
            baggages = new ArrayList<>();

            File file = new File(ROOT_FOLDER_PATH);
            if (file.listFiles() == null) return false;
            for (File ticketFile : file.listFiles()) {
                if (!ticketFile.getName().startsWith("ticket-")) continue;
                Ticket ticket = Ticket.loadFromFile(ticketFile.getName());

                Passenger passenger = ticket.getOwner();
                if (passenger != null && !passengers.contains(passenger)) passengers.add(passenger);

                Bus bus = ticket.getBus();

                if (!buses.contains(bus)) buses.add(bus);
                tickets.add(ticket);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
