import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.ParseException;

/**
 * The type Purchase dialog.
 */
public class PurchaseDialog extends JDialog {
    private Action<Ticket> onResultListener;
    private int seat;
    private Bus bus;
    private Ticket ticket;
    private Passenger passenger;

    private JCheckBox cbHasBaggage, cbIsMale;
    private JFormattedTextField tfpName, tfpSurname, tfpPhone, tfpId, tfpBaggageWeight;
    private JButton btnCancel, btnOk;

    /**
     * Instantiates a new Purchase dialog.
     *
     * @param bus  the bus
     * @param seat the seat
     */
    PurchaseDialog(Bus bus, int seat) {
        this.seat = seat;
        this.bus = bus;

        initComponents();
        initEvents();
        initLayout();
    }

    /**
     * Instantiates a new Purchase dialog.
     *
     * @param bus    the bus
     * @param ticket the ticket
     */
    public PurchaseDialog(Bus bus, Ticket ticket) {
        this.seat = ticket.getSeatNumber();
        this.bus = bus;
        this.ticket = ticket;
        this.passenger = ticket.getOwner();

        initComponents();
        initEvents();
        initLayout();
    }

    /**
     * Sets a result listener.
     *
     * @param onPurchasedListener An action with Ticket.
     */
    public void setOnResultListener(Action<Ticket> onPurchasedListener) {
        this.onResultListener = onPurchasedListener;
    }

    private void initComponents() {
        MaskFormatter formatter = null;

        try {
            formatter = new MaskFormatter("###-###-####");
            formatter.setPlaceholderCharacter('_');
            formatter.setAllowsInvalid(false);

            tfpPhone = new JFormattedTextField(formatter);
            tfpPhone.setFocusLostBehavior(JFormattedTextField.PERSIST);

            formatter = new MaskFormatter("????????????????????????");
            formatter.setAllowsInvalid(false);
            tfpName = new JFormattedTextField(formatter);
            tfpName.setFocusLostBehavior(JFormattedTextField.PERSIST);

            formatter = new MaskFormatter("?????????????????????????");
            formatter.setAllowsInvalid(false);
            tfpSurname = new JFormattedTextField(formatter);
            tfpSurname.setFocusLostBehavior(JFormattedTextField.PERSIST);

            formatter = new MaskFormatter("###########");
            formatter.setAllowsInvalid(false);
            formatter.setCommitsOnValidEdit(true);
            tfpId = new JFormattedTextField(formatter);
            tfpId.setFocusLostBehavior(JFormattedTextField.PERSIST);

            formatter = new MaskFormatter("##.#");
            formatter.setAllowsInvalid(false);
            formatter.setCommitsOnValidEdit(true);
            tfpBaggageWeight = new JFormattedTextField(formatter);
            tfpBaggageWeight.setVisible(false);
            tfpBaggageWeight.setFocusLostBehavior(JFormattedTextField.PERSIST);

            cbHasBaggage = new JCheckBox("Has Baggage");
            cbIsMale = new JCheckBox("Is Male");

            btnCancel = new JButton("Cancel");
            btnOk = new JButton("Purchase");

            btnCancel.setBackground(new Color(190, 25, 25, 255));
            btnOk.setBackground(new Color(25, 255, 25, 255));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void initLayout() {
        setLayout(new MigLayout());
        setTitle("Purchasing a Ticket for " + bus.toString());

        add(new JLabel("<html><body>" +
                String.format("You are going to purchase a ticket for the route " +
                        "<font color=\"orange\">%s</font> at " +
                        "<font color=\"red\">%s</font>.", bus.toString(), bus.getMovementDate()) +
                "<br>" +
                "If you are <b>NOT</b> sure or mistakenly chosen this, you can cancel below." +
                "</body></html>"), "span");

        add(new JLabel("Your ID:"));
        add(tfpId, "gap r, pushx, growx, width 300::, wrap");

        add(new JLabel("Your Name:"));
        add(tfpName, "gap r, pushx, growx, wrap");

        add(new JLabel("Your Surname:"));
        add(tfpSurname, "gap r, pushx, growx, wrap");

        add(new JLabel("Your Phone:"));
        add(tfpPhone, "gap r, pushx, growx, wrap");

        add(cbHasBaggage);
        add(tfpBaggageWeight, "gap r, pushx, growx, wrap");

        add(cbIsMale, "wrap");

        add(btnCancel);
        add(btnOk, "gapleft push, wrap");
    }

    private boolean isIdentifierValid(long id) {
        byte[] digits = Helpers.getDigits(id);
        if (digits.length != 11) return false;

        int totalOdd = 0, totalEven = 0;
        for (int i = 0; i < 9; i++) {
            if (i % 2 == 0) {
                totalOdd += digits[i];
            } else {
                totalEven += digits[i];
            }
        }

        int total = totalOdd + totalEven + digits[9];
        int lastDigit = total % 10;

        if (lastDigit == digits[10]) {
            int check = (totalOdd * 7 - totalEven) % 10;
            return digits[9] == check;
        }

        return false;
    }

    private void initEvents() {
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                Action<Ticket> listener = PurchaseDialog.this.onResultListener;
                if (listener == null) return;
                listener.call(ticket);
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

        btnCancel.addActionListener(e -> {
            ticket = null;
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
        btnOk.addActionListener(e -> {
            try {
                if (!tfpId.isEditValid() || !isIdentifierValid(Long.parseLong(tfpId.getText()))) {
                    showError("TC/ID was not true", "Validation Error");
                    return;
                }
            } catch (Exception ex) {
                showError("TC/ID was not true", "Validation Error");
            }

            if (tfpName.getText().length() < 3) {
                showError("Name field cannot be smaller than 3 characters", "Validation Error");
                return;
            }

            if (tfpSurname.getText().length() < 3) {
                showError("Surname field cannot be smaller than 3 characters", "Validation Error");
                return;
            }

            if (!tfpPhone.isEditValid()) {
                showError("Please enter a valid phone number");
                return;
            }

            Ticket ticket = null;
            if (this.ticket == null) {
                this.ticket = ticket = new Ticket();
                ticket.setBus(bus);
                ticket.setSeatNumber(seat);
            }

            if (passenger == null) {
                passenger = new Passenger();
                passenger.setId(Long.parseLong(tfpId.getText()));
                passenger.setName(tfpName.getText());
                passenger.setSurname(tfpSurname.getText());
                passenger.setPhone(tfpPhone.getText());
                passenger.setMale(cbIsMale.isSelected());
                bus.getPassengers().add(passenger);
                ticket.setOwner(passenger);
            }

            if (cbHasBaggage.isSelected() && tfpBaggageWeight.isEditValid()) {
                Baggage baggage = new Baggage();
                baggage.setOwner(this.passenger);
                baggage.setWeight(Float.parseFloat(tfpBaggageWeight.getText()));
                ticket.setBaggage(baggage);
            }

            boolean result = ticket.trySave();
            if (onResultListener != null) onResultListener.call(ticket);
            Helpers.addTicket(ticket);
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });

        cbHasBaggage.addActionListener(e -> tfpBaggageWeight.setVisible(cbHasBaggage.isSelected()));
        tfpBaggageWeight.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                String str = tfpBaggageWeight.getText().trim();
                if (str.contains(".")) {
                    if (str.length() == 4) return;
                    if (str.charAt(1) == ' ') {
                        tfpBaggageWeight.setText("0" + str.charAt(0) + ".0");
                    } else {
                        tfpBaggageWeight.setText(str + '0');
                    }
                }
            }
        });
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showError(String message) {
        showError(message, "Error");
    }
}
