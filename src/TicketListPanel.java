import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The type Ticket list panel.
 */
public class TicketListPanel extends JPanel {
    private JTable tableTicket;
    private ReadOnlyTableModel model;
    private JLabel totalTicketLabel;

    /**
     * Instantiates a new Ticket list panel.
     */
    public TicketListPanel() {
        initComponents();
        initEvents();
        initLayout();
    }

    private void initLayout() {
        setLayout(new MigLayout());
        add(new JScrollPane(tableTicket), "grow, push, span");
        add(totalTicketLabel = new JLabel("Total Ticket Count: " + Helpers.tickets.size()), "pushx");

        JLabel infoLabel = new JLabel("<html><font color=\"gray\">Double click to view ticket detail</font></html>");
        infoLabel.setFont(infoLabel.getFont().deriveFont(10.0f).deriveFont(Font.ITALIC));

        add(infoLabel);
    }

    private void initEvents() {
        tableTicket.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                int row = tableTicket.rowAtPoint(point);
                if (row == -1) return;
                if (e.getClickCount() != 2) return;

                Ticket ticket = (Ticket) model.getValueAt(tableTicket.getSelectedRow(), 0);
                TicketDialog frame = new TicketDialog(ticket);

                frame.setModal(true);
                frame.setVisible(true);
            }
        });

        Helpers.addOnTicketSavedListener(ticket -> {
            model.addRow(new Object[]{ticket, ticket.getBus(), ticket.getOwner(), ticket.getSeatNumber(), ticket.getBaggage() == null ? "None" : ticket.getBaggage()});
            totalTicketLabel.setText("Total Ticket Count: " + Helpers.tickets.size());
        });

        Helpers.addOnTicketRemoveListener(ticket -> {
            for (int i = 0, limit = model.getRowCount(); i < limit; i++) {
                Ticket rowTicket = (Ticket) model.getValueAt(i, 0);
                if (ticket.getId().equals(rowTicket.getId())) {
                    model.removeRow(i);
                    totalTicketLabel.setText("Total Ticket Count: " + Helpers.tickets.size());
                    break;
                }
            }
        });
    }

    private void initComponents() {
        tableTicket = new JTable(model = new ReadOnlyTableModel(new Object[]{
                "Number", "Direction", "Owner", "Seat", "Baggage"
        }, 0));

        for (Ticket ticket : Helpers.tickets) {
            model.addRow(new Object[]{
                    ticket, ticket.getBus(), ticket.getOwner(), ticket.getSeatNumber(), ticket.getBaggage() == null ? "None" : ticket.getBaggage()
            });
        }
    }
}
