import net.miginfocom.swing.MigLayout;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * The type Main frame.
 */
public class MainFrame extends JFrame {
    private SearchPanel searchPanel;
    private BusPanel busPanel;

    /**
     * Instantiates a new Main frame.
     */
    public MainFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTheme();
        initComponents();
        initLayout();
        initMenu();
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        Helpers.tryLoadPreSavedBuses();
        Helpers.tryLoadPreSavedTickets();
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem fmImportTickets = new JMenuItem("Import Tickets");
        JMenuItem fmExportTickets = new JMenuItem("Export Tickets");
        JMenuItem fmExit = new JMenuItem("Exit");

        fileMenu.add(fmImportTickets);
        fileMenu.add(fmExportTickets);
        fileMenu.addSeparator();
        fileMenu.add(fmExit);
        menuBar.add(fileMenu);

        JMenu settingsMenu = new JMenu("Settings");
        JCheckBoxMenuItem smExcludeSameLocations = new JCheckBoxMenuItem("Exclude Same Locations");
        settingsMenu.add(smExcludeSameLocations);
        JMenu smThemes = new JMenu("Theme");

        for (String theme :
                ("AluOxide:de.javasoft.plaf.synthetica.SyntheticaAluOxideLookAndFeel;" +
                        "OrangeMetallic:de.javasoft.plaf.synthetica.SyntheticaOrangeMetallicLookAndFeel;" +
                        "Simple-2D:de.javasoft.plaf.synthetica.SyntheticaSimple2DLookAndFeel;" +
                        "Sky Metallic:de.javasoft.plaf.synthetica.SyntheticaSkyMetallicLookAndFeel").split(";")
        ) {
            String[] parts = theme.split(":");
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(parts[0]);
            item.setSelected(parts[0].equals("AluOxide"));
            item.addActionListener(e -> {
                setTheme(parts[1]);
                for (Component component : smThemes.getMenuComponents()) {
                    if (component == item) continue;
                    ((JRadioButtonMenuItem)component).setSelected(false);
                }
            });
            smThemes.add(item);
        }

        settingsMenu.add(smThemes);

        menuBar.add(settingsMenu);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem hmAbout = new JMenuItem("About");
        helpMenu.add(hmAbout);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
        menuBar.revalidate();

        fmImportTickets.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    String bytes = new String(Files.readAllBytes(file.toPath()));
                    JSONArray array = new JSONArray(bytes);
                    for (int i = 0, limit = array.length(); i < limit; i++) {
                        JSONObject ticketObject = array.getJSONObject(i);
                        Ticket readTicket = Helpers.JSON_MAPPER.readValue(ticketObject.toString().getBytes(), Ticket.class);

                        if (Helpers.tickets.contains(readTicket)) continue;
                        Helpers.addTicket(readTicket);
                        readTicket.trySave();
                    }

                    Helpers.message("All " + array.length() + " tickets has/have been imported");
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Helpers.message("Could not imported");
                }
            }
        });
        fmExportTickets.addActionListener(e -> {
            JSONArray arr = new JSONArray(Helpers.tickets.parallelStream().map(it -> it.toJson()).toArray());
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    Files.write(
                            file.toPath(),
                            arr.toString().getBytes(),
                            StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING
                    );

                    Helpers.message("Tickets exported!");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        });
        fmExit.addActionListener(e -> this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        smExcludeSameLocations.addActionListener(e -> Helpers.setLocationCondition(smExcludeSameLocations.isSelected()));

        hmAbout.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                    this,
                    "<html>" +
                            "<body>" +
                            "<p>CMPE 331 Project [Ticketing]</p><br>" +
                            "<p>Project Group Members:<p><br>" +
                            "<ul>" +
                            "<li>Tarık İNCE</li>" +
                            "<li>Ayah Kunbaz</li>" +
                            "<li>Aysun Suer</li>" +
                            "</ul>" +
                            "</body>" +
                            "</html>",
                    "About",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void initLayout() {
        setLayout(new MigLayout());

        Dimension size = new Dimension(800, 500);
        this.setSize(size);

        JTabbedPane tabbedPane = new JTabbedPane();

        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, searchPanel, busPanel);
        pane.setDividerLocation(0.65);
        pane.setResizeWeight(0.65);

        TicketListPanel ticketListPanel = new TicketListPanel();

        ImageIcon ticketIcon = null, listIcon = null;
        File file = new File("assets/ticket.png");

        if (!file.exists()) {
            InputStream in = getClass().getResourceAsStream("ticket.png");
            try {
                ticketIcon = new ImageIcon(ImageIO.read(in));
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                in.close();
                in = getClass().getResourceAsStream("list.png");
                listIcon = new ImageIcon(ImageIO.read(in));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            ticketIcon = new ImageIcon("assets/ticket.png");
            listIcon = new ImageIcon("assets/list.png");
        }

        tabbedPane.addTab("Reservation", ticketIcon, pane);
        tabbedPane.addTab("Ticket List", listIcon, ticketListPanel);

        add(tabbedPane, "grow, push, wrap");
        JLabel lblMessage = new JLabel("System: Working");

        Helpers.messageHandler = lblMessage::setText;
        add(lblMessage, "span, pushx, growx");
    }

    private void initComponents() {
        busPanel = new BusPanel(null);
        searchPanel = new SearchPanel(busPanel);
        searchPanel.updateList();
        searchPanel.addOnBusSelectedListener(bus -> busPanel.setBus(bus));
    }

    private void setTheme(String theme) {
        try {
            UIManager.setLookAndFeel(theme);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.updateComponentTreeUI(this);
    }

    private void setTheme() {
        setTheme("de.javasoft.plaf.synthetica.SyntheticaAluOxideLookAndFeel");
    }
}