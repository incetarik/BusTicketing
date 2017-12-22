import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * The type Bus panel.
 */
public class BusPanel extends JPanel implements Waitable {
    private Bus bus;
    private ArrayList<JCheckBox> checkBoxSeats;
    private ArrayList<Action<Integer>> onSeatSelectedListeners;
    private boolean isReady = false;

    /**
     * Instantiates a new Bus panel.
     *
     * @param bus the bus
     */
    public BusPanel(Bus bus) {
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(0, 0, 0, 25)));
        setBus(bus);

        Helpers.addOnTicketRemoveListener(e -> {
            if (!e.getBus().equals(this.getBus())) return;
            JCheckBox cb = this.checkBoxSeats.get(e.getSeatNumber() - 1);
            cb.setEnabled(true);
            cb.setSelected(false);
        });
    }

    /**
     * Gets the bus attached to this panel.
     *
     * @return The bus attached to this panel.
     */
    public Bus getBus() {
        return this.bus;
    }

    /**
     * Attaches a bus to this panel.
     *
     * @param bus New bus to attach.
     */
    public void setBus(Bus bus) {
        if (this.checkBoxSeats != null) {
            this.checkBoxSeats.forEach(this::remove);
        }

        invalidate();
        revalidate();
        repaint();

        if (bus == null) return;

        this.bus = bus;
        this.checkBoxSeats = new ArrayList<>(bus.getSeatNumber());

        for (int i = 0, limit = bus.getSeatNumber(); i < limit; i++) {
            final int j = i + 1;
            JCheckBox checkBox = new JCheckBox(String.valueOf(j));
            checkBox.addActionListener(e -> {
                if (!isReady) return;
                selectSeat(j);
            });

            checkBoxSeats.add(checkBox);
        }

        for (Ticket ticket : Helpers.tickets) {
            if (!ticket.getBus().equals(bus)) continue;
            JCheckBox cb = this.checkBoxSeats.get(ticket.getSeatNumber() - 1);
            cb.setSelected(true);
            cb.setEnabled(false);
        }

        initSeats();

        isReady = true;
    }

    /**
     * Adds a seat selected listener.
     *
     * @param action Listener.
     */
    public void addOnSeatSelectedListener(Action<Integer> action) {
        if (onSeatSelectedListeners == null) onSeatSelectedListeners = new ArrayList<>();
        onSeatSelectedListeners.add(action);
    }

    private void selectSeat(int index) {
        JCheckBox checkBox = checkBoxSeats.get(index - 1);
        PurchaseDialog dialog = new PurchaseDialog(bus, index);
        dialog.setModal(true);

        setWaiting(true);
        dialog.setOnResultListener(ticket -> {
            if (ticket == null) {
                checkBox.setSelected(false);
            } else {
                checkBox.setSelected(true);
                checkBox.setEnabled(false);
                checkBox.setBackground(ticket.getOwner().isMale() ? Color.CYAN : Color.PINK);
            }

            setWaiting(false);
        });

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void initSeats() {
        if (checkBoxSeats == null) {

        } else {
            setLayout(new MigLayout());
            Random random = new Random();
            for (int i = 0, limit = checkBoxSeats.size(), soldSeats = bus.getSoldSeat(); i < limit; i++) {
                JCheckBox checkBox = checkBoxSeats.get(i);

                if (soldSeats-- > 0) {
                    checkBox.setEnabled(false);
                    checkBox.setSelected(true);
                    checkBox.setBackground(random.nextBoolean() ? Color.CYAN : Color.PINK);
                }

                switch (i % 4) {
                    case 0:
                    case 2:
                        add(checkBox, "gapleft 30");
                        break;

                    case 1:
                        add(checkBox);
                        break;
                    case 3:
                        add(checkBox, "wrap");
                        break;
                }
            }

            revalidate();
        }
    }

    private void initComponents() {
        initSeats();
    }

    @Override
    public void setWaiting(boolean value) {
        JRootPane pane = getRootPane();
        Component glassPane = pane.getGlassPane();

        if (value) {
            pane.setGlassPane(glassPane = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    g.setColor(new Color(0, 0, 0, 100));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    super.paintComponent(g);
                }
            });

            glassPane.setVisible(true);
        } else {
            if (glassPane == null) return;
            glassPane.setVisible(false);
        }

        revalidate();
    }
}
