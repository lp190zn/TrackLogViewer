/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parsers.utilities;

import datadef.TrackPointImpl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import parsers.G7tParser;
import parsers.GpxParser;
import parsers.KmlParser;

/**
 * Trieda určená na načítavanie nadmorských výšok z mapového servera
 * @author Matej Pazdič
 */
public class ElevationLoaderMapQuest extends Thread {

    private final String baseUrlString = "http://open.mapquestapi.com/elevation/v1/getElevationProfile?shapeFormat=raw&latLngCollection=";

    /**
     * Základný konštruktor triedy ElevationLoaderMapQuest
     */
    public ElevationLoaderMapQuest() {
    }

    /**
     * Metóda určená na samotné načítavanie viacerých nadmorských výšok
     * @param elev - Štruktúra v ktorej sú uložené GPS údaje zo zaridenia
     * @return Vracia zoznam načítaných nadmorských výšok
     */
    public ArrayList<String> reclaimElevations(ArrayList<TrackPointImpl> elev) {


        ArrayList<String> serverElevations = new ArrayList<String>();


        for (int i = 0; i < elev.size(); i++) {

            GpxParser.jProgressBar1.setMaximum(elev.size() - 1);
            GpxParser.jProgressBar1.setValue(i);
            G7tParser.jProgressBar1.setMaximum(elev.size() - 1);
            G7tParser.jProgressBar1.setValue(i);

            try {
                URL url = new URL(baseUrlString + elev.get(i).getLatitude() + "," + elev.get(i).getLongitude());
                URLConnection connection = url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String replyString = bufferedReader.readLine();
                bufferedReader.close();

                String elevation = replyString.substring(replyString.indexOf("height") + 8, replyString.indexOf("info") - 4);
                try {
                    int b = Integer.parseInt(elevation);
                } catch (NumberFormatException ex) {
                    if(i > 0){
                        elevation = serverElevations.get(i-1);
                    }else{
                        elevation = "0";
                    }

                }
                serverElevations.add(elevation);

            } catch (IOException ex) {
                System.out.println("Error: In trackpoint " + i + " using GPS device elevation!!!");
                serverElevations.add(String.valueOf(elev.get(i).getDeviceElevation()));
            }
        }

        return serverElevations;
    }

    /**
     * Metóda určená na samotné načítavanie viacerých nadmorských výšok
     * @param lats - Zoznam zemepisných šírok
     * @param longs - Zoznam zemepisných dĺžok
     * @return Vracia zoznam načítaných nadmorských výšok
     */
    public ArrayList<String> reclaimElevations(ArrayList<String> lats, ArrayList<String> longs) {
        ArrayList<String> serverElevations = new ArrayList<String>();

        if (lats.size() == longs.size()) {
            for (int i = 0; i < lats.size(); i++) {

                KmlParser.jProgressBar1.setMaximum(lats.size() - 1);
                KmlParser.jProgressBar1.setValue(i);

                try {
                    URL url = new URL(baseUrlString + lats.get(i) + "," + longs.get(i));
                    URLConnection connection = url.openConnection();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String replyString = bufferedReader.readLine();
                    bufferedReader.close();

                    String elevation = replyString.substring(replyString.indexOf("height") + 8, replyString.indexOf("info") - 4);
                    try {
                        int b = Integer.parseInt(elevation);
                    } catch (NumberFormatException ex) {
                        if (i > 0) {
                            elevation = serverElevations.get(i - 1);
                        } else {
                            elevation = "0";
                        }

                    }
                    serverElevations.add(elevation);

                } catch (IOException ex) {
                    System.out.println("Error: In trackpoint " + i + " using GPS device elevation!!!");
                    return null;
                }
            }

            return serverElevations;
        } else {
            return null;
        }
    }

    /**
     * Metóda na načítanie jednej nadmorskej výšky z mapového servera
     * @param lat - Zemepisná šírka
     * @param lon - Zemepisná dĺžka
     * @return Vracia získanú nadmorskú výšku
     */
    public int reclaimElevation(double lat, double lon) {
        try {
            URL url = new URL(baseUrlString + lat + "," + lon);
            URLConnection connection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String replyString = bufferedReader.readLine();
            bufferedReader.close();

            String elevation = replyString.substring(replyString.indexOf("height") + 8, replyString.indexOf("info") - 4);
            return Integer.parseInt(elevation);
        } catch (IOException ex) {
            System.out.println("ERROR: Cannot resolve elevation from MapQuest server, check your internet connection!!!");
            return -1;
        }
    }
}
