/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package usersettings;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Trieda určená na prácu s konfiguračnými súbormi a ich dátami
 * @author Ľubomír Petrus, Matej Pazdič
 */
public class SettingsLoader {

    private String gpxRecentFolder = System.getProperty("user.home");
    private String multimediaRecentFolder = System.getProperty("user.home");
    private String kmlRecentFolder = System.getProperty("user.home");
    private String g7tRecentFolder = System.getProperty("user.home");
    private String tlvRecentFolder = System.getProperty("user.home");
    private int lineColor = 0;
    private float lineWidth = 2F;
    private boolean drawLine = true;
    private int controlPointColor = 1;
    private int controlPointWidth = 3;
    private int controlPoint = 0;
    private int presentationControlPointColor = 1;
    private int presentationControlPointWidth = 3;
    private int presentationControlPoint = 5;
    private boolean drawControlPoint = true;
    private double pictureShowingLength = 5.0;
    private boolean showingLastPicture = false;
    private double documentsShowingLength = 10.0;
    private int PdfDpi = 150;
    private boolean closingMediaPlayers = true;
    private boolean cyclingPresentation = false;
    private final File tempFile = new File("." + System.getProperty("file.separator") + "temp" + System.getProperty("file.separator") + "TLV_TEMPF.temp");
    private static SettingsLoader instance = null;

    private SettingsLoader() {
    }

    /**
     * Konštruktor typu singleton triedy SettingsLoader
     * @return vracia singleton triedy
     */
    public static SettingsLoader getInstance() {
        if (instance == null) {
            instance = new SettingsLoader();
        }
        return instance;
    }

    /**
     * Metóda určená na načítanie konfiguračných údajov zo súboru
     */
    public void refreshSetttings() {
        if (tempFile.exists()) {
            try {
                DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();
                DocumentBuilder DB = DBF.newDocumentBuilder();
                org.w3c.dom.Document DOC = DB.parse(tempFile);
                DOC.getDocumentElement().normalize();
                Node tempNode = DOC.getElementsByTagName("TRACKLOGVIEWER_SETTINGS").item(0);
                Element tempElem = (Element) tempNode;

                Node tempNode1 = tempElem.getElementsByTagName("GPX_RECENT_FOLDER").item(0);
                Element tempElem1 = (Element) tempNode1;
                gpxRecentFolder = tempElem1.getTextContent();

                Node tempNode2 = tempElem.getElementsByTagName("MULTIMEDIA_RECENT_FOLDER").item(0);
                Element tempElem2 = (Element) tempNode2;
                multimediaRecentFolder = tempElem2.getTextContent();

                Node tempNode2apol = tempElem.getElementsByTagName("KML_RECENT_FOLDER").item(0);
                Element tempElem2apol = (Element) tempNode2apol;
                kmlRecentFolder = tempElem2apol.getTextContent();

                Node tempNode2apolapol = tempElem.getElementsByTagName("G7T_RECENT_FOLDER").item(0);
                Element tempElem2apolapol = (Element) tempNode2apolapol;
                g7tRecentFolder = tempElem2apolapol.getTextContent();

                Node tempNode3 = tempElem.getElementsByTagName("TLV_RECENT_FOLDER").item(0);
                Element tempElem3 = (Element) tempNode3;
                tlvRecentFolder = tempElem3.getTextContent();

                Node tempNode4 = tempElem.getElementsByTagName("LINE").item(0);
                Element tempElem4 = (Element) tempNode4;
                lineWidth = Float.parseFloat(tempElem4.getElementsByTagName("WIDTH").item(0).getTextContent());
                lineColor = Integer.parseInt(tempElem4.getElementsByTagName("COLOR").item(0).getTextContent());
                drawLine = Boolean.parseBoolean(tempElem4.getElementsByTagName("DRAW").item(0).getTextContent());

                Node tempNode5 = tempElem.getElementsByTagName("CONTROL_POINT").item(0);
                Element tempElem5 = (Element) tempNode5;
                controlPoint = Integer.parseInt(tempElem5.getElementsByTagName("TYPE").item(0).getTextContent());
                controlPointWidth = Integer.parseInt(tempElem5.getElementsByTagName("WIDTH").item(0).getTextContent());
                controlPointColor = Integer.parseInt(tempElem5.getElementsByTagName("COLOR").item(0).getTextContent());
                drawControlPoint = Boolean.parseBoolean(tempElem5.getElementsByTagName("DRAW").item(0).getTextContent());

                Node tempNode6 = tempElem.getElementsByTagName("PRESENTATION_CONTROL_POINT").item(0);
                Element tempElem6 = (Element) tempNode6;
                presentationControlPoint = Integer.parseInt(tempElem6.getElementsByTagName("TYPE").item(0).getTextContent());
                presentationControlPointWidth = Integer.parseInt(tempElem6.getElementsByTagName("WIDTH").item(0).getTextContent());
                presentationControlPointColor = Integer.parseInt(tempElem6.getElementsByTagName("COLOR").item(0).getTextContent());


                Node tempNode7 = tempElem.getElementsByTagName("MULTIMEDIA_PLAYERS").item(0);
                Element tempElem7 = (Element) tempNode7;
                pictureShowingLength = Double.parseDouble(tempElem7.getElementsByTagName("PICTURE_SHOWING_LENGTH").item(0).getTextContent());
                documentsShowingLength = Double.parseDouble(tempElem7.getElementsByTagName("DOCUMENTS_SHOWING_LENGTH").item(0).getTextContent());
                PdfDpi = Integer.parseInt(tempElem7.getElementsByTagName("PDF_DPI").item(0).getTextContent());
                showingLastPicture = Boolean.parseBoolean(tempElem7.getElementsByTagName("SHOWING_LAST_PICTURE").item(0).getTextContent());
                closingMediaPlayers = Boolean.parseBoolean(tempElem7.getElementsByTagName("CLOSING_MEDIA_PLAYERS").item(0).getTextContent());

                Node tempNode8 = tempElem.getElementsByTagName("PRESENTATION").item(0);
                Element tempElem8 = (Element) tempNode8;
                cyclingPresentation = Boolean.parseBoolean(tempElem8.getElementsByTagName("CYCLING_PRESENTATION").item(0).getTextContent());


            } catch (SAXException ex) {
                Logger.getLogger(SettingsLoader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SettingsLoader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(SettingsLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            saveTempFile();
        }
    }

    /**
     * Metóda určená na vygenerovanie konfiguračného súboru
     */
    public void saveTempFile() {
        try {
            if (tempFile.exists()) {
                try {
                    tempFile.delete();
                    tempFile.createNewFile();
                } catch (IOException ex) {
                    System.out.println("ERROR: Cannot create tempFile!!!");
                }
            }
            if (!tempFile.exists()) {
                try {
                    new File("." + System.getProperty("file.separator") + "temp").mkdir();
                    tempFile.createNewFile();
                } catch (IOException ex) {
                    System.out.println("ERROR: Cannot create tempFile!!!");
                }
            }

            DocumentBuilderFactory DBF1 = DocumentBuilderFactory.newInstance();
            DocumentBuilder DB1 = DBF1.newDocumentBuilder();
            org.w3c.dom.Document document = DB1.newDocument();
            org.w3c.dom.Element rootElement = document.createElement("TRACKLOGVIEWER_SETTINGS");
            document.appendChild(rootElement);
            org.w3c.dom.Element rootElement3 = document.createElement("GPX_RECENT_FOLDER");
            rootElement3.appendChild(document.createTextNode(gpxRecentFolder));
            rootElement.appendChild(rootElement3);
            org.w3c.dom.Element rootElement4 = document.createElement("MULTIMEDIA_RECENT_FOLDER");
            rootElement4.appendChild(document.createTextNode(multimediaRecentFolder));
            rootElement.appendChild(rootElement4);
            org.w3c.dom.Element rootElement4apol = document.createElement("KML_RECENT_FOLDER");
            rootElement4apol.appendChild(document.createTextNode(kmlRecentFolder));
            rootElement.appendChild(rootElement4apol);
            org.w3c.dom.Element rootElement4apolapol = document.createElement("G7T_RECENT_FOLDER");
            rootElement4apolapol.appendChild(document.createTextNode(g7tRecentFolder));
            rootElement.appendChild(rootElement4apolapol);
            org.w3c.dom.Element rootElement5 = document.createElement("TLV_RECENT_FOLDER");
            rootElement5.appendChild(document.createTextNode(tlvRecentFolder));
            rootElement.appendChild(rootElement5);
            org.w3c.dom.Element rootElement6 = document.createElement("LINE");
            rootElement.appendChild(rootElement6);
            org.w3c.dom.Element lineElem1 = document.createElement("WIDTH");
            lineElem1.appendChild(document.createTextNode(String.valueOf(lineWidth)));
            rootElement6.appendChild(lineElem1);
            org.w3c.dom.Element lineElem2 = document.createElement("COLOR");
            lineElem2.appendChild(document.createTextNode(String.valueOf(lineColor)));
            rootElement6.appendChild(lineElem2);
            org.w3c.dom.Element lineElem2apol = document.createElement("DRAW");
            lineElem2apol.appendChild(document.createTextNode(String.valueOf(drawLine)));
            rootElement6.appendChild(lineElem2apol);
            org.w3c.dom.Element rootElement7 = document.createElement("CONTROL_POINT");
            rootElement.appendChild(rootElement7);
            org.w3c.dom.Element controlElem1 = document.createElement("TYPE");
            controlElem1.appendChild(document.createTextNode(String.valueOf(controlPoint)));
            rootElement7.appendChild(controlElem1);
            org.w3c.dom.Element controlElem2 = document.createElement("WIDTH");
            controlElem2.appendChild(document.createTextNode(String.valueOf(controlPointWidth)));
            rootElement7.appendChild(controlElem2);
            org.w3c.dom.Element controlElem3 = document.createElement("COLOR");
            controlElem3.appendChild(document.createTextNode(String.valueOf(controlPointColor)));
            rootElement7.appendChild(controlElem3);
            org.w3c.dom.Element controlElem3apol = document.createElement("DRAW");
            controlElem3apol.appendChild(document.createTextNode(String.valueOf(drawControlPoint)));
            rootElement7.appendChild(controlElem3apol);

            org.w3c.dom.Element rootElement8 = document.createElement("MULTIMEDIA_PLAYERS");
            rootElement.appendChild(rootElement8);
            org.w3c.dom.Element multimPlayersElem1 = document.createElement("PICTURE_SHOWING_LENGTH");
            multimPlayersElem1.appendChild(document.createTextNode(String.valueOf(pictureShowingLength)));
            rootElement8.appendChild(multimPlayersElem1);

            org.w3c.dom.Element multimPlayersElem1apol = document.createElement("DOCUMENTS_SHOWING_LENGTH");
            multimPlayersElem1apol.appendChild(document.createTextNode(String.valueOf(documentsShowingLength)));
            rootElement8.appendChild(multimPlayersElem1apol);

            org.w3c.dom.Element multimPlayersElem1apolpol = document.createElement("PDF_DPI");
            multimPlayersElem1apolpol.appendChild(document.createTextNode(String.valueOf(PdfDpi)));
            rootElement8.appendChild(multimPlayersElem1apolpol);

            org.w3c.dom.Element multimPlayersElem2 = document.createElement("SHOWING_LAST_PICTURE");
            multimPlayersElem2.appendChild(document.createTextNode(String.valueOf(showingLastPicture)));
            rootElement8.appendChild(multimPlayersElem2);
            org.w3c.dom.Element multimPlayersElem3 = document.createElement("CLOSING_MEDIA_PLAYERS");
            multimPlayersElem3.appendChild(document.createTextNode(String.valueOf(closingMediaPlayers)));
            rootElement8.appendChild(multimPlayersElem3);

            org.w3c.dom.Element rootElement9 = document.createElement("PRESENTATION");
            rootElement.appendChild(rootElement9);
            org.w3c.dom.Element presentationElem1 = document.createElement("CYCLING_PRESENTATION");
            presentationElem1.appendChild(document.createTextNode(String.valueOf(cyclingPresentation)));
            rootElement9.appendChild(presentationElem1);

            org.w3c.dom.Element rootElement10 = document.createElement("PRESENTATION_CONTROL_POINT");
            rootElement.appendChild(rootElement10);
            org.w3c.dom.Element presentationControlElem1 = document.createElement("TYPE");
            presentationControlElem1.appendChild(document.createTextNode(String.valueOf(presentationControlPoint)));
            rootElement10.appendChild(presentationControlElem1);
            org.w3c.dom.Element presentationControlElem2 = document.createElement("WIDTH");
            presentationControlElem2.appendChild(document.createTextNode(String.valueOf(presentationControlPointWidth)));
            rootElement10.appendChild(presentationControlElem2);
            org.w3c.dom.Element presentationControlElem3 = document.createElement("COLOR");
            presentationControlElem3.appendChild(document.createTextNode(String.valueOf(presentationControlPointColor)));
            rootElement10.appendChild(presentationControlElem3);

            TransformerFactory TF = TransformerFactory.newInstance();
            Transformer T = TF.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(tempFile);
            T.transform(source, result);
            DB1.reset();

        } catch (TransformerException ex) {
            System.out.println("ERROR: Cannot create tempFile!!!");
        } catch (ParserConfigurationException ex) {
            System.out.println("ERROR: Cannot create tempFile!!!");
        }

    }

    /**
     * @return Vracia posledne použitý pričinok pre načitávanie gpx vstupného súboru
     */
    public String getGpxRecentFolder() {
        this.refreshSetttings();
        return gpxRecentFolder;
    }

    /**
     * @param gpxRecentFolder - Použítý priečinok pri načítavaní gpx vstupného súboru
     */
    public void setGpxRecentFolder(String gpxRecentFolder) {
        this.gpxRecentFolder = gpxRecentFolder;
        this.saveTempFile();
    }

    /**
     * @return Vracia posledne použitý priečinok, ktorý bol použitý pre automatické prehľadávanie multimediálnych súborov
     */
    public String getMultimediaRecentFolder() {
        this.refreshSetttings();
        return multimediaRecentFolder;
    }

    /**
     * @param multimediaRecentFolder - Priečinok, ktorý je použitý na automatické prehliadanie multimediálnych súborov
     */
    public void setMultimediaRecentFolder(String multimediaRecentFolder) {
        this.multimediaRecentFolder = multimediaRecentFolder;
        this.saveTempFile();
    }

    /**
     * @return Vracia posledne použitý pričinok pre otvorenie tlv súboru
     */
    public String getTlvRecentFolder() {
        this.refreshSetttings();
        return tlvRecentFolder;
    }

    /**
     * @param tlvRecentFolder - Priečinok, použitý pri otvorení súboru s príponou .tlv
     */
    public void setTlvRecentFolder(String tlvRecentFolder) {
        this.tlvRecentFolder = tlvRecentFolder;
        this.saveTempFile();
    }

    /**
     * @return Vracia farbu čiary tracklogu
     */
    public int getLineColor() {
        this.refreshSetttings();
        return lineColor;
    }

    /**
     * @param lineColor - Farba čiary tracklogu
     */
    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
        this.saveTempFile();
    }

    /**
     * @return Vracia hrúbku čiary tracklogu
     */
    public float getLineWidth() {
        this.refreshSetttings();
        return lineWidth;
    }

    /**
     * @param lineWidth - Hrúbka čiary tracklogu
     */
    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
        this.saveTempFile();
    }

    /**
     * @return Vracia farbu bodu tracklogu
     */
    public int getControlPointColor() {
        this.refreshSetttings();
        return controlPointColor;
    }

    /**
     * @param controlPointColor - Farba bodu tracklogu
     */
    public void setControlPointColor(int controlPointColor) {
        this.controlPointColor = controlPointColor;
        this.saveTempFile();
    }

    /**
     * @return Vracia veľkosť bodu tracklogu
     */
    public int getControlPointWidth() {
        this.refreshSetttings();
        return controlPointWidth;
    }

    /**
     * @param controlPointWidth - Veľkosť bodu tracklogu
     */
    public void setControlPointWidth(int controlPointWidth) {
        this.controlPointWidth = controlPointWidth;
        this.saveTempFile();
    }

    /**
     * @return Vracia typ bodu tracklogu
     */
    public int getControlPoint() {
        this.refreshSetttings();
        return controlPoint;
    }

    /**
     * @param controlPoint - Typ bodu trakcklogu
     */
    public void setControlPoint(int controlPoint) {
        this.controlPoint = controlPoint;
        this.saveTempFile();
    }

    /**
     * @return Vracia farbu bodu tracklogu pri prezentácii
     */
    public int getPresentationControlPointColor() {
        this.refreshSetttings();
        return presentationControlPointColor;
    }

    /**
     * @param presentationControlPointColor - Farba bodu tracklogu pri prezentácii
     */
    public void setPresentationControlPointColor(int presentationControlPointColor) {
        this.presentationControlPointColor = presentationControlPointColor;
        this.saveTempFile();
    }

    /**
     * @return Vracia veľkosť bodu tracklogu pri prezetácii
     */
    public int getPresentationControlPointWidth() {
        this.refreshSetttings();
        return presentationControlPointWidth;
    }

    /**
     * @param presentationControlPointWidth - Veľkosť bodu tracklogu pri prezetácii
     */
    public void setPresentationControlPointWidth(int presentationControlPointWidth) {
        this.presentationControlPointWidth = presentationControlPointWidth;
        this.saveTempFile();
    }

    /**
     * @return Vracia typ bodu tracklogu pri prezentácii
     */
    public int getPresentationControlPoint() {
        this.refreshSetttings();
        return presentationControlPoint;
    }

    /**
     * @param presentationControlPoint - Typ bodu tracklogu pri prezentácii
     */
    public void setPresentationControlPoint(int presentationControlPoint) {
        this.presentationControlPoint = presentationControlPoint;
        this.saveTempFile();
    }

    /**
     * @return Vracia posledne použitý pričinok pre načitávanie kml vstupného súboru
     */
    public String getKmlRecentFolder() {
        this.refreshSetttings();
        return kmlRecentFolder;
    }

    /**
     * @param kmlRecentFolder - Použítý priečinok pri načítavaní kml vstupného súboru
     */
    public void setKmlRecentFolder(String kmlRecentFolder) {
        this.kmlRecentFolder = kmlRecentFolder;
        this.saveTempFile();
    }

    /**
     * @return Vracia posledne použitý pričinok pre načitávanie g7t vstupného súboru
     */
    public String getG7tRecentFolder() {
        this.refreshSetttings();
        return g7tRecentFolder;
    }

    /**
     * @param g7tRecentFolder - Použítý priečinok pri načítavaní g7t vstupného súboru
     */
    public void setG7tRecentFolder(String g7tRecentFolder) {
        this.g7tRecentFolder = g7tRecentFolder;
        this.saveTempFile();
    }

    /**
     * @return Vracia, či má byť vykreslená čiara tracklogu
     */
    public boolean isDrawLine() {
        this.refreshSetttings();
        return drawLine;
    }

    /**
     * @param drawLine - Booleanská hodnota, popisujúca podmienku vykreslenia čiary tracklogu
     */
    public void setDrawLine(boolean drawLine) {
        this.drawLine = drawLine;
        this.saveTempFile();
    }

    /**
     * @return Vracia, či má byť vykreslené body tracklogu
     */
    public boolean isDrawControlPoint() {
        this.refreshSetttings();
        return drawControlPoint;
    }

    /**
     * @param drawControlPoint - Booleanská hodnota, popisujúca podmienku vykreslenia bodov tracklogu
     */
    public void setDrawControlPoint(boolean drawControlPoint) {
        this.drawControlPoint = drawControlPoint;
        this.saveTempFile();
    }

    /**
     * @return Vracia dĺžku zobrazenia fotky
     */
    public double getPictureShowingLength() {
        this.refreshSetttings();
        return pictureShowingLength;
    }

    /**
     * @param pictureShowingLength - Dĺžka zobrazenia fotky
     */
    public void setPictureShowingLength(double pictureShowingLength) {
        this.pictureShowingLength = pictureShowingLength;
        this.saveTempFile();
    }

    /**
     * @return Vracia, či má ostať zobrazená posledná fotka
     */
    public boolean isShowingLastPicture() {
        this.refreshSetttings();
        return showingLastPicture;
    }

    /**
     * @param showingLastPicture - Booleanská hodnota, popisujúca podmienku ostávania zobrazených fotiek
     */
    public void setShowingLastPicture(boolean showingLastPicture) {
        this.showingLastPicture = showingLastPicture;
        this.saveTempFile();
    }

    /**
     * @return Vracia, či majú ostávať zobrazené prehrávače po prehratí súboru
     */
    public boolean isClosingMediaPlayers() {
        this.refreshSetttings();
        return closingMediaPlayers;
    }

    /**
     * @param closingMediaPlayers - Booleanská hodnota, popisujúca podmienku ostávania zobrazených prehrávačov
     */
    public void setClosingMediaPlayers(boolean closingMediaPlayers) {
        this.closingMediaPlayers = closingMediaPlayers;
        this.saveTempFile();
    }

    /**
     * @return Vracia stav zacyklenia prezentácie
     */
    public boolean isCyclingPresentation() {
        this.refreshSetttings();
        return cyclingPresentation;
    }

    /**
     * @param cyclingPresentation - Booleanská hodnota, popisujúcu podmienku zacyklenia prezentácie
     */
    public void setCyclingPresentation(boolean cyclingPresentation) {
        this.cyclingPresentation = cyclingPresentation;
        this.saveTempFile();
    }

    /**
     * @return Vracia dĺžku zobrazenia elektronických dokumentov
     */
    public double getDocumentsShowingLength() {
        this.refreshSetttings();
        return documentsShowingLength;
    }

    /**
     * @param documentsShowingLength - Dĺžka zobrazenia elektronických dokumentov
     */
    public void setDocumentsShowingLength(double documentsShowingLength) {
        this.documentsShowingLength = documentsShowingLength;
        this.saveTempFile();
    }

    /**
     * @return Vracia veľkosť DPI pre renderovanie elektronických dokumentov
     */
    public int getPdfDpi() {
        this.refreshSetttings();
        return PdfDpi;
    }

    /**
     * @param PdfDpi - veľkosť DPI pre renderovanie elektronických dokumentov
     */
    public void setPdfDpi(int PdfDpi) {
        this.PdfDpi = PdfDpi;
        this.saveTempFile();
    }
}
