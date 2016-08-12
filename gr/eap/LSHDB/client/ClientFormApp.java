/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.client;

// Imports
import gr.eap.LSHDB.Key;
import gr.eap.LSHDB.Server_Thread;
import gr.eap.LSHDB.StoreInitException;
import gr.eap.LSHDB.util.Record;
import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Result;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.Border;

import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.java.dev.designgridlayout.DesignGridLayout;

public class ClientFormApp extends JFrame {

    // Instance attributes used in this example
    private JPanel topPanel;
    private JPanel dPanel = new JPanel();
    private JTable dTable;
    private JScrollPane dScrollPane;
    TitledBorder tb;
    WaitLayerUI layerUI;
    JLayer<JComponent> jlayer;
    String server = "localhost";
    int port = 4443;
    boolean performComparisons = true;
    String dbName = "";
    int maxQueryRows = 20;
    String[] labels;
    String[] descriptions;
    char[] mnemonics;
    int[] widths;
    Vector<String> dbNames;
    JTextField[] textFields;
    JSlider[] sliders;
    JCheckBox[] checks;
    JLabel[] sliderValues;
    JLabel serverMsg = new JLabel("");
    String timeLegend = " Time to retrieve the result set: ";
    JLabel timeMsg = new JLabel(timeLegend);
    String noRecsLegend = " Number of retrieved records: ";
    JLabel noRecsMsg = new JLabel(noRecsLegend);
    JPanel detailsPanel = new JPanel();
    DesignGridLayout designLayout = new DesignGridLayout(detailsPanel);
    JPanel qPanel = new JPanel();
    DesignGridLayout queryLayout = new DesignGridLayout(qPanel);
    JSplitPane splitPane;
    JPanel formPanel;
    JPanel controlPanel;
    private ChangeListener listener;

    public void handleResult(ArrayList<Record> r) {
        detailsPanel = new JPanel();
        designLayout = new DesignGridLayout(detailsPanel);
        dScrollPane = new JScrollPane(detailsPanel);
        splitPane.setBottomComponent(dScrollPane);

        for (int i = 0; i < detailsPanel.getComponentCount(); i++) {
            Component c = detailsPanel.getComponent(i);
            if ((c instanceof JTextField) || ((c instanceof JLabel))) {
                detailsPanel.remove(c);
            }
        }

        for (int j = 0; j < r.size(); j++) {
            Record rec = r.get(j);

            designLayout.row().grid(new JLabel(rec.getIdFieldName())).add(new JLabel(rec.getId())).empty();
            for (int x = 0; x < rec.getFieldNames().size(); x++) {
                String fieldName = rec.getFieldNames().get(x);
                String v = (String) rec.get(fieldName);
                JLabel jLab = new JLabel(fieldName);
                if (rec.isRemote()) {
                    jLab.setForeground(Color.red);
                }
                designLayout.row().grid(jLab).add(new JTextField(v)).empty();
            }
            designLayout.row().center().fill().add(new JSeparator());

        }

    }

    public ClientFormApp(String serverInstance) {

        this.labels = labels;

        this.mnemonics = mnemonics;
        this.widths = widths;
        this.descriptions = descriptions;
        if ((serverInstance != null) && (!serverInstance.equals(""))) {
            this.server = serverInstance;
        }
        serverMsg.setText(" Server: " + this.server);
        setTitle("Queries");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 700);
        setBackground(Color.gray);

        // Create a panel to hold all other components
        layerUI = new WaitLayerUI();
        topPanel = new JPanel();

        topPanel.setLayout(new BorderLayout());
        //getContentPane().add(topPanel);

        //Panel.setLayout(new BorderLayout());
        tb = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "keyed fields",
                TitledBorder.CENTER,
                TitledBorder.TOP);
        qPanel.setBorder(tb);

        splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

        dScrollPane = new JScrollPane(detailsPanel);

        final Client client = new Client(this.server, this.port);
        try {
            dbNames = (Vector<String>) client.submitCommand(Server_Thread.GET_INDEXED_DATABASES);
        } catch (ConnectException cex) {
            System.out.println(Client.CONNECTION_ERROR_MSG);
            System.out.println("Specified server: " + server);
            System.out.println("You should either check its availability, or resolve any possible network issues.");
            System.exit(0);
        } catch (UnknownHostException uhex) {
            System.out.println(Client.UNKNOWNHOST_ERROR_MSG);
            System.exit(0);
        }

        final Timer stopper = new Timer(400, new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                layerUI.stop();
            }
        });

        stopper.setRepeats(false);

        final JComboBox jcombo = new JComboBox(dbNames);

        JLabel dbNamesLab = new JLabel(" Select keyed database: ");
        JButton dbButt = new JButton("Fetch keyed fields");
        final JPanel form = new JPanel(new BorderLayout());

        dbButt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                formPanel = new JPanel(new BorderLayout());
                formPanel.add(controlPanel, BorderLayout.NORTH);
                qPanel = new JPanel();
                tb = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                        "keyed fields",
                        TitledBorder.CENTER,
                        TitledBorder.TOP);
                qPanel.setBorder(tb);
                queryLayout = new DesignGridLayout(qPanel);

                for (int i = 0; i < qPanel.getComponentCount(); i++) {
                    Component c = qPanel.getComponent(i);
                    qPanel.remove(c);

                }
                dbName = (String) jcombo.getSelectedItem();
                String[] indexFieldNames = null;
                try {
                    indexFieldNames = (String[]) client.submitCommand(Server_Thread.GET_INDEXED_FIELDS + dbName);
                } catch (ConnectException cex) {
                    System.out.println(Client.CONNECTION_ERROR_MSG);
                    System.out.println("Specified server: " + server);
                    System.out.println("You should either check its availability, or resolve any possible network issues.");
                    System.exit(0);
                } catch (UnknownHostException uhex) {
                    System.out.println(Client.UNKNOWNHOST_ERROR_MSG);
                    System.exit(0);
                }
                if (indexFieldNames == null) {
                    return;
                }

                textFields = new JTextField[indexFieldNames.length];
                sliders = new JSlider[indexFieldNames.length];
                sliderValues = new JLabel[indexFieldNames.length];
                checks = new JCheckBox[indexFieldNames.length];

                listener = new ChangeListener() {
                    public void stateChanged(ChangeEvent event) {
                        JSlider source = (JSlider) event.getSource();
                        int i = Integer.parseInt(source.getClientProperty("custom_Id").toString());
                        sliderValues[i].setText("" + source.getValue());
                    }
                };

                for (int i = 0; i < indexFieldNames.length; i++) {
                    String indexFieldName = indexFieldNames[i];
                    JLabel lab = new JLabel(indexFieldName, JLabel.RIGHT);
                    textFields[i] = new JTextField();
                    lab.setLabelFor(textFields[i]);
                    textFields[i].setName(indexFieldName);
                    textFields[i].setColumns(25);
                    sliders[i] = new JSlider(10, 100, 80);
                    sliders[i].setPaintTicks(true);
                    sliders[i].setMajorTickSpacing(50);
                    sliders[i].setMinorTickSpacing(10);
                    sliders[i].addChangeListener(listener);
                    sliders[i].putClientProperty("custom_Id", i);
                    sliderValues[i] = new JLabel(sliders[i].getValue() + "");
                    checks[i] = new JCheckBox();
                    checks[i].setSelected(performComparisons);
                    checks[i].setToolTipText("perform comparisons");
                    queryLayout.row().grid(new JLabel(indexFieldName)).add(textFields[i]).add(sliders[i]).add(sliderValues[i]);
                }

                textFields[0].requestFocusInWindow();
                JButton submit = new JButton("Submit");
                queryLayout.row().left().add(submit);
                formPanel.add(qPanel);
                qPanel.repaint();
                qPanel.revalidate();
                formPanel.repaint();
                formPanel.revalidate();
                splitPane.setTopComponent(formPanel);
                //JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
                //p.add(submit);
                //fieldPanel.add(dPanel);
                //fieldPanel.add(p);

                submit.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        layerUI.start();
                        if (!stopper.isRunning()) {
                            stopper.start();
                        }
                        dPanel.repaint();
                        QueryRecord query = new QueryRecord(dbName, maxQueryRows);
                        for (int i = 0; i < textFields.length; i++) {
                            String name = textFields[i].getName();
                            String value = textFields[i].getText();
                            int userThreshold = sliders[i].getValue();
                            boolean performComparisons = checks[i].isSelected();
                            if (!value.equals("")) {
                                query.set(name, value, userThreshold * 1.0 / 100, performComparisons); //may be name-based                              
                                String[] values = value.split(" ");
                                query.set(name + Key.TOKENS, values, userThreshold * 1.0 / 100, performComparisons); //may be name-based                            
                            }
                        }
                        //if (query.getFieldNames().size()==0)
                        //  return;
                        Result r = null;
                        try {
                            r = client.queryServer(query);
                            if (r.getStatus() == Result.STATUS_STORE_NOT_FOUND)
                                throw new StoreInitException("The specified store "+dbName+" not found.");
                            noRecsMsg.setText(noRecsLegend + r.getRecords().size());
                            timeMsg.setText(timeLegend + r.getTime());
                            handleResult(r.getRecords());
                        } catch (ConnectException cex) {
                            System.out.println(Client.CONNECTION_ERROR_MSG);
                            System.out.println("Specified server: " + server);
                            System.out.println("You should either check its availability, or resolve any possible network issues.");
                            System.exit(0);
                        } catch (UnknownHostException uhex) {
                            System.out.println(Client.UNKNOWNHOST_ERROR_MSG);
                            System.exit(0);
                        } catch (StoreInitException ex) {
                            System.out.println(ex.getMessage());
                        }
                        
                    }
                });
                form.repaint();

                form.revalidate();
            }
        }
        );

        controlPanel = new JPanel();
        Border bevelBorder = BorderFactory.createRaisedBevelBorder();

        controlPanel.setBorder(bevelBorder);
        GroupLayout layout = new GroupLayout(controlPanel);
        SequentialGroup leftToRight = layout.createSequentialGroup();
        leftToRight.addComponent(dbNamesLab);
        leftToRight.addComponent(jcombo);
        leftToRight.addComponent(dbButt);
        leftToRight.addComponent(serverMsg);
        leftToRight.addComponent(noRecsMsg);
        leftToRight.addComponent(timeMsg);
        layout.setHorizontalGroup(leftToRight);
        formPanel = new JPanel(new BorderLayout());
        formPanel.add(controlPanel, BorderLayout.NORTH);
        splitPane.setTopComponent(formPanel);
        splitPane.setBottomComponent(dScrollPane);
        splitPane.setDividerLocation(200);
        getContentPane().add(splitPane);
        jlayer = new JLayer<JComponent>(splitPane, layerUI);
        add(jlayer);
    }

// Main entry point for this example
    public static void main(String args[]) {
        String server = "";
        if (args.length > 0) {
            server = args[0];
        }
        ClientFormApp main = new ClientFormApp(server);

        // Create an instance of the test application
        main.setVisible(true);
    }
}
