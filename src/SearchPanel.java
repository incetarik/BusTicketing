import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * The type Search panel.
 */
public class SearchPanel extends JPanel {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    private static Calendar CALENDAR = Calendar.getInstance();
    private static String[] MONTHS = new String[]{
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };
    private JComboBox<Location> comboFrom, comboTo;
    private ArrayList<Location> locations;
    private JTable tableAvailableBuses;
    private ReadOnlyTableModel model;
    private JButton btnSearch;
    private JCheckBox checkBothWay;
    private Location from, to;
    private JPanel conditionsPanel;
    private JComboBox<Integer> comboDay;
    private JComboBox<String> comboMonth;
    private ArrayList<Action<Bus>> onBusSelectedListeners;
    private boolean isUpdating = false;
    private boolean isRemovingBuses = false;
    private BusPanel busPanel;

    /**
     * Instantiates a new Search panel.
     *
     * @param busPanel the bus panel
     */
    SearchPanel(BusPanel busPanel) {
        setLayout(new MigLayout());
        initComponents();
        initEvents();
        initLayout();

        this.busPanel = busPanel;
    }

    /**
     * Adds a bus selected listener.
     *
     * @param listener An action with a bus.
     */
    public void addOnBusSelectedListener(Action<Bus> listener) {
        (onBusSelectedListeners == null ? (onBusSelectedListeners = new ArrayList<>()) : onBusSelectedListeners).add(listener);
    }

    private void initComponents() {
        CALENDAR.setTime(new Date());

        comboDay = new JComboBox<>();
        comboMonth = new JComboBox<>();
        comboFrom = new JComboBox<>();
        comboTo = new JComboBox<>();
        btnSearch = new JButton("Search");
        checkBothWay = new JCheckBox("Both Way Ticket");

        comboFrom.setEnabled(false);
        comboTo.setEnabled(false);

        model = new ReadOnlyTableModel(new Object[]{
                "Route", "Price", "Movement Date", "Landing Date", "Remaining Seat"
        }, 0);

        tableAvailableBuses = new JTable(model);
        tableAvailableBuses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableAvailableBuses.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        TableColumnModel colModel = tableAvailableBuses.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(300);

        checkBothWay = new JCheckBox("Both Way");

        for (int i = 1; i <= CALENDAR.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            comboDay.addItem(i);
        }

        comboDay.setSelectedIndex(CALENDAR.get(Calendar.DAY_OF_MONTH) - 1);

        for (int i = 0; i < 12; i++) {
            comboMonth.addItem(MONTHS[i]);
        }

        comboMonth.setSelectedIndex(CALENDAR.get(Calendar.MONTH));
        conditionsPanel = new JPanel(new MigLayout("insets 0"));
    }

    private void initLayout() {
        JPanel left = new JPanel(new MigLayout());
        left.add(new JLabel("From:"));
        left.add(comboFrom);

        JPanel right = new JPanel(new MigLayout());
        right.add(new JLabel("To:"));
        right.add(comboTo);

        JPanel date = new JPanel(new MigLayout());
        date.add(new JLabel("Date:"));
        date.add(comboDay);
        date.add(comboMonth);

        conditionsPanel.add(left, "split 3");
        conditionsPanel.add(right);
        conditionsPanel.add(date);

        add(conditionsPanel, "pushx, growx");
        add(btnSearch, "gapleft push, align right, wrap");
        add(new JSeparator(JSeparator.HORIZONTAL), "span, pushx, growx, wrap");
        add(new JScrollPane(tableAvailableBuses), "span, push, grow");

        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0, 0, 0, 25)));
    }

    private void setEnabledConditions(boolean value) {
        comboDay.setEnabled(value);
        comboMonth.setEnabled(value);
        comboTo.setEnabled(value);
        comboFrom.setEnabled(value);
        revalidate();
    }

    private void initEvents() {
        comboFrom.addItemListener(e -> {
            Location location = (Location) e.getItem();
            if (e.getStateChange() == ItemEvent.SELECTED) {
                from = location;
                comboTo.removeAll();

                for (Location target : locations) {
                    if (target == from) continue;
                    comboTo.addItem(target);
                }
            }

            comboTo.setEnabled(true);
        });

        comboTo.addItemListener(e -> {
            Location location = (Location) e.getItem();
            if (e.getStateChange() == ItemEvent.SELECTED) {
                to = location;
            }
        });

        comboMonth.addItemListener(e -> {
            String month = (String) e.getItem();
            if (e.getStateChange() == ItemEvent.SELECTED) {
                int index = 0;
                while (index++ < 12 && !MONTHS[index].equals(month)) ;

                CALENDAR.set(Calendar.MONTH, index);
                int dayCount = CALENDAR.getActualMaximum(Calendar.DAY_OF_MONTH);

                int selectedDayIndex = comboDay.getSelectedIndex();
                if (selectedDayIndex > dayCount - 1) selectedDayIndex = dayCount - 1;

                comboDay.removeAllItems();
                for (int day = 1; day <= dayCount; day++) comboDay.addItem(day);
                comboDay.setSelectedIndex(selectedDayIndex);
            }
        });

        comboDay.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) return;
            CALENDAR.set(Calendar.DAY_OF_MONTH, (Integer) e.getItem());
        });

        btnSearch.addActionListener(e -> {
            Ticket ticket = new Ticket();
            ticket.setFrom(from = (Location) comboFrom.getSelectedItem());
            ticket.setTo(to = (Location) comboTo.getSelectedItem());
            ticket.setDate(CALENDAR.getTime());

            setEnabledConditions(false);
            isRemovingBuses = true;
            while (model.getRowCount() > 0) model.removeRow(0);
            busPanel.setBus(null);

            Helpers.getJourneyList(ticket, buses -> {
                buses.sort(Bus::compareTo);

                for (Bus bus : buses) {
                    String priceStr = String.valueOf(bus.getPrice());
                    if (((int) bus.getPrice()) == bus.getPrice()) {
                        priceStr = String.valueOf((int) bus.getPrice());
                    }

                    model.addRow(new Object[]{
                            bus,
                            priceStr + " TL",
                            DATE_FORMAT.format(bus.getMovementDate()),
                            DATE_FORMAT.format(bus.getLandingDate()),
                            (bus.getRemainingSeatNumber())
                    });
                }

                isRemovingBuses = false;

                setEnabledConditions(true);
            });
        });

        ListSelectionModel selectionModel = tableAvailableBuses.getSelectionModel();

        final boolean[] checker = new boolean[]{false};
        selectionModel.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || checker[0] || model.getRowCount() == 0 || isRemovingBuses) return;
            try {
                checker[0] = true;
                int index = tableAvailableBuses.getSelectedRow();
                Bus bus = (Bus) model.getValueAt(index, 0);
                if (onBusSelectedListeners != null) onBusSelectedListeners.forEach(it -> it.call(bus));
                checker[0] = false;
            } catch (Exception ex) {
                // ignored
            }
        });

        Helpers.addOnLocationConditionChangedListener(cond -> {
            if (from.getName().split(" ")[0].equals(to.getName().split(" ")[0]))
                while (model.getRowCount() > 0) model.removeRow(0);
            comboTo.removeAllItems();
            ArrayList<String> places = cond ? new ArrayList<>() : null;

            Location selectedLocation = (Location) comboFrom.getSelectedItem();
            for (int i = 0, limit = comboFrom.getItemCount(); i < limit; i++) {
                Location location = comboFrom.getItemAt(i);
                if (cond) {
                    String cityName = location.getName().split(" ")[0];
                    if (places.contains(cityName)) continue;
                    places.add(cityName);

                    if (location.equals(selectedLocation)) continue;
                }

                if (!location.equals(selectedLocation)) {
                    comboTo.addItem(location);
                }
            }

            if (!cond) return;
        });
    }

    /**
     * Updates the possible/available routes.
     *
     * @param action An action to call with the result, as an array list of the locations available.
     */
    public void updateList(final Action<ArrayList<Location>> action) {
        if (isUpdating) return;

        isUpdating = true;
        Helpers.getAvailableTerminals(value -> {
            locations = value;
            comboFrom.setEnabled(true);

            action.call(value);
            isUpdating = false;
        });
    }

    /**
     * Updates the possible/available routes.
     */
    public void updateList() {
        updateList(value -> {
            locations = value;

            String firstLocationName = locations.get(0).getName();
            if (firstLocationName.contains(" ")) firstLocationName = firstLocationName.split(" ")[0];

            for (Location location : value) {
                String locationName = location.getName();
                comboFrom.addItem(location);

                if (!locationName.contains(firstLocationName)) comboTo.addItem(location);
            }

            comboTo.setEnabled(true);
        });
    }
}
