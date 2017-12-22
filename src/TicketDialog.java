import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

/**
 * The type Ticket dialog.
 */
public class TicketDialog extends JDialog {
    private Ticket ticket;
    private JTextField tfOwnerName, tfFrom, tfTo, tfBaggage, tfMovementDate, tfLandingDate;
    private JButton btnDelete, btnClose;

    /**
     * Instantiates a new Ticket dialog.
     *
     * @param ticket the ticket
     */
    TicketDialog(Ticket ticket) {
        this.ticket = ticket;
        initComponents();
        initEvents();
        initLayout();
        fillFields();
    }

    private void initLayout() {
        setLayout(new MigLayout());

        add(new JLabel("Owner:"));
        add(tfOwnerName, "gap r, pushx, growx, wrap");

        add(new JLabel("From:"));
        add(tfFrom, "gap r, pushx, growx, wrap");

        add(new JLabel("To:"));
        add(tfTo, "gap r, pushx, growx, wrap");

        add(new JLabel("Movement Date:"));
        add(tfMovementDate, "gap r, pushx, growx, wrap");

        add(new JLabel("Landing Date:"));
        add(tfLandingDate, "gap r, pushx, growx, wrap");

        if (ticket.getBaggage() != null) {
            add(new JLabel("Baggage:"));
            add(tfBaggage, "gap r, pushx, growx, wrap");
        }

        add(btnDelete);
        add(btnClose, "gapleft push, align right");

        setSize(400, 218);
        setResizable(false);
    }

    private void initComponents() {
        tfOwnerName = new JFormattedTextField();
        tfBaggage = new JFormattedTextField();
        tfFrom = new JFormattedTextField();
        tfLandingDate = new JFormattedTextField();
        tfMovementDate = new JFormattedTextField();
        tfTo = new JFormattedTextField();
        btnClose = new JButton("Close");
        btnDelete = new JButton("Delete");

        btnClose.setBackground(Color.PINK);
        btnDelete.setBackground(Color.RED);

        tfOwnerName.setEditable(false);
        tfBaggage.setEditable(false);
        tfFrom.setEditable(false);
        tfLandingDate.setEditable(false);
        tfMovementDate.setEditable(false);
        tfTo.setEditable(false);

        tfOwnerName.setFocusable(false);
        tfBaggage.setFocusable(false);
        tfFrom.setFocusable(false);
        tfLandingDate.setFocusable(false);
        tfMovementDate.setFocusable(false);
        tfTo.setFocusable(false);

        btnDelete.setFocusable(false);
    }

    private void initEvents() {
        btnClose.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        btnDelete.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure to delete ticket " + ticket.toString() + "?",
                    "Confirm Dialog",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (result != JOptionPane.YES_OPTION) return;
            Helpers.removeTicket(ticket);
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
    }

    private void fillFields() {
        tfOwnerName.setText(ticket.getOwner().toString());
        tfFrom.setText(ticket.getBus().getFrom().getName());
        tfTo.setText(ticket.getBus().getTo().getName());
        tfMovementDate.setText(Helpers.NORMAL_DATE_FORMAT.format(ticket.getBus().getMovementDate()));
        tfLandingDate.setText(Helpers.NORMAL_DATE_FORMAT.format(ticket.getBus().getLandingDate()));

        if (ticket.getBaggage() != null) {
            tfBaggage.setText(ticket.getBaggage().toString());
        }
    }
}
