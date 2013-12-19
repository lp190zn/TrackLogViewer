/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mapps;

import players.PictureViewer;
import usersettings.SettingsLoader;
import players.VideoPlayer;
import players.AudioPlayer;
import datadef.FileImpl;
import datadef.TrackPointImpl;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Timer;
import javax.imageio.ImageIO;
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;
import org.jfree.chart.plot.XYPlot;
import players.PDFViewer;
import players.TxtViewer;

/**
 * objekt na vykreslenie a prezentáciu tracklogu a súvisiacich multimédií
 * @author Ľubomír Petrus
 */
public class Presentator {

    private JXMapKit map;
    private TrackPointImpl[] TP;
    private Set<GeoPosition> geoPositions = new HashSet<GeoPosition>();
     
    /**
     * objekt hlavného časovača automatickej prezentácie
     */
    public static Timer mainPresentationTimer;

    /**
     * objekt multimediálneho časovača
     */
    public static Timer secondMultimediaSearchTimer;

    private ArrayList<FileImpl> MultimediaFiles;
    private BufferedImage redPin, bluePin, airplane, bicycle, boat, canoe, car, hicker, paraglide, ski;

    private PictureViewer MP = new PictureViewer();
    private VideoPlayer VP = new VideoPlayer();
    private AudioPlayer AP = new AudioPlayer();
    private PDFViewer PV = new PDFViewer();
    
    /**
     * objekt zobrazovača textových súborov
     */
    public TxtViewer TV = new TxtViewer();
    private XYPlot plot = (XYPlot) MAPPSView.chart.getPlot();
    private boolean isFiles[];
    
    /**
     * index aktuálnej pozície v tracklogu (krok)
     */
    public int step = 0;

    private boolean isViewingMedia = false;
    
    /**
     * premenná indikujúca zatváranie okien po skončení zobrazenia multimédií
     */
    public static boolean isClosingWindows;
    
    /**
     * premenná indikujúca ponechania poslednej fotografie v zobrazovači
     */
    public static boolean showLastPicture;

    private boolean cyclePresentation;
    
    /**
     * premenná indikujúca krokovanie v manuálnom režime
     */
    public boolean isStepping = false;

    private boolean wasStepBack = false;

    private int LineColor;
    private float LineWidth;
    private int ControlPointWidth;
    private int ControlPointStyle;
    private boolean isDrawLine;
    private boolean isDrawPoint;
    private int ControlPointColor;

    /**
     * priemer bodu tracklogu pri prezentácii
     */
    public int presentationControlPointWidthM;
    
    /**
     * typ zobrazenia bodu tracklogu pri prezentácii
     */
    public int presentationControlPointM;

    private String trackType;


    /**
     * konštruktor objektu Presentator
     * @param map - mapa
     * @param TP - štruktúra obsahujúca všetky body tracklogu
     * @param geoPositions - štruktúra obsahujúca všetky body tracklogu
     * @param MultimediaFiles - štruktúra obsahujúca všetky multimediálne súbory
     * @param isFiles - zoznam jednotlivých bodov s indikáciou prítomnosti multimediálnych súborov
     * @param trackType - typ trasy
     */
    public Presentator(JXMapKit map, TrackPointImpl[] TP, Set<GeoPosition> geoPositions,
            ArrayList<FileImpl> MultimediaFiles, boolean isFiles[], String trackType) {

        this.map = map;
        this.TP = TP;
        this.geoPositions = geoPositions;
        this.MultimediaFiles = MultimediaFiles;
        this.isFiles = isFiles;
        this.trackType = trackType;

        try {
            redPin = ImageIO.read(getClass().getResource("pinRed.png"));
            bluePin = ImageIO.read(getClass().getResource("pinBlue.png"));
            airplane = ImageIO.read(getClass().getResource("trackicons/Airplane.png"));
            bicycle = ImageIO.read(getClass().getResource("trackicons/Bicycle.png"));
            boat = ImageIO.read(getClass().getResource("trackicons/Boat.png"));
            canoe = ImageIO.read(getClass().getResource("trackicons/Canoe.png"));
            car = ImageIO.read(getClass().getResource("trackicons/Car.png"));
            hicker = ImageIO.read(getClass().getResource("trackicons/Hicker.png"));
            paraglide = ImageIO.read(getClass().getResource("trackicons/Paraglide.png"));
            ski = ImageIO.read(getClass().getResource("trackicons/Ski.png"));

        } catch (IOException ex) {
            System.out.println("chyba pri loadovani obrazku");
        }
        MAPPSView.jSlider2.setMaximum(TP.length-1);

    }

    /**
     * hlavná metóda na vykreslenie tracklogu
     */
    public void drawMap() {

        MAPPSView.jMenu5.setEnabled(true);
        MAPPSView.jToggleButton5.setEnabled(true);
        MAPPSView.jToggleButton6.setEnabled(true);

        step = 0;
        MAPPSView.jSlider2.setValue(step);
        MAPPSView.isPresented = false;

        isClosingWindows = SettingsLoader.getInstance().isClosingMediaPlayers();
        showLastPicture = SettingsLoader.getInstance().isShowingLastPicture();
        cyclePresentation = SettingsLoader.getInstance().isCyclingPresentation();

        MAPPSView.series.clear();
        MAPPSView.series1.clear();
        MAPPSView.series2.clear();


        AudioPlayer.frame.setVisible(false);
        VideoPlayer.frame.setVisible(false);
        PictureViewer.frame.setVisible(false);
        PDFViewer.frame.setVisible(false);
        TV.setVisible(false);

        plot.getRangeAxis().setTickLabelsVisible(true);

        CompoundPainter cp = new CompoundPainter();

        ControlPointWidth = SettingsLoader.getInstance().getControlPointWidth();
        ControlPointStyle = SettingsLoader.getInstance().getControlPoint();
        ControlPointColor = SettingsLoader.getInstance().getControlPointColor();
        LineColor = SettingsLoader.getInstance().getLineColor();
        LineWidth = SettingsLoader.getInstance().getLineWidth();
        isDrawLine = SettingsLoader.getInstance().isDrawLine();
        isDrawPoint = SettingsLoader.getInstance().isDrawControlPoint();

        map.getMainMap().setZoomEnabled(true);
        map.getZoomSlider().setEnabled(true);
        map.getZoomInButton().setEnabled(true);
        map.getZoomOutButton().setEnabled(true);

        cp.setCacheable(false);
        map.getMainMap().setOverlayPainter(cp);

        Painter<JXMapViewer> polyLineOverlay = new Painter<JXMapViewer>() {

            public void paint(Graphics2D g, JXMapViewer map, int w, int h) {

                g = (Graphics2D) g.create();

                switch (ControlPointColor) {
                    case 0:
                        g.setColor(Color.BLUE);
                        break;
                    case 1:
                        g.setColor(Color.RED);
                        break;
                    case 2:
                        g.setColor(Color.YELLOW);
                        break;
                    case 3:
                        g.setColor(Color.GREEN);
                        break;
                    case 4:
                        g.setColor(Color.CYAN);
                        break;
                    case 5:
                        g.setColor(Color.WHITE);
                        break;
                    case 6:
                        g.setColor(Color.ORANGE);
                        break;
                    case 7:
                        g.setColor(Color.PINK);
                        break;
                }

                Rectangle rect = map.getViewportBounds();
                g.translate(-rect.x + 0, -rect.y + 0);
                g.setStroke(new BasicStroke(LineWidth));

                GeneralPath polyLine = new GeneralPath(GeneralPath.WIND_EVEN_ODD, TP.length);
                GeoPosition gp = new GeoPosition(TP[0].getLatitude(), TP[0].getLongitude());
                Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                polyLine.moveTo(pt.getX(), pt.getY());

                for (int i = 0; i < TP.length; i++) {

                    gp = new GeoPosition(TP[i].getLatitude(), TP[i].getLongitude());
                    pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                    polyLine.lineTo(pt.getX(), pt.getY());

                    if (isDrawPoint) {

                        if (isFiles[i] == true) {

                            g.drawImage(bluePin, (int) pt.getX()-10, (int) pt.getY()-20, null);

                        } else {

                            switch (ControlPointStyle) {
                                case 0:
                                    g.drawOval((int) pt.getX() - 1, (int) pt.getY() - 1, ControlPointWidth, ControlPointWidth);
                                    break;
                                case 1:
                                    g.fillOval((int) pt.getX() - 1, (int) pt.getY() - 1, ControlPointWidth + 2, ControlPointWidth + 2);
                                    break;
                                case 2:
                                    g.drawRect((int) pt.getX() - 1, (int) pt.getY() - 1, ControlPointWidth, ControlPointWidth);
                                    break;
                                case 3:
                                    g.fillRect((int) pt.getX() - 1, (int) pt.getY() - 1, ControlPointWidth + 2, ControlPointWidth + 2);
                                    ;
                                    break;
                                case 4:
                                    g.drawImage(redPin, (int) pt.getX() - 10, (int) pt.getY() - 20, null);
                                    break;
                                case 5:

                                    if (trackType.equals("Walk")) {
                                        g.drawImage(hicker, (int) pt.getX() - 10, (int) pt.getY() - 10, null); }
                                    else if (trackType.equals("Bicycle")) {
                                        g.drawImage(bicycle, (int) pt.getX() - 10, (int) pt.getY() - 10, null); }
                                    else if (trackType.equals("Car")) {
                                        g.drawImage(car, (int) pt.getX() - 10, (int) pt.getY() - 10, null); }
                                    else if (trackType.equals("Paraglide")) {
                                        g.drawImage(paraglide, (int) pt.getX() - 10, (int) pt.getY() - 10, null); }
                                    else if (trackType.equals("Boat")) {
                                        g.drawImage(boat , (int) pt.getX() - 10, (int) pt.getY() - 10, null); }
                                    else if (trackType.equals("Airplane")) {
                                        g.drawImage(airplane, (int) pt.getX() - 10, (int) pt.getY() - 10, null); }
                                    else if (trackType.equals("Canoe")) {
                                        g.drawImage(canoe, (int) pt.getX() - 10, (int) pt.getY() - 10, null); }
                                    else if (trackType.equals("Ski")) {
                                        g.drawImage(ski, (int) pt.getX() - 10, (int) pt.getY() - 10, null);}
                            }

                        }
                    }
                }

                switch (LineColor) {
                    case 0:
                        g.setColor(Color.BLUE);
                        break;
                    case 1:
                        g.setColor(Color.RED);
                        break;
                    case 2:
                        g.setColor(Color.YELLOW);
                        break;
                    case 3:
                        g.setColor(Color.GREEN);
                        break;
                    case 4:
                        g.setColor(Color.CYAN);
                        break;
                    case 5:
                        g.setColor(Color.WHITE);
                        break;
                    case 6:
                        g.setColor(Color.ORANGE);
                        break;
                    case 7:
                        g.setColor(Color.PINK);
                        break;

                }

                if (isDrawLine) {

                    g.draw(polyLine);
                }

                g.dispose();
            }
        };

        cp.setPainters(polyLineOverlay);
        map.getMainMap().setOverlayPainter(cp);
        map.getMainMap().setZoom(1);
        map.getMainMap().calculateZoomFrom(geoPositions);

        drawElevation();

    }

    /**
     * automatická prezentácia v režime Dynamic Remains Presentation (DRP)
     */
    public void DynamicRemainsPresentation() {

        MAPPSView.series.clear();
        MAPPSView.series1.clear();


        final int presentationControlPointWidth = SettingsLoader.getInstance().getPresentationControlPointWidth();
        final int presentationControlPointStyle = SettingsLoader.getInstance().getPresentationControlPoint();
        final int presentationControlPointColor = SettingsLoader.getInstance().getPresentationControlPointColor();

        final Set<Waypoint> waypoints1 = new HashSet<Waypoint>();
        final Set<Waypoint> temp = new HashSet<Waypoint>();

        MAPPSView.isPresented = true;

        final WaypointPainter WP = new WaypointPainter();
        final WaypointPainter WP1 = new WaypointPainter();
        final CompoundPainter cp = new CompoundPainter();

        map.getMainMap().setZoom(2);

        mainPresentationTimer = new Timer(0, new ActionListener() {

            int i = 0;

            public void actionPerformed(ActionEvent e) {

                if (i < TP.length) {

                    temp.clear();

                    if (i > 0) {
                        Waypoint wa1 = new Waypoint(TP[i - 1].getLatitude(), TP[i - 1].getLongitude());
                        waypoints1.add(wa1);
                    }

                    Waypoint wa = new Waypoint(TP[i].getLatitude(), TP[i].getLongitude());
                    map.getMainMap().setCenterPosition(new GeoPosition(TP[i].getLatitude(), TP[i].getLongitude()));
                    WP.setWaypoints(waypoints1);

                    MAPPSView.series.add(i, TP[i].getDeviceElevation());
                    
                    if (MAPPSView.elevationsType.equals("INTERNET")) {
                    MAPPSView.series1.add(i, TP[i].getInternetElevation()); }                    

                    WP.setRenderer(new WaypointRenderer() {

                        public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {

                            switch (presentationControlPointColor) {
                                case 0:
                                    g.setColor(Color.BLUE);
                                    break;
                                case 1:
                                    g.setColor(Color.RED);
                                    break;
                                case 2:
                                    g.setColor(Color.YELLOW);
                                    break;
                                case 3:
                                    g.setColor(Color.GREEN);
                                    break;
                                case 4:
                                    g.setColor(Color.CYAN);
                                    break;
                                case 5:
                                    g.setColor(Color.WHITE);
                                    break;
                                case 6:
                                    g.setColor(Color.ORANGE);
                                    break;
                                case 7:
                                    g.setColor(Color.PINK);
                                    break;
                            }


                            switch (presentationControlPointStyle) {
                                case 0:
                                    g.drawOval(0, 0, presentationControlPointWidth, presentationControlPointWidth);
                                    break;
                                case 1:
                                    g.fillOval(0, 0, presentationControlPointWidth + 2, presentationControlPointWidth + 2);
                                    break;
                                case 2:
                                    g.drawRect(0, 0, presentationControlPointWidth, presentationControlPointWidth);
                                    break;
                                case 3:
                                    g.fillRect(0, 0, presentationControlPointWidth + 2, presentationControlPointWidth + 2);
                                    break;
                                case 4:
                                    g.drawImage(redPin, -10, -20, null);
                                    break;
                                case 5:

                                    if (trackType.equals("Walk")) {
                                        g.drawImage(hicker, - 10, - 10, null); }
                                    else if (trackType.equals("Bicycle")) {
                                        g.drawImage(bicycle, - 10, - 10, null); }
                                    else if (trackType.equals("Car")) {
                                        g.drawImage(car, - 10, - 10, null); }
                                    else if (trackType.equals("Paraglide")) {
                                        g.drawImage(paraglide, - 10, - 10, null); }
                                    else if (trackType.equals("Boat")) {
                                        g.drawImage(boat , - 10, - 10, null); }
                                    else if (trackType.equals("Airplane")) {
                                        g.drawImage(airplane, - 10, - 10, null); }
                                    else if (trackType.equals("Canoe")) {
                                        g.drawImage(canoe, - 10, - 10, null); }
                                    else if (trackType.equals("Ski")) {
                                        g.drawImage(ski, - 10, - 10, null);}

                                    break;

                            }

                            return true;
                        }
                    });

                    temp.add(wa);
                    WP1.setWaypoints(temp);

                    WP1.setRenderer(new WaypointRenderer() {

                        public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {

                            switch (presentationControlPointStyle) {
                                case 0:
                                    g.setColor(Color.magenta);
                                    g.drawOval(0, 0, presentationControlPointWidth, presentationControlPointWidth);
                                    break;
                                case 1:
                                    g.setColor(Color.magenta);
                                    g.fillOval(0, 0, presentationControlPointWidth + 2, presentationControlPointWidth + 2);
                                    break;
                                case 2:
                                    g.setColor(Color.magenta);
                                    g.drawRect(0, 0, presentationControlPointWidth, presentationControlPointWidth);
                                    break;
                                case 3:
                                    g.setColor(Color.magenta);
                                    g.fillRect(0, 0, presentationControlPointWidth + 2, presentationControlPointWidth + 2);
                                    break;
                                case 4:
                                    g.drawImage(bluePin, -10, -20, null);
                                    break;
                                case 5:
                                    if (trackType.equals("Walk")) {
                                        g.drawImage(hicker, - 10, - 10, null); }
                                    else if (trackType.equals("Bicycle")) {
                                        g.drawImage(bicycle, - 10, - 10, null); }
                                    else if (trackType.equals("Car")) {
                                        g.drawImage(car, - 10, - 10, null); }
                                    else if (trackType.equals("Paraglide")) {
                                        g.drawImage(paraglide, - 10, - 10, null); }
                                    else if (trackType.equals("Boat")) {
                                        g.drawImage(boat , - 10, - 10, null); }
                                    else if (trackType.equals("Airplane")) {
                                        g.drawImage(airplane, - 10, - 10, null); }
                                    else if (trackType.equals("Canoe")) {
                                        g.drawImage(canoe, - 10, - 10, null); }
                                    else if (trackType.equals("Ski")) {
                                        g.drawImage(ski, - 10, - 10, null);}
                                    break;
                            }

                            return true;
                        }
                    });


                    cp.setCacheable(false);
                    cp.setPainters(WP, WP1);
                    map.getMainMap().setOverlayPainter(cp);

                    i++;

                    presentFilesA(i - 1);

                } else {

                    if (cyclePresentation) {
                        i = 0;
                        MAPPSView.series.clear();
                        MAPPSView.series1.clear();
                    }
                    else {

                    MAPPSView.isPresented = false;

                    mainPresentationTimer.stop();

                    secondMultimediaSearchTimer.stop();
               
                    drawMap(); }

                }
            }
        });
        mainPresentationTimer.start();

    }

    /**
     * automatická prezentácia v režime Static Remains Presentation (SRP)
     */
    public void staticRemainsPresentation() {

        MAPPSView.series.clear();
        MAPPSView.series1.clear();

        final int presentationControlPointWidth = SettingsLoader.getInstance().getPresentationControlPointWidth();
        final int presentationControlPointStyle = SettingsLoader.getInstance().getPresentationControlPoint();
        final int presentationControlPointColor = SettingsLoader.getInstance().getPresentationControlPointColor();

        final Set<Waypoint> waypoints1 = new HashSet<Waypoint>();
        final Set<Waypoint> temp = new HashSet<Waypoint>();

        final WaypointPainter WP = new WaypointPainter();
        final WaypointPainter WP1 = new WaypointPainter();
        final CompoundPainter cp = new CompoundPainter();

        map.getMainMap().setZoomEnabled(false);
        map.getZoomSlider().setEnabled(false);
        map.getZoomInButton().setEnabled(false);
        map.getZoomOutButton().setEnabled(false);

        map.getMainMap().setZoom(1);
        map.getMainMap().calculateZoomFrom(geoPositions);

        MAPPSView.isPresented = true;

        mainPresentationTimer = new Timer(0, new ActionListener() {

            int i = 0;

            public void actionPerformed(ActionEvent e) {
                if (i < TP.length) {

                    temp.clear();

                    if (i > 0) {
                        Waypoint wa1 = new Waypoint(TP[i - 1].getLatitude(), TP[i - 1].getLongitude());
                        waypoints1.add(wa1);
                    }

                    Waypoint wa = new Waypoint(TP[i].getLatitude(), TP[i].getLongitude());

                    WP.setWaypoints(waypoints1);

                    MAPPSView.series.add(i, TP[i].getDeviceElevation());

                    if (MAPPSView.elevationsType.equals("INTERNET")) {
                    MAPPSView.series1.add(i, TP[i].getInternetElevation()); }


                    WP.setRenderer(new WaypointRenderer() {

                        public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {

                            switch (presentationControlPointColor) {
                                case 0:
                                    g.setColor(Color.BLUE);
                                    break;
                                case 1:
                                    g.setColor(Color.RED);
                                    break;
                                case 2:
                                    g.setColor(Color.YELLOW);
                                    break;
                                case 3:
                                    g.setColor(Color.GREEN);
                                    break;
                                case 4:
                                    g.setColor(Color.CYAN);
                                    break;
                                case 5:
                                    g.setColor(Color.WHITE);
                                    break;
                                case 6:
                                    g.setColor(Color.ORANGE);
                                    break;
                                case 7:
                                    g.setColor(Color.PINK);
                                    break;
                            }


                            switch (presentationControlPointStyle) {
                                case 0:
                                    g.drawOval(0, 0, presentationControlPointWidth, presentationControlPointWidth);
                                    break;
                                case 1:
                                    g.fillOval(0, 0, presentationControlPointWidth + 2, presentationControlPointWidth + 2);
                                    break;
                                case 2:
                                    g.drawRect(0, 0, presentationControlPointWidth, presentationControlPointWidth);
                                    break;
                                case 3:
                                    g.fillRect(0, 0, presentationControlPointWidth + 2, presentationControlPointWidth + 2);
                                    break;
                                case 4:
                                    g.drawImage(redPin, -10, -20, null);
                                    break;
                                case 5:
                                    if (trackType.equals("Walk")) {
                                        g.drawImage(hicker, - 10, - 10, null); }
                                    else if (trackType.equals("Bicycle")) {
                                        g.drawImage(bicycle, - 10, - 10, null); }
                                    else if (trackType.equals("Car")) {
                                        g.drawImage(car, - 10, - 10, null); }
                                    else if (trackType.equals("Paraglide")) {
                                        g.drawImage(paraglide, - 10, - 10, null); }
                                    else if (trackType.equals("Boat")) {
                                        g.drawImage(boat , - 10, - 10, null); }
                                    else if (trackType.equals("Airplane")) {
                                        g.drawImage(airplane, - 10, - 10, null); }
                                    else if (trackType.equals("Canoe")) {
                                        g.drawImage(canoe, - 10, - 10, null); }
                                    else if (trackType.equals("Ski")) {
                                        g.drawImage(ski, - 10, - 10, null);}

                                    break;
                            }

                            return true;
                        }
                    });

                    temp.add(wa);
                    WP1.setWaypoints(temp);

                    WP1.setRenderer(new WaypointRenderer() {

                        public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {

                            switch (presentationControlPointStyle) {
                                case 0:
                                    g.setColor(Color.magenta);
                                    g.drawOval(0, 0, presentationControlPointWidth, presentationControlPointWidth);
                                    break;
                                case 1:
                                    g.setColor(Color.magenta);
                                    g.fillOval(0, 0, presentationControlPointWidth + 2, presentationControlPointWidth + 2);
                                    break;
                                case 2:
                                    g.setColor(Color.magenta);
                                    g.drawRect(0, 0, presentationControlPointWidth, presentationControlPointWidth);
                                    break;
                                case 3:
                                    g.setColor(Color.magenta);
                                    g.fillRect(0, 0, presentationControlPointWidth + 2, presentationControlPointWidth + 2);
                                    break;
                                case 4:
                                    g.drawImage(bluePin, -10, -20, null);
                                    break;
                                case 5:
                                    if (trackType.equals("Walk")) {
                                        g.drawImage(hicker, - 10, - 10, null); }
                                    else if (trackType.equals("Bicycle")) {
                                        g.drawImage(bicycle, - 10, - 10, null); }
                                    else if (trackType.equals("Car")) {
                                        g.drawImage(car, - 10, - 10, null); }
                                    else if (trackType.equals("Paraglide")) {
                                        g.drawImage(paraglide, - 10, - 10, null); }
                                    else if (trackType.equals("Boat")) {
                                        g.drawImage(boat , - 10, - 10, null); }
                                    else if (trackType.equals("Airplane")) {
                                        g.drawImage(airplane, - 10, - 10, null); }
                                    else if (trackType.equals("Canoe")) {
                                        g.drawImage(canoe, - 10, - 10, null); }
                                    else if (trackType.equals("Ski")) {
                                        g.drawImage(ski, - 10, - 10, null);}
                                    break;
                            }

                            return true;
                        }
                    });


                    cp.setCacheable(false);
                    cp.setPainters(WP, WP1);
                    map.getMainMap().setOverlayPainter(cp);

                    i++;

                    presentFilesA(i - 1);

                } else {

                    if (cyclePresentation) {
                        i = 0;
                        MAPPSView.series.clear();
                        MAPPSView.series1.clear();
                    }
                    else {
                    MAPPSView.isPresented = false;

                    mainPresentationTimer.stop();

                    secondMultimediaSearchTimer.stop();

                    drawMap();
                    }
                }
            }
        });
        mainPresentationTimer.start();

    }

    /**
     * automatická prezentácia v režime Static Clear Presentation (SCP)
     */
    public void staticClearPresentation() {

        MAPPSView.series.clear();
        MAPPSView.series1.clear();

        MAPPSView.isPresented = true;

        //LineColor = SettingsLoader.getInstance().getLineColor();
        final int presentationControlPointWidth = SettingsLoader.getInstance().getPresentationControlPointWidth();
        final int presentationControlPointStyle = SettingsLoader.getInstance().getPresentationControlPoint();

        final Set<Waypoint> temp = new HashSet<Waypoint>();

        final WaypointPainter WP = new WaypointPainter();
        final CompoundPainter cp = new CompoundPainter();

        map.getMainMap().setZoomEnabled(false);
        map.getZoomSlider().setEnabled(false);
        map.getZoomInButton().setEnabled(false);
        map.getZoomOutButton().setEnabled(false);

        map.getMainMap().setZoom(1);
        map.getMainMap().calculateZoomFrom(geoPositions);


        final Painter<JXMapViewer> polyLineOverlay = new Painter<JXMapViewer>() {

            public void paint(Graphics2D g, JXMapViewer map, int w, int h) {

                g = (Graphics2D) g.create();

                Rectangle rect = map.getViewportBounds();
                g.translate(-rect.x + 0, -rect.y + 0);
                g.setStroke(new BasicStroke(LineWidth));

                GeneralPath polyLine = new GeneralPath(GeneralPath.WIND_EVEN_ODD, TP.length);
                GeoPosition gp = new GeoPosition(TP[0].getLatitude(), TP[0].getLongitude());
                Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                polyLine.moveTo(pt.getX(), pt.getY());

                for (int i = 0; i < TP.length; i++) {

                    gp = new GeoPosition(TP[i].getLatitude(), TP[i].getLongitude());
                    pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                    polyLine.lineTo(pt.getX(), pt.getY());

                }

                switch (LineColor) {
                    case 0:
                        g.setColor(Color.BLUE);
                        break;
                    case 1:
                        g.setColor(Color.RED);
                        break;
                    case 2:
                        g.setColor(Color.YELLOW);
                        break;
                    case 3:
                        g.setColor(Color.GREEN);
                        break;
                    case 4:
                        g.setColor(Color.CYAN);
                        break;
                    case 5:
                        g.setColor(Color.WHITE);
                        break;
                    case 6:
                        g.setColor(Color.ORANGE);
                        break;
                    case 7:
                        g.setColor(Color.PINK);
                        break;

                }

                g.draw(polyLine);


                g.dispose();
            }
        };


        mainPresentationTimer = new Timer(0, new ActionListener() {

            int i = 0;

            public void actionPerformed(ActionEvent e) {
                if (i < TP.length) {

                    temp.clear();

                    Waypoint wa = new Waypoint(TP[i].getLatitude(), TP[i].getLongitude());

                    MAPPSView.series.add(i, TP[i].getDeviceElevation());

                   if (MAPPSView.elevationsType.equals("INTERNET")) {
                   MAPPSView.series1.add(i, TP[i].getInternetElevation()); }

                    temp.add(wa);
                    WP.setWaypoints(temp);


                    WP.setRenderer(new WaypointRenderer() {

                        public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {

                            switch (presentationControlPointStyle) {
                                case 0:
                                    g.setColor(Color.magenta);
                                    g.drawOval(0, 0, presentationControlPointWidth, ControlPointWidth);
                                    break;
                                case 1:
                                    g.setColor(Color.magenta);
                                    g.fillOval(0, 0, presentationControlPointWidth + 2, presentationControlPointWidth + 2);
                                    break;
                                case 2:
                                    g.setColor(Color.magenta);
                                    g.drawRect(0, 0, presentationControlPointWidth, presentationControlPointWidth);
                                    break;
                                case 3:
                                    g.setColor(Color.magenta);
                                    g.fillRect(0, 0, presentationControlPointWidth + 2, presentationControlPointWidth + 2);
                                    break;
                                case 4:
                                    g.drawImage(redPin, -10, -20, null);
                                    break;
                                case 5:
                                    if (trackType.equals("Walk")) {
                                        g.drawImage(hicker, - 10, -10, null); }
                                    else if (trackType.equals("Bicycle")) {
                                        g.drawImage(bicycle, - 10, - 10, null); }
                                    else if (trackType.equals("Car")) {
                                        g.drawImage(car, - 10, - 10, null); }
                                    else if (trackType.equals("Paraglide")) {
                                        g.drawImage(paraglide, - 10, - 10, null); }
                                    else if (trackType.equals("Boat")) {
                                        g.drawImage(boat , - 10, - 10, null); }
                                    else if (trackType.equals("Airplane")) {
                                        g.drawImage(airplane, - 10, - 10, null); }
                                    else if (trackType.equals("Canoe")) {
                                        g.drawImage(canoe, - 10, - 10, null); }
                                    else if (trackType.equals("Ski")) {
                                        g.drawImage(ski, - 10, - 10, null);}

                                    break;
                            }

                            return true;
                        }
                    });

                    cp.setCacheable(false);
                    cp.setPainters(polyLineOverlay, WP);
                    map.getMainMap().setOverlayPainter(cp);

                    i++;

                    presentFilesA(i - 1);

                } else {

                    if (cyclePresentation) {
                        i = 0;
                        MAPPSView.series.clear();
                        MAPPSView.series1.clear();
                    }
                    else {
                    MAPPSView.isPresented = false;

                    mainPresentationTimer.stop();

                    secondMultimediaSearchTimer.stop();

                    drawMap();
                    }

                }
                mainPresentationTimer.setDelay(MAPPSView.speed);
            }
        });
        mainPresentationTimer.start();

    }

    /**
     * automatická prezentácia v režime Dynamic Clear Presentation (DCP)
     */
    public void dynamicClearPresentation() {

        MAPPSView.series.clear();
        MAPPSView.series1.clear();

        MAPPSView.isPresented = true;

        //LineColor = SettingsLoader.getInstance().getLineColor();
        final int presentationControlPointWidth = SettingsLoader.getInstance().getPresentationControlPointWidth();
        final int presentationControlPointStyle = SettingsLoader.getInstance().getPresentationControlPoint();

        final Set<Waypoint> temp = new HashSet<Waypoint>();

        final WaypointPainter WP = new WaypointPainter();
        final CompoundPainter cp = new CompoundPainter();


        map.getMainMap().setZoom(2);


        final Painter<JXMapViewer> polyLineOverlay = new Painter<JXMapViewer>() {

            public void paint(Graphics2D g, JXMapViewer map, int w, int h) {

                g = (Graphics2D) g.create();

                Rectangle rect = map.getViewportBounds();
                g.translate(-rect.x + 0, -rect.y + 0);
                g.setStroke(new BasicStroke(LineWidth));

                GeneralPath polyLine = new GeneralPath(GeneralPath.WIND_EVEN_ODD, TP.length);
                GeoPosition gp = new GeoPosition(TP[0].getLatitude(), TP[0].getLongitude());
                Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                polyLine.moveTo(pt.getX(), pt.getY());

                for (int i = 0; i < TP.length; i++) {

                    gp = new GeoPosition(TP[i].getLatitude(), TP[i].getLongitude());
                    pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                    polyLine.lineTo(pt.getX(), pt.getY());

                }

                switch (LineColor) {
                    case 0:
                        g.setColor(Color.BLUE);
                        break;
                    case 1:
                        g.setColor(Color.RED);
                        break;
                    case 2:
                        g.setColor(Color.YELLOW);
                        break;
                    case 3:
                        g.setColor(Color.GREEN);
                        break;
                    case 4:
                        g.setColor(Color.CYAN);
                        break;
                    case 5:
                        g.setColor(Color.WHITE);
                        break;
                    case 6:
                        g.setColor(Color.ORANGE);
                        break;
                    case 7:
                        g.setColor(Color.PINK);
                        break;

                }

                g.draw(polyLine);

                g.dispose();
            }
        };

        mainPresentationTimer = new Timer(0, new ActionListener() {

            int i = 0;

            public void actionPerformed(ActionEvent e) {
                if (i < TP.length) {

                    temp.clear();

                    Waypoint wa = new Waypoint(TP[i].getLatitude(), TP[i].getLongitude());
                    map.getMainMap().setCenterPosition(new GeoPosition(TP[i].getLatitude(), TP[i].getLongitude()));

                    MAPPSView.series.add(i, TP[i].getDeviceElevation());

                    if (MAPPSView.elevationsType.equals("INTERNET")) {
                    MAPPSView.series1.add(i, TP[i].getInternetElevation()); }

                    temp.add(wa);
                    WP.setWaypoints(temp);


                    WP.setRenderer(new WaypointRenderer() {

                        public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {

                            switch (presentationControlPointStyle) {
                                case 0:
                                    g.setColor(Color.magenta);
                                    g.drawOval(0, 0, presentationControlPointWidth, presentationControlPointWidth);
                                    break;
                                case 1:
                                    g.setColor(Color.magenta);
                                    g.fillOval(0, 0, presentationControlPointWidth + 2, presentationControlPointWidth + 2);
                                    break;
                                case 2:
                                    g.setColor(Color.magenta);
                                    g.drawRect(0, 0, presentationControlPointWidth, presentationControlPointWidth);
                                    break;
                                case 3:
                                    g.setColor(Color.magenta);
                                    g.fillRect(0, 0, presentationControlPointWidth + 2, presentationControlPointWidth + 2);
                                    break;
                                case 4:
                                    g.drawImage(redPin, -10, -20, null);
                                    break;
                                case 5:
                                    if (trackType.equals("Walk")) {
                                        g.drawImage(hicker, - 10, -10, null); }
                                    else if (trackType.equals("Bicycle")) {
                                        g.drawImage(bicycle, - 10, - 10, null); }
                                    else if (trackType.equals("Car")) {
                                        g.drawImage(car, - 10, - 10, null); }
                                    else if (trackType.equals("Paraglide")) {
                                        g.drawImage(paraglide, - 10, - 10, null); }
                                    else if (trackType.equals("Boat")) {
                                        g.drawImage(boat , - 10, - 10, null); }
                                    else if (trackType.equals("Airplane")) {
                                        g.drawImage(airplane, - 10, - 10, null); }
                                    else if (trackType.equals("Canoe")) {
                                        g.drawImage(canoe, - 10, - 10, null); }
                                    else if (trackType.equals("Ski")) {
                                        g.drawImage(ski, - 10, - 10, null);}

                                    break;
                            }

                            return true;
                        }
                    });

                    cp.setCacheable(false);
                    cp.setPainters(polyLineOverlay, WP);
                    map.getMainMap().setOverlayPainter(cp);

                    i++;

                    presentFilesA(i - 1);

                } else {
                    if (cyclePresentation) {
                        i = 0;
                        MAPPSView.series.clear();
                        MAPPSView.series1.clear();
                    }
                    else {
                    MAPPSView.isPresented = false;

                    mainPresentationTimer.stop();

                    secondMultimediaSearchTimer.stop();

                    drawMap();
                    }
                }
                mainPresentationTimer.setDelay(MAPPSView.speed);
            }
        });
        mainPresentationTimer.start();

    }

    /**
     * krok vpred v manuálnom móde prezentácie
     */
    public void manualStepForward() {

        if (isViewingMedia == false) {

            isStepping = true;

            if (wasStepBack == true)

                step++;

            MAPPSView.isPresented = true;

            final Set<Waypoint> temp = new HashSet<Waypoint>();

            final WaypointPainter WP = new WaypointPainter();
            final CompoundPainter cp = new CompoundPainter();

            final Painter<JXMapViewer> polyLineOverlay = new Painter<JXMapViewer>() {

                public void paint(Graphics2D g, JXMapViewer map, int w, int h) {

                    g = (Graphics2D) g.create();

                    Rectangle rect = map.getViewportBounds();
                    g.translate(-rect.x + 0, -rect.y + 0);
                    g.setStroke(new BasicStroke(LineWidth));

                    GeneralPath polyLine = new GeneralPath(GeneralPath.WIND_EVEN_ODD, TP.length);
                    GeoPosition gp = new GeoPosition(TP[0].getLatitude(), TP[0].getLongitude());
                    Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                    polyLine.moveTo(pt.getX(), pt.getY());

                    for (int i = 0; i < TP.length; i++) {

                        gp = new GeoPosition(TP[i].getLatitude(), TP[i].getLongitude());
                        pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                        polyLine.lineTo(pt.getX(), pt.getY());
                    
                        if (isFiles[i] == true) {

                            g.drawImage(bluePin, (int) pt.getX() - 10, (int) pt.getY() - 20, null);

                        }
                    }

                    switch (LineColor) {
                        case 0:
                            g.setColor(Color.BLUE);
                            break;
                        case 1:
                            g.setColor(Color.RED);
                            break;
                        case 2:
                            g.setColor(Color.YELLOW);
                            break;
                        case 3:
                            g.setColor(Color.GREEN);
                            break;
                        case 4:
                            g.setColor(Color.CYAN);
                            break;
                        case 5:
                            g.setColor(Color.WHITE);
                            break;
                        case 6:
                            g.setColor(Color.ORANGE);
                            break;
                        case 7:
                            g.setColor(Color.PINK);
                            break;

                    }

                    g.draw(polyLine);


                    g.dispose();
                }
            };
       
            if (step < TP.length - 1) {

                temp.clear();

                Waypoint wa = new Waypoint(TP[step].getLatitude(), TP[step].getLongitude());

                MAPPSView.series2.clear();

                if (step != 0 && step < TP.length-1) {

                    MAPPSView.series2.add(step - 1, TP[step - 1].getDeviceElevation());
                    MAPPSView.series2.add(step, TP[step].getDeviceElevation());
                    MAPPSView.series2.add(step + 1, TP[step + 1].getDeviceElevation());
                }
                else if (step == 0) {
                    MAPPSView.series2.add(step, TP[step].getDeviceElevation());
                    MAPPSView.series2.add(step + 1, TP[step + 1].getDeviceElevation());

                }
                else if (step == TP.length) {
                    MAPPSView.series2.add(step, TP[step].getDeviceElevation());
                }

                temp.add(wa);
                WP.setWaypoints(temp);

                WP.setRenderer(new WaypointRenderer() {

                    public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {

                        switch (presentationControlPointM) {
                            case 0:
                                g.setColor(Color.magenta);
                                g.drawOval(0, 0, presentationControlPointWidthM, presentationControlPointWidthM);
                                break;
                            case 1:
                                g.setColor(Color.magenta);
                                g.fillOval(0, 0, presentationControlPointWidthM + 2, presentationControlPointWidthM + 2);
                                break;
                            case 2:
                                g.setColor(Color.magenta);
                                g.drawRect(0, 0, presentationControlPointWidthM, presentationControlPointWidthM);
                                break;
                            case 3:
                                g.setColor(Color.magenta);
                                g.fillRect(0, 0, presentationControlPointWidthM + 2, presentationControlPointWidthM + 2);
                                break;
                            case 4:
                                g.drawImage(redPin, -10, -20, null);
                                break;
                            case 5:
                                if (trackType.equals("Walk")) {
                                        g.drawImage(hicker, - 10, -10, null); }
                                    else if (trackType.equals("Bicycle")) {
                                        g.drawImage(bicycle, - 10, - 10, null); }
                                    else if (trackType.equals("Car")) {
                                        g.drawImage(car, - 10, - 10, null); }
                                    else if (trackType.equals("Paraglide")) {
                                        g.drawImage(paraglide, - 10, - 10, null); }
                                    else if (trackType.equals("Boat")) {
                                        g.drawImage(boat, - 10, - 10, null); }
                                    else if (trackType.equals("Airplane")) {
                                        g.drawImage(airplane, - 10, - 10, null); }
                                    else if (trackType.equals("Canoe")) {
                                        g.drawImage(canoe, - 10, - 10, null); }
                                    else if (trackType.equals("Ski")) {
                                        g.drawImage(ski, - 10, - 10, null);}

                                break;
                        }

                        g.dispose();
                        return true;
                    }
                });

                

                cp.setCacheable(false);
                cp.setPainters(polyLineOverlay, WP);
                map.getMainMap().setOverlayPainter(cp);

                step++;
                
                if (step != 0)
                MAPPSView.jSlider2.setValue(step-1);

                presentFilesM(step - 1);


            } else {
                MAPPSView.isPresented = false;
                if (secondMultimediaSearchTimer != null)
                secondMultimediaSearchTimer.stop();

                drawMap();
                step = 0;

                MAPPSView.series2.clear();

            }
        }

        isStepping = false;
        wasStepBack = false;

    }


    /**
     * krok vzad v manuálnom móde prezentácie
     */
    public void manualStepBackward() {

        wasStepBack = true;

        if (isViewingMedia == false) {

            isStepping = true;

            MAPPSView.isPresented = true;

            final Set<Waypoint> temp = new HashSet<Waypoint>();

            final WaypointPainter WP = new WaypointPainter();
            final CompoundPainter cp = new CompoundPainter();

            final Painter<JXMapViewer> polyLineOverlay = new Painter<JXMapViewer>() {

                public void paint(Graphics2D g, JXMapViewer map, int w, int h) {

                    g = (Graphics2D) g.create();

                    Rectangle rect = map.getViewportBounds();
                    g.translate(-rect.x + 0, -rect.y + 0);
                    g.setStroke(new BasicStroke(LineWidth));

                    GeneralPath polyLine = new GeneralPath(GeneralPath.WIND_EVEN_ODD, TP.length);
                    GeoPosition gp = new GeoPosition(TP[0].getLatitude(), TP[0].getLongitude());
                    Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                    polyLine.moveTo(pt.getX(), pt.getY());

                    for (int i = 0; i < TP.length; i++) {
                        gp = new GeoPosition(TP[i].getLatitude(), TP[i].getLongitude());
                        pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                        polyLine.lineTo(pt.getX(), pt.getY());

                        if (isFiles[i] == true) {

                            g.drawImage(bluePin, (int) pt.getX() - 10, (int) pt.getY() - 20, null);

                        }
                    }

                    switch (LineColor) {
                        case 0:
                            g.setColor(Color.BLUE);
                            break;
                        case 1:
                            g.setColor(Color.RED);
                            break;
                        case 2:
                            g.setColor(Color.YELLOW);
                            break;
                        case 3:
                            g.setColor(Color.GREEN);
                            break;
                        case 4:
                            g.setColor(Color.CYAN);
                            break;
                        case 5:
                            g.setColor(Color.WHITE);
                            break;
                        case 6:
                            g.setColor(Color.ORANGE);
                            break;
                        case 7:
                            g.setColor(Color.PINK);
                            break;

                    }

                    g.draw(polyLine);


                    g.dispose();
                }
            };

            step = MAPPSView.jSlider2.getValue();

            if (step > 0) {

                step--;

                MAPPSView.jSlider2.setValue(step);

                temp.clear();

                Waypoint wa = new Waypoint(TP[step].getLatitude(), TP[step].getLongitude());

                MAPPSView.series2.clear();

                if (step != 0 && step < TP.length-1) {

                    MAPPSView.series2.add(step - 1, TP[step - 1].getDeviceElevation());
                    MAPPSView.series2.add(step, TP[step].getDeviceElevation());
                    MAPPSView.series2.add(step + 1, TP[step + 1].getDeviceElevation());
                }
                else if (step == 0) {
                    MAPPSView.series2.add(step, TP[step].getDeviceElevation());
                    MAPPSView.series2.add(step + 1, TP[step + 1].getDeviceElevation());

                }
                else if (step == TP.length) {
                    MAPPSView.series2.add(step, TP[step].getDeviceElevation());
                }
                
                temp.add(wa);
                WP.setWaypoints(temp);


                WP.setRenderer(new WaypointRenderer() {

                    public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {

                        switch (presentationControlPointM) {
                            case 0:
                                g.setColor(Color.magenta);
                                g.drawOval(0, 0, presentationControlPointWidthM, presentationControlPointWidthM);
                                break;
                            case 1:
                                g.setColor(Color.magenta);
                                g.fillOval(0, 0, presentationControlPointWidthM + 2, presentationControlPointWidthM + 2);
                                break;
                            case 2:
                                g.setColor(Color.magenta);
                                g.drawRect(0, 0, presentationControlPointWidthM, presentationControlPointWidthM);
                                break;
                            case 3:
                                g.setColor(Color.magenta);
                                g.fillRect(0, 0, presentationControlPointWidthM + 2, presentationControlPointWidthM + 2);
                                break;
                            case 4:
                                g.drawImage(redPin, -10, -20, null);
                                break;
                            case 5:
                                 if (trackType.equals("Walk")) {
                                        g.drawImage(hicker, - 10, -10, null); }
                                    else if (trackType.equals("Bicycle")) {
                                        g.drawImage(bicycle, - 10, - 10, null); }
                                    else if (trackType.equals("Car")) {
                                        g.drawImage(car, - 10, - 10, null); }
                                    else if (trackType.equals("Paraglide")) {
                                        g.drawImage(paraglide, - 10, - 10, null); }
                                    else if (trackType.equals("Boat")) {
                                        g.drawImage(boat , - 10, - 10, null); }
                                    else if (trackType.equals("Airplane")) {
                                        g.drawImage(airplane, - 10, - 10, null); }
                                    else if (trackType.equals("Canoe")) {
                                        g.drawImage(canoe, - 10, - 10, null); }
                                    else if (trackType.equals("Ski")) {
                                        g.drawImage(ski, - 10, - 10, null);}

                                break;
                        }

                        g.dispose();
                        return true;
                    }
                });

                cp.setCacheable(false);
                cp.setPainters(polyLineOverlay, WP);
                map.getMainMap().setOverlayPainter(cp);

                presentFilesM(step);


            } else {
                MAPPSView.isPresented = false;
                if (secondMultimediaSearchTimer != null)
                secondMultimediaSearchTimer.stop();

                drawMap();
                step = 0;

                MAPPSView.series2.clear();

            }
        }

        isStepping = false;

    }



    /**
     * prehľadávanie a prezentácia multimediálnych súborov v automatickom režime
     * @param i - index bodu v tracklogu
     */
    public void presentFilesA(final int i) {

        mainPresentationTimer.stop();

        secondMultimediaSearchTimer = new Timer(MAPPSView.speed, new ActionListener() {

            int j = 0;

            public void actionPerformed(ActionEvent e) {

                secondMultimediaSearchTimer.setDelay(MAPPSView.speed);

                if (isFiles[i] == true) {

                    secondMultimediaSearchTimer.setDelay(0);
                    if (j < MultimediaFiles.size()) {
                        if (MultimediaFiles.get(j).getTrackPointIndex() == i) {
                            if (MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".jpg")) {
                                MP.start(MultimediaFiles.get(j).getPath());
                            } else if (MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".avi")
                                    || MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".mov")
                                    || MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".3gp")
                                    || MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".mp4")
                                    || MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".mpg")) {
                                VP.start(MultimediaFiles.get(j).getPath());
                            } else if (MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".mp3")
                                    || MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".amr")
                                    || MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".wav")) {
                                AP.start(MultimediaFiles.get(j).getPath());
                            } else if (MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".pdf")) {
                                PV.start(MultimediaFiles.get(j).getPath());
                            } else if (MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".txt")) {
                                TV.loadTextFile(MultimediaFiles.get(j).getPath());
                            }


                            j++;

                            mainPresentationTimer.stop();
                            secondMultimediaSearchTimer.stop();
                        } else {
                            j++;
                        }
                    } else if (j == MultimediaFiles.size()) {

                        secondMultimediaSearchTimer.setDelay(MAPPSView.speed);
                        mainPresentationTimer.restart();
                        secondMultimediaSearchTimer.stop();
                    }
                } else {
                    mainPresentationTimer.restart();
                    secondMultimediaSearchTimer.stop();
                }
            }
        });
        secondMultimediaSearchTimer.start();
    }


    /**
     * prehľadávanie a prezentácia multimediálnych súborov v manuálnom režime
     * @param i - index bodu v tracklogu
     */
    public void presentFilesM(final int i) {

        isViewingMedia = true;

        if (isFiles[i] == true) {
        
        secondMultimediaSearchTimer = new Timer(0, new ActionListener() {

            int j = 0;

            public void actionPerformed(ActionEvent e) {

                secondMultimediaSearchTimer.setDelay(0);

                    //MAPPSView.jSlider2.setEnabled(false);

                    secondMultimediaSearchTimer.setDelay(0);
                    if (j < MultimediaFiles.size()) {
                        if (MultimediaFiles.get(j).getTrackPointIndex() == i) {
                            if (MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".jpg")) {
                                MP.start(MultimediaFiles.get(j).getPath());
                            } else if (MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".avi")
                                    || MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".mov")
                                    || MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".3gp")
                                    || MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".mp4")
                                    || MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".mpg")) {
                                VP.start(MultimediaFiles.get(j).getPath());
                            } else if (MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".mp3")
                                    || MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".amr")
                                    || MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".wav")) {
                                AP.start(MultimediaFiles.get(j).getPath());
                            } else if (MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".pdf")) {
                                PV.start(MultimediaFiles.get(j).getPath());
                            } else if (MultimediaFiles.get(j).getPath().toLowerCase().endsWith(".txt")) {
                                TV.loadTextFile(MultimediaFiles.get(j).getPath());
                            }

                            j++;

                            secondMultimediaSearchTimer.stop();
                        } else {
                            j++;
                        }
                    } else if (j == MultimediaFiles.size()) {

                        secondMultimediaSearchTimer.setDelay(MAPPSView.speed);

                        secondMultimediaSearchTimer.stop();

                        isViewingMedia = false;
                    }
                }
            });
            secondMultimediaSearchTimer.start();

                } else {
                    isViewingMedia = false;
               }
               
    }

    /**
     * vykreslenie výškového profilu do grafu
     */
    public void drawElevation() {
        MAPPSView.series.clear();
        MAPPSView.series1.clear();

        plot = (XYPlot) MAPPSView.chart.getPlot();
        plot.getDomainAxis().setRange(0.0, (double) TP.length);

        int maxy = -500;
        int miny = 10000;

        for (int in = 0; in < TP.length; in++) {

            MAPPSView.series.add(in, TP[in].getDeviceElevation());
            
            if (MAPPSView.elevationsType.equals("INTERNET")) {
            
                MAPPSView.series1.add(in,TP[in].getInternetElevation());

            }

            if (maxy < TP[in].getDeviceElevation()) {
                maxy = TP[in].getDeviceElevation();
            }
            if (miny > TP[in].getDeviceElevation()) {
                miny = TP[in].getDeviceElevation();
            }
            if (MAPPSView.elevationsType.equals("INTERNET") && maxy < TP[in].getInternetElevation()) {
                maxy = TP[in].getInternetElevation();
            }
            if (MAPPSView.elevationsType.equals("INTERNET") && miny > TP[in].getInternetElevation()) {
                miny = TP[in].getInternetElevation();
            }
        }
        plot.getRangeAxis().setRange(miny - 10, maxy + 10);
    }


    /**
     * skok na ľubovoľnú pozíciu v tracklogu
     * @param i - index bodu v tracklogu
     */
    public void jumpTo(int i) {

        wasStepBack = true;

        this.step = i;

        if (isViewingMedia == false) {

            MAPPSView.isPresented = true;

            final Set<Waypoint> temp = new HashSet<Waypoint>();

            final WaypointPainter WP = new WaypointPainter();
            final CompoundPainter cp = new CompoundPainter();


            final Painter<JXMapViewer> polyLineOverlay = new Painter<JXMapViewer>() {

                public void paint(Graphics2D g, JXMapViewer map, int w, int h) {

                    g = (Graphics2D) g.create();

                    Rectangle rect = map.getViewportBounds();
                    g.translate(-rect.x + 0, -rect.y + 0);
                    g.setStroke(new BasicStroke(LineWidth));

                    GeneralPath polyLine = new GeneralPath(GeneralPath.WIND_EVEN_ODD, TP.length);
                    GeoPosition gp = new GeoPosition(TP[0].getLatitude(), TP[0].getLongitude());
                    Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                    polyLine.moveTo(pt.getX(), pt.getY());

                    for (int i = 0; i < TP.length; i++) {

                        gp = new GeoPosition(TP[i].getLatitude(), TP[i].getLongitude());
                        pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                        polyLine.lineTo(pt.getX(), pt.getY());

                        if (isFiles[i] == true) {

                            g.drawImage(bluePin, (int) pt.getX() - 10, (int) pt.getY() - 20, null);

                        }
                    }

                    switch (LineColor) {
                        case 0:
                            g.setColor(Color.BLUE);
                            break;
                        case 1:
                            g.setColor(Color.RED);
                            break;
                        case 2:
                            g.setColor(Color.YELLOW);
                            break;
                        case 3:
                            g.setColor(Color.GREEN);
                            break;
                        case 4:
                            g.setColor(Color.CYAN);
                            break;
                        case 5:
                            g.setColor(Color.WHITE);
                            break;
                        case 6:
                            g.setColor(Color.ORANGE);
                            break;
                        case 7:
                            g.setColor(Color.PINK);
                            break;

                    }

                    g.draw(polyLine);


                    g.dispose();
                }
            };


                temp.clear();

                Waypoint wa = new Waypoint(TP[i].getLatitude(), TP[i].getLongitude());

                MAPPSView.series2.clear();

                if (i != 0 && i < TP.length-1) {

                    MAPPSView.series2.add(i - 1, TP[i - 1].getDeviceElevation());
                    MAPPSView.series2.add(i, TP[i].getDeviceElevation());
                    MAPPSView.series2.add(i + 1, TP[i + 1].getDeviceElevation());
                }
                else if (i == 0) {
                    MAPPSView.series2.add(i, TP[i].getDeviceElevation());
                    MAPPSView.series2.add(i + 1, TP[i + 1].getDeviceElevation());

                }
                else if (i == TP.length) {
                    MAPPSView.series2.add(i, TP[i].getDeviceElevation());
                }

                temp.add(wa);
                WP.setWaypoints(temp);

                WP.setRenderer(new WaypointRenderer() {

                    public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {

                        switch (presentationControlPointM) {
                            case 0:
                                g.setColor(Color.magenta);
                                g.drawOval(0, 0, presentationControlPointWidthM, presentationControlPointWidthM);
                                break;
                            case 1:
                                g.setColor(Color.magenta);
                                g.fillOval(0, 0, presentationControlPointWidthM + 2, presentationControlPointWidthM + 2);
                                break;
                            case 2:
                                g.setColor(Color.magenta);
                                g.drawRect(0, 0, presentationControlPointWidthM, presentationControlPointWidthM);
                                break;
                            case 3:
                                g.setColor(Color.magenta);
                                g.fillRect(0, 0, presentationControlPointWidthM + 2, presentationControlPointWidthM + 2);
                                break;
                            case 4:
                                g.drawImage(redPin, -10, -20, null);
                                break;
                            case 5:
                                 if (trackType.equals("Walk")) {
                                        g.drawImage(hicker, - 10, -10, null); }
                                    else if (trackType.equals("Bicycle")) {
                                        g.drawImage(bicycle, - 10, - 10, null); }
                                    else if (trackType.equals("Car")) {
                                        g.drawImage(car, - 10, - 10, null); }
                                    else if (trackType.equals("Paraglide")) {
                                        g.drawImage(paraglide, - 10, - 10, null); }
                                    else if (trackType.equals("Boat")) {
                                        g.drawImage(boat , - 10, - 10, null); }
                                    else if (trackType.equals("Airplane")) {
                                        g.drawImage(airplane, - 10, - 10, null); }
                                    else if (trackType.equals("Canoe")) {
                                        g.drawImage(canoe, - 10, - 10, null); }
                                    else if (trackType.equals("Ski")) {
                                        g.drawImage(ski, - 10, - 10, null);}

                                 break;
                        }

                        g.dispose();
                        return true;
                    }
                });



                cp.setCacheable(false);
                cp.setPainters(polyLineOverlay, WP);
                map.getMainMap().setOverlayPainter(cp);

                presentFilesM(i);
                
            }
        }
    }