import javax.swing.table.DefaultTableModel;

/**
 * Readonly table model which extends the default table model.
 * This is used application-wide to prevent edits in cells.
 */
public class ReadOnlyTableModel extends DefaultTableModel {
    /**
     * Instantiates a new Read only table model.
     *
     * @param objects the objects
     * @param i       the
     */
    ReadOnlyTableModel(Object[] objects, int i) {
        super(objects, i);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
