/*
 * MAPPSView.java
 *  Petrus a Pazdic
 */
package mapps;

import players.AudioPlayer;
import usersettings.GraficalSettings;
import players.PictureViewer;
import usersettings.SettingsLoader;
import players.VideoPlayer;
import datadef.FileImpl;
import datadef.TrackPointImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import javax.swing.JFileChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.xml.sax.SAXException;
import parsers.G7tParser;
import parsers.GpxParser;
import parsers.KmlParser;
import players.PDFViewer;
import players.TxtViewer;
import usersettings.MultimediaPlayersSettings;

/**
 * Hlavné zobrazovacie okno aplikácie
 * @author Ľubomír Petrus, Matej Pazdič
 */
public class MAPPSView extends FrameView {

    /**
     * premenná indikujúca stav prezentácie
     */
    public static boolean isPresented = false;
    
    /**
     * premenná indikujúca pozastavenie prezentácie
     */
    public static boolean isPaused = false;
    
    /**
     * hodnota rýchlosti prezentácie
     */
    public static int speed;
    
    /**
     * objekt presentatora
     */
    public Presentator presentator;
    
    /**
     * séria nadmorských výššok zo zariadenia
     */
    public static XYSeries series;

    /**
     * séria nadmorských výššok z mapového servera
     */
    public static XYSeries series1;

    /**
     * séria na indikáciu pozície vo výškovom profile v manuálnom móde
     */
    public static XYSeries series2;

    /**
     * objekt grafu výškového zobrazenia
     */
    public static JFreeChart chart;

    /**
     * zakladná štruktúra bodov tracklogu
     */
    public Set<Waypoint> ways = new HashSet<Waypoint>();
    private Set<GeoPosition> geoPositions = new HashSet<GeoPosition>();
    private boolean isFiles[];
    private TrackPointImpl[] TP;
    private ArrayList<TrackPointImpl> track = new ArrayList<TrackPointImpl>();
    private File gpxFile = null;
    private ArrayList<FileImpl> multimediaFiles = new ArrayList<FileImpl>();
    private GeoPosition tempGP;
    private int tempZoom;
    private String os = System.getProperty("os.name");
    private boolean isCorrectSystem = true;

    /**
     * index aktuálnej pozície v tracklogu v manuálnom móde
     */
    public int index;
    private String trackType;
    
    /**
     * typ nadmorskej výške
     */
    public static String elevationsType;


    /**
     * konštruktor hlavného okna aplikácie
     * @param app - objekt celej aplikácie
     */
    public MAPPSView(SingleFrameApplication app) {
        super(app);

        initComponents();
        jXMapKit1.getMainMap().setRecenterOnClickEnabled(true);

        speed = jSlider1.getValue();
        
        SettingsLoader.getInstance().refreshSetttings();
        
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        tempGP = new GeoPosition(48.730861,21.244630);
        tempZoom = 1;
        jXMapKit1.setAddressLocation(tempGP);
        jXMapKit1.setZoom(tempZoom);

        jSplitPane.setName(null);
        jSplitPane.setContinuousLayout(true);
        jSplitPane.setResizeWeight(1.0);

         SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
               jSplitPane.setDividerLocation(0.84);
            }
        });

        /////////////////////////////////////////////////////////////////////

        series = new XYSeries("a");
        series1 = new XYSeries("b");
        series2 = new XYSeries("c");


        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(series);
        xyseriescollection.addSeries(series1);
        xyseriescollection.addSeries(series2);
        xyseriescollection.setIntervalWidth(0.0D);


        chart = ChartFactory.createXYAreaChart
        (null, null, "Elevation",
        xyseriescollection, PlotOrientation.VERTICAL, false,
        false, false);

        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);
        chart.setBackgroundPaint(mainPanel.getBackground());

        XYPlot plot = (XYPlot)chart.getPlot();

        plot.getDomainAxis().setTickLabelsVisible(false);
        plot.getRenderer().setSeriesPaint(0, Color.RED);
        plot.getRenderer().setSeriesPaint(1, Color.BLUE);
        plot.getRenderer().setSeriesPaint(2, Color.BLACK);

        plot.getRangeAxis().setTickLabelsVisible(false);
        plot.setBackgroundAlpha(1.0f);
        plot.setForegroundAlpha(0.63f);

        ChartPanel frame1=new ChartPanel(chart);

        frame1.setMaximumDrawHeight(700);
        frame1.setMinimumDrawHeight(100);
        frame1.setMinimumDrawWidth(100);
        frame1.setMaximumDrawWidth(20000);


        jPanel1.setLayout(new BorderLayout());
        frame1.setVisible(true);

        jPanel1.add(frame1, BorderLayout.CENTER);

    }

    /**
     * zobrazenie okna About
     */
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = MAPPSApp.getApplication().getMainFrame();
            aboutBox = new MAPPSAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        MAPPSApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jSplitPane = new javax.swing.JSplitPane();
        jXMapKit1 = new org.jdesktop.swingx.JXMapKit();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jSlider2 = new javax.swing.JSlider();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jSlider1 = new javax.swing.JSlider();
        jLabel2 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton2 = new javax.swing.JToggleButton();
        jToggleButton3 = new javax.swing.JToggleButton();
        jToggleButton4 = new javax.swing.JToggleButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        jToggleButton5 = new javax.swing.JToggleButton();
        jToggleButton6 = new javax.swing.JToggleButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        jButton6 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem3 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem4 = new javax.swing.JRadioButtonMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jRadioButtonMenuItem5 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem6 = new javax.swing.JRadioButtonMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();

        mainPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(mapps.MAPPSApp.class).getContext().getResourceMap(MAPPSView.class);
        mainPanel.setToolTipText(resourceMap.getString("myPanel.toolTipText")); // NOI18N
        mainPanel.setName("myPanel"); // NOI18N
        mainPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mainPanelMouseClicked(evt);
            }
        });

        jSplitPane.setDividerLocation(400);
        jSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane.setMinimumSize(new java.awt.Dimension(1, 1));
        jSplitPane.setName("splitPane"); // NOI18N
        jSplitPane.setPreferredSize(new java.awt.Dimension(0, 0));

        jXMapKit1.setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.OpenStreetMaps);
        jXMapKit1.setMiniMapVisible(false);
        jXMapKit1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jXMapKit1.setMinimumSize(new java.awt.Dimension(100, 100));
        jXMapKit1.setName("jXMapKit1"); // NOI18N
        jXMapKit1.setPreferredSize(new java.awt.Dimension(200, 300));
        jXMapKit1.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jXMapKit1MouseWheelMoved(evt);
            }
        });
        jXMapKit1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jXMapKit1MouseClicked(evt);
            }
        });
        jSplitPane.setTopComponent(jXMapKit1);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(400, 39));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 802, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 205, Short.MAX_VALUE)
        );

        jSplitPane.setBottomComponent(jPanel1);

        jPanel2.setName("jPanel2"); // NOI18N

        jSlider2.setToolTipText(resourceMap.getString("jSlider2.toolTipText")); // NOI18N
        jSlider2.setValue(0);
        jSlider2.setEnabled(false);
        jSlider2.setName("jSlider2"); // NOI18N
        jSlider2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider2StateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSlider2, javax.swing.GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setEnabled(false);
        jToolBar1.setName("jToolBar1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(mapps.MAPPSApp.class).getContext().getActionMap(MAPPSView.class, this);
        jButton1.setAction(actionMap.get("openFile")); // NOI18N
        jButton1.setIcon(resourceMap.getIcon("jButton1.icon")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setToolTipText(resourceMap.getString("jButton1.toolTipText")); // NOI18N
        jButton1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButton1.setBorderPainted(false);
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton1);

        jButton7.setAction(actionMap.get("showGpxParser")); // NOI18N
        jButton7.setIcon(resourceMap.getIcon("jButton7.icon")); // NOI18N
        jButton7.setText(resourceMap.getString("jButton7.text")); // NOI18N
        jButton7.setToolTipText(resourceMap.getString("jButton7.toolTipText")); // NOI18N
        jButton7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButton7.setBorderPainted(false);
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setName("jButton7"); // NOI18N
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton7);

        jButton8.setAction(actionMap.get("showG7tParser")); // NOI18N
        jButton8.setIcon(resourceMap.getIcon("jButton8.icon")); // NOI18N
        jButton8.setText(resourceMap.getString("jButton8.text")); // NOI18N
        jButton8.setToolTipText(resourceMap.getString("jButton8.toolTipText")); // NOI18N
        jButton8.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButton8.setBorderPainted(false);
        jButton8.setFocusable(false);
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setName("jButton8"); // NOI18N
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton8);

        jButton9.setAction(actionMap.get("showKmlParser")); // NOI18N
        jButton9.setIcon(resourceMap.getIcon("jButton9.icon")); // NOI18N
        jButton9.setToolTipText(resourceMap.getString("jButton9.toolTipText")); // NOI18N
        jButton9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButton9.setBorderPainted(false);
        jButton9.setFocusable(false);
        jButton9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton9.setName("jButton9"); // NOI18N
        jButton9.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton9);

        jSeparator3.setName("jSeparator3"); // NOI18N
        jToolBar1.add(jSeparator3);

        jButton2.setAction(actionMap.get("Present")); // NOI18N
        jButton2.setIcon(resourceMap.getIcon("jButton2.icon")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setToolTipText(resourceMap.getString("jButton2.toolTipText")); // NOI18N
        jButton2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButton2.setBorderPainted(false);
        jButton2.setEnabled(false);
        jButton2.setName("jButton2"); // NOI18N
        jToolBar1.add(jButton2);

        jButton3.setAction(actionMap.get("pausePresentation")); // NOI18N
        jButton3.setIcon(resourceMap.getIcon("jButton3.icon")); // NOI18N
        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setToolTipText(resourceMap.getString("jButton3.toolTipText")); // NOI18N
        jButton3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButton3.setBorderPainted(false);
        jButton3.setEnabled(false);
        jButton3.setName("jButton3"); // NOI18N
        jToolBar1.add(jButton3);

        jButton4.setAction(actionMap.get("stopPresentation")); // NOI18N
        jButton4.setIcon(resourceMap.getIcon("jButton4.icon")); // NOI18N
        jButton4.setText(resourceMap.getString("jButton4.text")); // NOI18N
        jButton4.setToolTipText(resourceMap.getString("jButton4.toolTipText")); // NOI18N
        jButton4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButton4.setBorderPainted(false);
        jButton4.setEnabled(false);
        jButton4.setName("jButton4"); // NOI18N
        jToolBar1.add(jButton4);

        jLabel1.setIcon(resourceMap.getIcon("jLabel1.icon")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 2, 1, 1));
        jLabel1.setName("jLabel1"); // NOI18N
        jToolBar1.add(jLabel1);

        jSlider1.setMaximum(800);
        jSlider1.setToolTipText(resourceMap.getString("jSlider1.toolTipText")); // NOI18N
        jSlider1.setValue(250);
        jSlider1.setEnabled(false);
        jSlider1.setInverted(true);
        jSlider1.setName("jSlider1"); // NOI18N
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });
        jToolBar1.add(jSlider1);

        jLabel2.setIcon(resourceMap.getIcon("jLabel2.icon")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 3));
        jLabel2.setName("jLabel2"); // NOI18N
        jToolBar1.add(jLabel2);

        jSeparator4.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 3));
        jSeparator4.setName("jSeparator4"); // NOI18N
        jToolBar1.add(jSeparator4);

        buttonGroup2.add(jToggleButton1);
        jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mapps/resources/DRPsilver.png"))); // NOI18N
        jToggleButton1.setText(resourceMap.getString("jToggleButton1.text")); // NOI18N
        jToggleButton1.setToolTipText(resourceMap.getString("jToggleButton1.toolTipText")); // NOI18N
        jToggleButton1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jToggleButton1.setBorderPainted(false);
        jToggleButton1.setEnabled(false);
        jToggleButton1.setFocusable(false);
        jToggleButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton1.setName("jToggleButton1"); // NOI18N
        jToggleButton1.setSelectedIcon(resourceMap.getIcon("jToggleButton1.selectedIcon")); // NOI18N
        jToggleButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jToggleButton1);

        buttonGroup2.add(jToggleButton2);
        jToggleButton2.setIcon(resourceMap.getIcon("jToggleButton2.icon")); // NOI18N
        jToggleButton2.setText(resourceMap.getString("jToggleButton2.text")); // NOI18N
        jToggleButton2.setToolTipText(resourceMap.getString("jToggleButton2.toolTipText")); // NOI18N
        jToggleButton2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jToggleButton2.setBorderPainted(false);
        jToggleButton2.setEnabled(false);
        jToggleButton2.setFocusable(false);
        jToggleButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton2.setName("jToggleButton2"); // NOI18N
        jToggleButton2.setSelectedIcon(resourceMap.getIcon("jToggleButton2.selectedIcon")); // NOI18N
        jToggleButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jToggleButton2);

        buttonGroup2.add(jToggleButton3);
        jToggleButton3.setIcon(resourceMap.getIcon("jToggleButton3.icon")); // NOI18N
        jToggleButton3.setSelected(true);
        jToggleButton3.setText(resourceMap.getString("jToggleButton3.text")); // NOI18N
        jToggleButton3.setToolTipText(resourceMap.getString("jToggleButton3.toolTipText")); // NOI18N
        jToggleButton3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jToggleButton3.setBorderPainted(false);
        jToggleButton3.setEnabled(false);
        jToggleButton3.setFocusable(false);
        jToggleButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton3.setName("jToggleButton3"); // NOI18N
        jToggleButton3.setSelectedIcon(resourceMap.getIcon("jToggleButton3.selectedIcon")); // NOI18N
        jToggleButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jToggleButton3);

        buttonGroup2.add(jToggleButton4);
        jToggleButton4.setIcon(resourceMap.getIcon("jToggleButton4.icon")); // NOI18N
        jToggleButton4.setText(resourceMap.getString("jToggleButton4.text")); // NOI18N
        jToggleButton4.setToolTipText(resourceMap.getString("jToggleButton4.toolTipText")); // NOI18N
        jToggleButton4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jToggleButton4.setBorderPainted(false);
        jToggleButton4.setEnabled(false);
        jToggleButton4.setFocusable(false);
        jToggleButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton4.setName("jToggleButton4"); // NOI18N
        jToggleButton4.setSelectedIcon(resourceMap.getIcon("jToggleButton4.selectedIcon")); // NOI18N
        jToggleButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jToggleButton4);

        jSeparator5.setName("jSeparator5"); // NOI18N
        jToolBar1.add(jSeparator5);

        jToggleButton5.setAction(actionMap.get("setAutomaticMode")); // NOI18N
        buttonGroup4.add(jToggleButton5);
        jToggleButton5.setIcon(resourceMap.getIcon("jToggleButton5.icon")); // NOI18N
        jToggleButton5.setSelected(true);
        jToggleButton5.setText(resourceMap.getString("jToggleButton5.text")); // NOI18N
        jToggleButton5.setToolTipText(resourceMap.getString("jToggleButton5.toolTipText")); // NOI18N
        jToggleButton5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jToggleButton5.setBorderPainted(false);
        jToggleButton5.setEnabled(false);
        jToggleButton5.setFocusable(false);
        jToggleButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton5.setName("jToggleButton5"); // NOI18N
        jToggleButton5.setSelectedIcon(resourceMap.getIcon("jToggleButton5.selectedIcon")); // NOI18N
        jToggleButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jToggleButton5);

        jToggleButton6.setAction(actionMap.get("setManualMode")); // NOI18N
        buttonGroup4.add(jToggleButton6);
        jToggleButton6.setIcon(resourceMap.getIcon("jToggleButton6.icon")); // NOI18N
        jToggleButton6.setText(resourceMap.getString("jToggleButton6.text")); // NOI18N
        jToggleButton6.setToolTipText(resourceMap.getString("jToggleButton6.toolTipText")); // NOI18N
        jToggleButton6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jToggleButton6.setBorderPainted(false);
        jToggleButton6.setEnabled(false);
        jToggleButton6.setFocusable(false);
        jToggleButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton6.setName("jToggleButton6"); // NOI18N
        jToggleButton6.setSelectedIcon(resourceMap.getIcon("jToggleButton6.selectedIcon")); // NOI18N
        jToggleButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jToggleButton6);

        jSeparator6.setName("jSeparator6"); // NOI18N
        jToolBar1.add(jSeparator6);

        jButton6.setIcon(resourceMap.getIcon("jButton6.icon")); // NOI18N
        jButton6.setText(resourceMap.getString("jButton6.text")); // NOI18N
        jButton6.setToolTipText(resourceMap.getString("jButton6.toolTipText")); // NOI18N
        jButton6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButton6.setBorderPainted(false);
        jButton6.setEnabled(false);
        jButton6.setName("jButton6"); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton6);

        jButton5.setIcon(resourceMap.getIcon("jButton5.icon")); // NOI18N
        jButton5.setText(resourceMap.getString("jButton5.text")); // NOI18N
        jButton5.setToolTipText(resourceMap.getString("jButton5.toolTipText")); // NOI18N
        jButton5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButton5.setBorderPainted(false);
        jButton5.setEnabled(false);
        jButton5.setName("jButton5"); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton5);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 804, Short.MAX_VALUE)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 643, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(161, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        jMenuItem1.setAction(actionMap.get("openFile")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        fileMenu.add(jMenuItem1);

        jSeparator1.setName("jSeparator1"); // NOI18N
        fileMenu.add(jSeparator1);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItem2.setAction(actionMap.get("showGpxParser")); // NOI18N
        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setToolTipText(resourceMap.getString("jMenuItem2.toolTipText")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenu1.add(jMenuItem2);

        jMenuItem5.setAction(actionMap.get("showG7tParser")); // NOI18N
        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setText(resourceMap.getString("jMenuItem5.text")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        jMenu1.add(jMenuItem5);

        jSeparator2.setName("jSeparator2"); // NOI18N
        jMenu1.add(jSeparator2);

        jMenuItem3.setAction(actionMap.get("showKmlParser")); // NOI18N
        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setToolTipText(resourceMap.getString("jMenuItem3.toolTipText")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jMenu1.add(jMenuItem3);

        menuBar.add(jMenu1);

        jMenu2.setText(resourceMap.getString("jMenu2.text")); // NOI18N
        jMenu2.setName("jMenu2"); // NOI18N

        jMenu3.setText(resourceMap.getString("jMenu3.text")); // NOI18N
        jMenu3.setToolTipText(resourceMap.getString("jMenu3.toolTipText")); // NOI18N
        jMenu3.setName("jMenu3"); // NOI18N

        jRadioButtonMenuItem1.setAction(actionMap.get("refreshMapS")); // NOI18N
        buttonGroup1.add(jRadioButtonMenuItem1);
        jRadioButtonMenuItem1.setSelected(true);
        jRadioButtonMenuItem1.setText(resourceMap.getString("jRadioButtonMenuItem1.text")); // NOI18N
        jRadioButtonMenuItem1.setName("jRadioButtonMenuItem1"); // NOI18N
        jMenu3.add(jRadioButtonMenuItem1);

        jRadioButtonMenuItem2.setAction(actionMap.get("refreshMapG")); // NOI18N
        buttonGroup1.add(jRadioButtonMenuItem2);
        jRadioButtonMenuItem2.setText(resourceMap.getString("jRadioButtonMenuItem2.text")); // NOI18N
        jRadioButtonMenuItem2.setName("jRadioButtonMenuItem2"); // NOI18N
        jMenu3.add(jRadioButtonMenuItem2);

        jRadioButtonMenuItem3.setAction(actionMap.get("refreshMapT")); // NOI18N
        buttonGroup1.add(jRadioButtonMenuItem3);
        jRadioButtonMenuItem3.setText(resourceMap.getString("jRadioButtonMenuItem3.text")); // NOI18N
        jRadioButtonMenuItem3.setName("jRadioButtonMenuItem3"); // NOI18N
        jMenu3.add(jRadioButtonMenuItem3);

        jRadioButtonMenuItem4.setAction(actionMap.get("refreshMapSt")); // NOI18N
        buttonGroup1.add(jRadioButtonMenuItem4);
        jRadioButtonMenuItem4.setText(resourceMap.getString("jRadioButtonMenuItem4.text")); // NOI18N
        jRadioButtonMenuItem4.setToolTipText(resourceMap.getString("jRadioButtonMenuItem4.toolTipText")); // NOI18N
        jRadioButtonMenuItem4.setName("jRadioButtonMenuItem4"); // NOI18N
        jMenu3.add(jRadioButtonMenuItem4);

        jMenu2.add(jMenu3);

        menuBar.add(jMenu2);

        jMenu5.setText(resourceMap.getString("jMenu5.text")); // NOI18N
        jMenu5.setDoubleBuffered(true);
        jMenu5.setEnabled(false);
        jMenu5.setName("jMenu5"); // NOI18N

        jRadioButtonMenuItem5.setAction(actionMap.get("setAutomaticMode")); // NOI18N
        buttonGroup3.add(jRadioButtonMenuItem5);
        jRadioButtonMenuItem5.setSelected(true);
        jRadioButtonMenuItem5.setText(resourceMap.getString("jRadioButtonMenuItem5.text")); // NOI18N
        jRadioButtonMenuItem5.setName("jRadioButtonMenuItem5"); // NOI18N
        jMenu5.add(jRadioButtonMenuItem5);

        jRadioButtonMenuItem6.setAction(actionMap.get("setManualMode")); // NOI18N
        buttonGroup3.add(jRadioButtonMenuItem6);
        jRadioButtonMenuItem6.setText(resourceMap.getString("jRadioButtonMenuItem6.text")); // NOI18N
        jRadioButtonMenuItem6.setName("jRadioButtonMenuItem6"); // NOI18N
        jMenu5.add(jRadioButtonMenuItem6);

        menuBar.add(jMenu5);

        jMenu4.setText(resourceMap.getString("jMenu4.text")); // NOI18N
        jMenu4.setName("jMenu4"); // NOI18N

        jMenuItem4.setAction(actionMap.get("showTrackPointGraficalSettingsDialog")); // NOI18N
        jMenuItem4.setText(resourceMap.getString("jMenuItem4.text")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        jMenu4.add(jMenuItem4);

        jMenuItem6.setAction(actionMap.get("showMediaPlayersSettings")); // NOI18N
        jMenuItem6.setText(resourceMap.getString("jMenuItem6.text")); // NOI18N
        jMenuItem6.setName("jMenuItem6"); // NOI18N
        jMenu4.add(jMenuItem6);

        menuBar.add(jMenu4);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 810, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 640, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jXMapKit1MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_jXMapKit1MouseWheelMoved
        // TODO add your handling code here:
    }//GEN-LAST:event_jXMapKit1MouseWheelMoved

    private void jXMapKit1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXMapKit1MouseClicked

    }//GEN-LAST:event_jXMapKit1MouseClicked

    private void mainPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainPanelMouseClicked

    }//GEN-LAST:event_mainPanelMouseClicked

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
        if (jSlider1.getValueIsAdjusting()) {
            speed = jSlider1.getValue();
        }
    }//GEN-LAST:event_jSlider1StateChanged

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        presentator.manualStepForward();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        presentator.manualStepBackward();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jSlider2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider2StateChanged
        if (jSlider2.getValueIsAdjusting()) {
            index = jSlider2.getValue();
            if (presentator.isStepping == false && presentator.step != index)
                presentator.jumpTo(index);
        }
}//GEN-LAST:event_jSlider2StateChanged

    /**
     * nastavenie automatického módu
     */
    @Action
    public void setAutomaticMode () {
        MAPPSView.isPresented = false;

        jButton6.setEnabled(false);
        jButton5.setEnabled(false);
        jSlider2.setEnabled(false);

        index = 0;

        if (!ways.isEmpty()) {
        presentator.drawMap();}

        jButton2.setEnabled(true);
        jSlider1.setEnabled(true);
        jButton3.setEnabled(true);
        jButton4.setEnabled(true);
        jToggleButton1.setEnabled(true);
        jToggleButton2.setEnabled(true);
        jToggleButton3.setEnabled(true);
        jToggleButton4.setEnabled(true);

        jRadioButtonMenuItem5.setSelected(true);
        jToggleButton5.setSelected(true);
    }

    /**
     * nastavenie manuálneho módu
     */
    @Action
    public void setManualMode () {
        MAPPSView.isPresented = false;

        index = 0;

        jButton6.setEnabled(true);
        jButton5.setEnabled(true);

        if (SwingUtilities.isEventDispatchThread()) {
            jSlider2.setEnabled(true);
            jSlider2.setValue(0);
        } else {

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    jSlider2.setEnabled(true);
                    jSlider2.setValue(0);
                }
            });
        }
        
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        presentator.presentationControlPointM = SettingsLoader.getInstance().getPresentationControlPoint();
        presentator.presentationControlPointWidthM = SettingsLoader.getInstance().getPresentationControlPointWidth();
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        jButton2.setEnabled(false);
        jSlider1.setEnabled(false);
        jButton3.setEnabled(false);
        jButton4.setEnabled(false);
        jToggleButton1.setEnabled(false);
        jToggleButton2.setEnabled(false);
        jToggleButton3.setEnabled(false);
        jToggleButton4.setEnabled(false);

        jRadioButtonMenuItem6.setSelected(true);
        jToggleButton6.setSelected(true);
    }


    /**
     * otvorenie tlv súboru
     */
    @Action
    public void openFile() {

        statusMessageLabel.setText("BUSSY");
        statusAnimationLabel.setIcon(busyIcons[1]);
        busyIconTimer.start();

        isPresented = false;
        isPaused = false;

        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new filefilters.TlvFilter());
        chooser.setCurrentDirectory(new File(SettingsLoader.getInstance().getTlvRecentFolder()));
        int returnVal = chooser.showOpenDialog(jXMapKit1);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            ways.clear();
            track.clear();
            geoPositions.clear();
            multimediaFiles.clear();

            try {
                File f = chooser.getSelectedFile();
                String strTemp = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(System.getProperty("file.separator")));
                SettingsLoader.getInstance().setTlvRecentFolder(strTemp);
                DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();
                DocumentBuilder DocB = DBF.newDocumentBuilder();
                org.w3c.dom.Document doc = DocB.parse(f);
                doc.getDocumentElement().normalize();
                NodeList list = doc.getElementsByTagName("TLV");
                Node gpxFileeee = list.item(0);
                Element elem = (Element) gpxFileeee;

                NodeList systemNodeList = elem.getElementsByTagName("SYSTEM");
                String system = systemNodeList.item(0).getTextContent();
                int opt = -1;
                if(!os.startsWith(system)){
                    isCorrectSystem = false;
                    Object[] options = {"Yes", "No"};
                    opt = JOptionPane.showOptionDialog(this.getFrame(), "Error: " + f.getName() + " were not parsed in " + System.getProperty("os.name") + " . Do you want to continue?", "Not correct system", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

                }

                if(opt == -1){
                NodeList coordinatesNodeList = elem.getElementsByTagName("COORDINATES");
                Node coordinatesNode = coordinatesNodeList.item(0);
                Element coordinatesElement =(Element) coordinatesNode;

                NodeList trackTypeList = coordinatesElement.getElementsByTagName("Track_Type");
                Node trackTypeNode = trackTypeList.item(0);
                trackType = trackTypeNode.getTextContent();

                NodeList elevationsTypeList = coordinatesElement.getElementsByTagName("Elevations_type");
                Node elevationsTypeNode = elevationsTypeList.item(0);
                elevationsType = elevationsTypeNode.getTextContent();

                NodeList trackPointNodeList = coordinatesElement.getElementsByTagName("TrackPoint");
                for(int i = 0; i < trackPointNodeList.getLength(); i++){
                    Node trackPointNode = trackPointNodeList.item(i);
                    Element trackPointElement = (Element) trackPointNode;
                    NodeList latitude = trackPointElement.getElementsByTagName("Latitude");
                    NodeList longitude = trackPointElement.getElementsByTagName("Longitude");
                    NodeList deviceElevation = trackPointElement.getElementsByTagName("Device_Elevation");
                    NodeList internetElevation = null;
                    if(elevationsType.equals("INTERNET")){
                        internetElevation = trackPointElement.getElementsByTagName("Internet_Elevation");
                    }
                    NodeList time = trackPointElement.getElementsByTagName("Time");

                    TrackPointImpl tempTP = new TrackPointImpl();
                    tempTP.setLatitude(Double.parseDouble(latitude.item(0).getTextContent()));
                    tempTP.setLongitude(Double.parseDouble(longitude.item(0).getTextContent()));
                    double tempD = Double.parseDouble(deviceElevation.item(0).getTextContent());
                    int tempInt = (int) tempD;
                    tempTP.setDeviceElevation(tempInt);

                    if(elevationsType.equals("INTERNET")){
                        double tempIE = Double.parseDouble(internetElevation.item(0).getTextContent());
                        int tempIntIE = (int) tempIE;
                        tempTP.setInternetElevation(tempIntIE);
                    }

                    if(!time.item(0).getTextContent().equalsIgnoreCase("null")){
                        tempTP.setTime(new Date(Long.parseLong(time.item(0).getTextContent())));
                    }
                    else{
                        tempTP.setTime(new Date(Long.parseLong("0")));
                    }
                    track.add(tempTP);
                }

                NodeList filesNodeList = elem.getElementsByTagName("FILES");
                Node filesNode = filesNodeList.item(0);
                Element filesElement = (Element) filesNode;
                NodeList fileEntityNode = filesElement.getElementsByTagName("File_entity");
                for(int i = 0; i < fileEntityNode.getLength(); i++){
                    FileImpl tempFile = new FileImpl();
                    Node fileNode = fileEntityNode.item(i);
                    Element fileElement = (Element) fileNode;
                    NodeList pathNode = fileElement.getElementsByTagName("path");
                    tempFile.setPath(pathNode.item(0).getTextContent());
                    NodeList dateNode = fileElement.getElementsByTagName("creation_date");
                    Date tempDate = new Date(Long.parseLong(dateNode.item(0).getTextContent()));
                    tempFile.setDate(tempDate);
                    NodeList fileLatitudeNode = fileElement.getElementsByTagName("gps_latitude");
                    if(!fileLatitudeNode.item(0).getTextContent().equals("null")){
                        tempFile.setLatitude(fileLatitudeNode.item(0).getTextContent());
                    }
                    NodeList fileLongitudeNode = fileElement.getElementsByTagName("gps_longitude");
                    if(!fileLongitudeNode.item(0).getTextContent().equals("null")){
                        tempFile.setLongitude(fileLongitudeNode.item(0).getTextContent());
                    }
                    NodeList fileElevationNode = fileElement.getElementsByTagName("gps_elevation");
                    if(!fileElevationNode.item(0).getTextContent().equals("null")){
                        tempFile.setElevation(fileElevationNode.item(0).getTextContent());
                    }
                    multimediaFiles.add(tempFile);
                }

                    TP = new TrackPointImpl[track.size()];
                    for(int i = 0; i < track.size(); i++){
                        TP[i] = track.get(i);
                    }

                    statusMessageLabel.setText("Loaded " + (TP.length) + " track points.");


                    isFiles = new boolean[track.size()];
                    for(int i = 0 ; i < track.size() ; i++){
                        isFiles[i] = false;
                    }

                    for (int i = 0; i < TP.length; i++) {
                        
                        if (TP[i] != null) {
                            double lat = TP[i].getLatitude();
                            double lon = TP[i].getLongitude();
                        }

                        Waypoint wa = new Waypoint(TP[i].getLatitude(), TP[i].getLongitude());
                        ways.add(wa);
                        geoPositions.add(new GeoPosition(TP[i].getLatitude(),TP[i].getLongitude()));
                    }

                    for (int i = 0; i < multimediaFiles.size(); i++){

                        Date fileDate = multimediaFiles.get(i).getDate();
                        for(int j = 1; j < track.size(); j++){
                            Date prevTrackPointDate = track.get(j-1).getTime();
                            prevTrackPointDate.setSeconds(track.get(j-1).getTime().getSeconds()-1);
                            Date nextTrackPointDate = track.get(j).getTime();
                            nextTrackPointDate.setSeconds(track.get(j).getTime().getSeconds()+1);
                            if (multimediaFiles.get(i).getLongitude() != null && multimediaFiles.get(i).getLatitude() != null) {
                                if ((fileDate.after(prevTrackPointDate) && fileDate.before(nextTrackPointDate)) || (fileDate.equals(prevTrackPointDate) || (fileDate.equals(nextTrackPointDate)))) {
                                    double deltaLat1 = Math.abs(Double.parseDouble(multimediaFiles.get(i).getLatitude()) - track.get(j - 1).getLatitude());
                                    double deltaLon1 = Math.abs(Double.parseDouble(multimediaFiles.get(i).getLongitude()) - track.get(j - 1).getLongitude());
                                    double deltaLat2 = Math.abs(Double.parseDouble(multimediaFiles.get(i).getLatitude()) - track.get(j).getLatitude());
                                    double deltaLon2 = Math.abs(Double.parseDouble(multimediaFiles.get(i).getLongitude()) - track.get(j).getLongitude());
                                    
                                    if ((deltaLat1 <= 0.0009 && deltaLon1 <= 0.0009) || (deltaLat2 <= 0.0009 && deltaLon2 <= 0.0009)) {
                                        
                                         multimediaFiles.get(i).setTrackPointIndex(j - 1);
                                         isFiles[j - 1] = true;
                                         break;
                                    }
                                }
                            } else {
                                if ((fileDate.after(prevTrackPointDate) && fileDate.before(nextTrackPointDate))) {
                                    multimediaFiles.get(i).setTrackPointIndex(j - 1);
                                    isFiles[j - 1] = true;
                                    break;
                                }
                            }
                        }
                    }

                    jButton2.setEnabled(true);
                    jButton3.setEnabled(true);
                    jButton4.setEnabled(true);
                    
                    jToggleButton1.setEnabled(true);
                    jToggleButton2.setEnabled(true);
                    jToggleButton3.setEnabled(true);
                    jToggleButton4.setEnabled(true);
                    jToggleButton5.setEnabled(true);
                    jToggleButton6.setEnabled(true);
                    
                    jSlider1.setEnabled(true);

                    jMenu5.setEnabled(true);
                    

                    presentator = new Presentator(jXMapKit1, TP, geoPositions, multimediaFiles, isFiles, trackType);
                    presentator.drawMap();

                }else if(opt == 0){
                   
                    NodeList coordinatesNodeList = elem.getElementsByTagName("COORDINATES");
                    Node coordinatesNode = coordinatesNodeList.item(0);
                    Element coordinatesElement = (Element) coordinatesNode;

                    NodeList trackTypeList = coordinatesElement.getElementsByTagName("Track_Type");
                    Node trackTypeNode = trackTypeList.item(0);
                    trackType = trackTypeNode.getTextContent();

                    NodeList elevationsTypeList = coordinatesElement.getElementsByTagName("Elevations_type");
                    Node elevationsTypeNode = elevationsTypeList.item(0);
                    elevationsType = elevationsTypeNode.getTextContent();

                    NodeList trackPointNodeList = coordinatesElement.getElementsByTagName("TrackPoint");
                    for (int i = 0; i < trackPointNodeList.getLength(); i++) {
                        Node trackPointNode = trackPointNodeList.item(i);
                        Element trackPointElement = (Element) trackPointNode;
                        NodeList latitude = trackPointElement.getElementsByTagName("Latitude");
                        NodeList longitude = trackPointElement.getElementsByTagName("Longitude");
                        NodeList deviceElevation = trackPointElement.getElementsByTagName("Device_Elevation");
                        NodeList internetElevation = null;
                        if (elevationsType.equals("INTERNET")) {
                            internetElevation = trackPointElement.getElementsByTagName("Internet_Elevation");
                        }
                        NodeList time = trackPointElement.getElementsByTagName("Time");

                        TrackPointImpl tempTP = new TrackPointImpl();
                        tempTP.setLatitude(Double.parseDouble(latitude.item(0).getTextContent()));
                        tempTP.setLongitude(Double.parseDouble(longitude.item(0).getTextContent()));
                        double tempD = Double.parseDouble(deviceElevation.item(0).getTextContent());
                        int tempInt = (int) tempD;
                        tempTP.setDeviceElevation(tempInt);

                        if (elevationsType.equals("INTERNET")) {
                            double tempIE = Double.parseDouble(internetElevation.item(0).getTextContent());
                            int tempIntIE = (int) tempIE;
                            tempTP.setInternetElevation(tempIntIE);
                        }

                        if (!time.item(0).getTextContent().equalsIgnoreCase("null")) {
                            tempTP.setTime(new Date(Long.parseLong(time.item(0).getTextContent())));
                        } else {
                            tempTP.setTime(new Date(Long.parseLong("0")));
                        }
                        track.add(tempTP);
                    }

                    TP = new TrackPointImpl[track.size()];
                    for (int i = 0; i < track.size(); i++) {
                        TP[i] = track.get(i);
                    }

                    statusMessageLabel.setText("Loaded " + (TP.length) + " track points.");


                    isFiles = new boolean[track.size()];
                    for (int i = 0; i < track.size(); i++) {
                        isFiles[i] = false;
                    }

                    for (int i = 0; i < TP.length; i++) {
                        
                        if (TP[i] != null) {
                            double lat = TP[i].getLatitude();
                            double lon = TP[i].getLongitude();
                        }

                        Waypoint wa = new Waypoint(TP[i].getLatitude(), TP[i].getLongitude());
                        ways.add(wa);
                        geoPositions.add(new GeoPosition(TP[i].getLatitude(), TP[i].getLongitude()));
                    }

                    jButton2.setEnabled(true);
                    jButton3.setEnabled(true);
                    jButton4.setEnabled(true);

                    jToggleButton1.setEnabled(true);
                    jToggleButton2.setEnabled(true);
                    jToggleButton3.setEnabled(true);
                    jToggleButton4.setEnabled(true);
                    jToggleButton5.setEnabled(true);
                    jToggleButton6.setEnabled(true);

                    jSlider1.setEnabled(true);

                    jMenu5.setEnabled(true);


                    presentator = new Presentator(jXMapKit1, TP, geoPositions, multimediaFiles, isFiles, trackType);
                    presentator.drawMap();

                }

                    statusAnimationLabel.setIcon(idleIcon);
                    busyIconTimer.stop();

            } catch (ParserConfigurationException ex) {
                Logger.getLogger(MAPPSView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(MAPPSView.class.getName()).log( Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MAPPSView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (returnVal == JFileChooser.CANCEL_OPTION) {
            statusAnimationLabel.setIcon(idleIcon);
            busyIconTimer.stop();
            statusMessageLabel.setText("Canceled");
        }
    }


    /**
     * zobrazenie parsera pre gpx súbory
     */
    @Action
    public void showGpxParser() {

        JFrame mainFrame = MAPPSApp.getApplication().getMainFrame();
        JFrame parser = new GpxParser();
        parser.setLocationRelativeTo(mainFrame);
        //parser.setAlwaysOnTop(true);
        parser.setVisible(true);

    }
    
    /**
     * zobrazenie parsera pre kml súbory
     */
    @Action
    public void showKmlParser() {

        JFrame mainFrame = MAPPSApp.getApplication().getMainFrame();
        JFrame parser = new KmlParser();
        parser.setLocationRelativeTo(mainFrame);
        parser.setVisible(true);

    }

    /**
     * nastavenie mapového servera zobrazenia ulíc openstreetmaps
     */
    @Action
    public void refreshMapS() {
        tempGP = jXMapKit1.getCenterPosition();
        tempZoom = jXMapKit1.getZoomSlider().getValue();

            TileFactoryInfo info = new TileFactoryInfo(1,15,17,
                256, true, true,
                "http://a.tile.openstreetmap.org/",
                "x","y","z") {
            @Override
            public String getTileUrl(int x, int y, int zoom) {
                zoom = 17-zoom;
                String url = this.baseURL +"/"+zoom+"/"+x+"/"+y+".png";
                return url;
            }

        };
        TileFactory tf = new DefaultTileFactory(info);
        jXMapKit1.setTileFactory(tf);
        jXMapKit1.setZoom(tempZoom);
        jXMapKit1.setCenterPosition(tempGP);


    }

    /**
     * nastavenie mapového servera zobrazenia ulíc mapquest
     */
    @Action
    public void refreshMapG() {
        tempGP = jXMapKit1.getCenterPosition();
        tempZoom = jXMapKit1.getZoomSlider().getValue();

            TileFactoryInfo info = new TileFactoryInfo(1,15,17,
                256, true, true,
                "http://otile1.mqcdn.com/tiles/1.0.0/osm/",
                "x","y","z") {
            @Override
            public String getTileUrl(int x, int y, int zoom) {
                zoom = 17-zoom;
                String url = this.baseURL +"/"+zoom+"/"+x+"/"+y+".png";
                return url;
            }

        };
        TileFactory tf = new DefaultTileFactory(info);
        jXMapKit1.setTileFactory(tf);
        jXMapKit1.setZoom(tempZoom);
        jXMapKit1.setCenterPosition(tempGP);

    }

    /**
     * nastavenie mapového servera turistického zobrazenia openstreetmaps
     */
    @Action
    public void refreshMapT() {
        tempGP = jXMapKit1.getCenterPosition();
        tempZoom = jXMapKit1.getZoomSlider().getValue();

            TileFactoryInfo info = new TileFactoryInfo(1,15,17,
                256, true, true,
                "http://tile.opencyclemap.org/cycle",
                "x","y","z") {
            @Override
            public String getTileUrl(int x, int y, int zoom) {
                zoom = 17-zoom;
                String url = this.baseURL +"/"+zoom+"/"+x+"/"+y+".png";
                return url;
            }

        };
        TileFactory tf = new DefaultTileFactory(info);
        jXMapKit1.setTileFactory(tf);
        jXMapKit1.setZoom(tempZoom);
        jXMapKit1.setCenterPosition(tempGP);


    }

    /**
     * nastavenie mapového servera satelitného zobrazenia mapquest
     */
    @Action
    public void refreshMapSt() {
        tempGP = jXMapKit1.getCenterPosition();
        tempZoom = jXMapKit1.getZoomSlider().getValue();
        
        TileFactoryInfo info = new TileFactoryInfo(1,15,17,
                256, true, true,
                "http://oatile1.mqcdn.com/naip",
                "x","y","z") {
            @Override
            public String getTileUrl(int x, int y, int zoom) {
                zoom = 17-zoom;
                String url = this.baseURL +"/"+zoom+"/"+x+"/"+y+".png";
                return url;
            }

        };
        TileFactory tf = new DefaultTileFactory(info);
        jXMapKit1.setTileFactory(tf);
        jXMapKit1.setZoom(tempZoom);
        jXMapKit1.setCenterPosition(tempGP);
    }

    /**
     * spustenie prezentácie v automatickom móde
     */
    @Action
    public void Present() {

        jMenu5.setEnabled(false);
        jToggleButton5.setEnabled(false);
        jToggleButton6.setEnabled(false);

        if (ways.isEmpty() != true && isPresented == false) {


            if (jToggleButton1.isSelected()) {

                presentator.DynamicRemainsPresentation();
            }
            else if (jToggleButton2.isSelected()) {

                presentator.staticRemainsPresentation();
            }

            else if (jToggleButton3.isSelected()) {

                presentator.staticClearPresentation();
            }
            else if (jToggleButton4.isSelected()) {

                presentator.dynamicClearPresentation();
            }

        } else if (ways.isEmpty() == true) {
            statusMessageLabel.setText("you must open file first");

        } else if ((isPresented == true && isPaused == true) || (!VideoPlayer.mediaPlayer.isPlaying() && VideoPlayer.frame.isVisible() )
                || (!AudioPlayer.mediaPlayer.isPlaying() && AudioPlayer.frame.isVisible() ) || PictureViewer.frame.isVisible()
                || PDFViewer.frame.isVisible() || TxtViewer.jEditorPane1.isVisible()) {
            if (VideoPlayer.isVPlaying) {
                VideoPlayer.mediaPlayer.play();
                isPaused = false;
            }
            else if (AudioPlayer.isAPlaying) {
                AudioPlayer.mediaPlayer.play();
                isPaused = false;
            }
            else if (PictureViewer.isPViewed){
                PictureViewer.restartTimer();
                isPaused = false;
            }
            else if (PDFViewer.isPDFViewed){
                PDFViewer.restartTimer();
                isPaused = false;
            }
            else if (TxtViewer.isTViewed){
                presentator.TV.restartTimer();
                isPaused = false;
            }
            
            else {
 
            Presentator.secondMultimediaSearchTimer.restart();
            isPaused = false; }

        }
    }

    /**
     * pozastavenie prezentácie v automatickom móde
     */
    @Action
    public void pausePresentation() {

        if (isPresented == true) {
            if (isPaused == false) {

                isPaused = true;
                Presentator.mainPresentationTimer.stop();

                Presentator.secondMultimediaSearchTimer.stop();

                if (PictureViewer.isPViewed) {
                PictureViewer.timer.cancel(); }
                if (PDFViewer.isPDFViewed) {
                PDFViewer.timer.cancel(); }
                if (TxtViewer.isTViewed) {
                TxtViewer.timer.cancel(); }
                if (AudioPlayer.isAPlaying) {
                AudioPlayer.pause();
                }
                else {
                VideoPlayer.pause();
                }
            }
        } else {
            statusMessageLabel.setText("you must start presentation first");
        }
    }

    /**
     * ukončenie prezentácie v automatickom móde
     */
    @Action
    public void stopPresentation() {

        if (ways.isEmpty() != true && isPresented == true) {         //////////////
            Presentator.mainPresentationTimer.stop();

            Presentator.secondMultimediaSearchTimer.stop();

            if (PictureViewer.WasPictureViewed){
            PictureViewer.timer.cancel();}
            if (PDFViewer.WasPDFViewed){
            PDFViewer.timer.cancel();}
            if (TxtViewer.WasTextViewed){
            TxtViewer.timer.cancel();}
            PictureViewer.mediaPlayer.stop();
            PictureViewer.setVisibleFalse();
            PDFViewer.mediaPlayer.stop();
            PDFViewer.setVisibleFalse();
            presentator.TV.setVisible(false);
            AudioPlayer.stop();
            VideoPlayer.stop();
            AudioPlayer.setVisibleFalse();
            VideoPlayer.setVisibleFalse();
            isPresented = false;
            isPaused = false;                                       //////////////

            PictureViewer.isPViewed = false;
            AudioPlayer.isAPlaying = false;
            VideoPlayer.isVPlaying = false;
            PDFViewer.isPDFViewed = false;
            TxtViewer.isTViewed = false;
                                                                    //////////////

            presentator.drawMap();
        } else {
            statusMessageLabel.setText("you dont have instance of running presentation");
        }
    }

    /**
     * zobrazenie okna grafických nastevení
     */
    @Action
    public void showTrackPointGraficalSettingsDialog() {
        JFrame mainFrame = MAPPSApp.getApplication().getMainFrame();
        JFrame GSettings = new GraficalSettings(presentator,!ways.isEmpty());
        GSettings.setLocationRelativeTo(mainFrame);
        //parser.setAlwaysOnTop(true);
        GSettings.setVisible(true);
    }

    /**
     * zobrazenie parsera pre g7t súbory
     */
    @Action
    public void showG7tParser() {
        JFrame mainFrame = MAPPSApp.getApplication().getMainFrame();
        JFrame parser = new G7tParser();
        parser.setLocationRelativeTo(mainFrame);
        parser.setVisible(true);
    }

    /**
     * zobrazenie okna nastavení zobrazovačov a prehrávačov
     */
    @Action
    public void showMediaPlayersSettings() {
        JFrame mainFrame = MAPPSApp.getApplication().getMainFrame();
        JFrame multimediaPlayersSettings = new MultimediaPlayersSettings(presentator, !ways.isEmpty());
        multimediaPlayersSettings.setLocationRelativeTo(mainFrame);
        //parser.setAlwaysOnTop(true);
        multimediaPlayersSettings.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.JButton jButton1;
    javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    public static javax.swing.JMenu jMenu5;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    public static javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem2;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem3;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem4;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem5;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem6;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JSlider jSlider1;
    public static javax.swing.JSlider jSlider2;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToggleButton jToggleButton4;
    public static javax.swing.JToggleButton jToggleButton5;
    public static javax.swing.JToggleButton jToggleButton6;
    private javax.swing.JToolBar jToolBar1;
    private org.jdesktop.swingx.JXMapKit jXMapKit1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;

}
