/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.priv;

/**
 *
 * @author dimkar
 */
/********************************************************************************
 *
 * GTableModel Class
 * Author   : Amir Kost
 * Purpose  : A TableModel that enables column sorting by clicking the column
 *
 * This class extends the DefaultTableModel class and enables sorting of columns
 * It sorts the TableModel's dataVector whenever a column is clicked.
 * Clicking the same column again causes it to be sorted in reverse order.
 * The sort is based on Java's Collection class sort method.
 *
 * Usage :
 *
 * GTableModel model = new GTableModel();
 * JTable table = new JTable(model);
 * model.addMouseListenerToHeaderInTable(table);
 *
 *******************************************************************************/

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.Comparator;
import java.util.Collections;

import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.event.TableModelEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

public class GTableModel extends DefaultTableModel implements Comparator{

    protected   int         currCol;
    protected   Vector      ascendCol;  // this vector stores the state (ascending or descending) of each column
    protected   Integer     one         = new Integer(1);
    protected   Integer     minusOne    = new Integer(-1);

    
   

    public GTableModel() {
        super();
        ascendCol = new Vector();
        //data = new Record[numRows];
       
        // Fill our table with random data (from the Record() constructor).
        //for (int i = 0; i < data.length; i++) {
          // data[i] = new Record();
        //}
  
    }

    /*******************************************************************
     * addColumn methods are inherited from the DefaultTableModel class.
     *******************************************************************/

    public void addColumn(Object columnName) {
        super.addColumn(columnName);
        ascendCol.add(one);
    }

    public void addColumn(Object columnName, Object[] columnData) {
        super.addColumn(columnName, columnData);
        ascendCol.add(one);
    }

    public void addColumn(Object columnName, Vector columnData) {
        super.addColumn(columnName, columnData);
        ascendCol.add(one);
    }

    /*****************************************************************
     * This method is the implementation of the Comparator interface.
     * It is used for sorting the rows
     *****************************************************************/
    public int compare(Object v1, Object v2) {

        // the comparison is between 2 vectors, each representing a row
        // the comparison is done between 2 objects from the different rows that are in the column that is being sorted

        int ascending = ((Integer) ascendCol.get(currCol)).intValue();
        if (v1 == null && v2 == null) {
            return 0;
        } else if (v2 == null) { // Define null less than everything.
            return 1 * ascending;
        } else if (v1 == null) {
            return -1 * ascending;
        }

        Object o1 = ((Vector) v1).get(currCol);
        Object o2 = ((Vector) v2).get(currCol);

        // If both values are null, return 0.
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o2 == null) { // Define null less than everything.
            return 1 * ascending;
        } else if (o1 == null) {
            return -1 * ascending;
        }

        if (o1 instanceof Number && o2 instanceof Number) {
            Number n1 = (Number) o1;
            double d1 = n1.doubleValue();
            Number n2 = (Number) o2;
            double d2 = n2.doubleValue();

            if (d1 == d2) {
                return 0;
            } else if (d1 > d2) {
                return 1 * ascending;
            } else {
                return -1 * ascending;
            }

        } else if (o1 instanceof Boolean && o2 instanceof Boolean) {
            Boolean bool1 = (Boolean) o1;
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean) o2;
            boolean b2 = bool2.booleanValue();

            if (b1 == b2) {
                return 0;
            } else if (b1) {
                return 1 * ascending;
            } else {
                return -1 * ascending;
            }

        } else {
            // default case
            if (o1 instanceof Comparable && o2 instanceof Comparable) {
                Comparable c1 = (Comparable) o1;
                Comparable c2 = (Comparable) o2; // superflous cast, no need for it!

                try {
                    return c1.compareTo(c2) * ascending;
                } catch (ClassCastException cce) {
                    // forget it... we'll deal with them like 2 normal objects below.
                }
            }

            String s1 = o1.toString();
            String s2 = o2.toString();
            return s1.compareTo(s2) * ascending;
        }
    }

    /***************************************************************************
     * This method sorts the rows using Java's Collections class.
     * After sorting, it changes the state of the column -
     * if the column was ascending, its new state is descending, and vice versa.
     ***************************************************************************/
    public void sort() {
        Collections.sort(dataVector, this);
        Integer val = (Integer) ascendCol.get(currCol);
        ascendCol.remove(currCol);
        if(val.equals(one)) // change the state of the column
            ascendCol.add(currCol, minusOne);
        else
            ascendCol.add(currCol, one);
    }

    public void sortByColumn(int column) {
        this.currCol = column;
        sort();
        fireTableChanged(new TableModelEvent(this));
    }

    // Add a mouse listener to the Table to trigger a table sort
    // when a column heading is clicked in the JTable.
    public void addMouseListenerToHeaderInTable(JTable table) {
        final GTableModel sorter = this;
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (e.getClickCount() == 1 && column != -1) {
                    int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK;
                    boolean ascending = (shiftPressed == 0);
                    sorter.sortByColumn(column);
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }

    
    
  
   
  }
    
    
       
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
