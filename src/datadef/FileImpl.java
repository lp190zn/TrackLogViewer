/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datadef;

import java.util.Date;

/**
 * Pomocná trieda, ktorá definuje multimediálny súbor a údaje ktoré k nemu priliehajú
 * @author Matej Pazdič
 */
public class FileImpl {
    
    private String path;
    private Date date;
    private String latitude;
    private String longitude;
    private String elevation;
    private int trackPointIndex;
    
    /**
     * Základný konštruktor triedy FileImpl
     */
    public FileImpl(){
        this.path = null;
        this.date = null;
        this.latitude = null;
        this.longitude = null;
        this.elevation = null;
        this.trackPointIndex = -1;
    }
    
    /**
     * Preťažený konštruktor triedy FileImpl
     * @param path - Cesta k danému súboru
     * @param date -Dátum vytvorenia daného súboru
     */
    public FileImpl(String path, Date date){
        this.path = path;
        this.date = date;
        this.latitude = null;
        this.longitude = null;
        this.elevation = null;
        this.trackPointIndex = -1;
    }
    
    /**
     * Preťažený konštruktor triedy FileImpl
     * @param path - Cesta k danému súboru
     * @param date -Dátum vytvorenia daného súboru
     * @param latitude - Zemepisná šírka, ktorá prilieha k danému súboru
     * @param longitude - Zemepisná dĺžka, ktorá prilieha k danému súboru
     */
    public FileImpl(String path, Date date, String latitude, String longitude){
        this.path = path;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = null;
        this.trackPointIndex = -1;
    }
    
    /**
     * Preťažený konštruktor triedy FileImpl
     * @param path - Cesta k danému súboru
     * @param date -Dátum vytvorenia daného súboru
     * @param latitude - Zemepisná šírka, ktorá prilieha k danému súboru
     * @param longitude - Zemepisná dĺžka, ktorá prilieha k danému súboru
     * @param elevation - Nadmorská výška, ktorá prilieha k danému súboru
     */
    public FileImpl(String path, Date date, String latitude, String longitude, String elevation){
        this.path = path;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.trackPointIndex = -1;
    }

    /**
     * @return Vracia cestu k danému súboru
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path - Cesta k danému súboru
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return Vracia dátum vytvorenia aktuálneho súboru
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date - Dátum vytvorenia daného súboru
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return Vracia zemepisnú šírku, ktorá prilieha k danému súboru
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * @param latitude - Zemepisná širka, ktorá prilieha k danému súboru
     */
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    /**
     * @return Vracia zemepisnú dĺžku, ktorá prilieha k danému súboru
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     * @param longitude - Zemepisná dĺžka, ktorá prilieha k danému súboru
     */
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /**
     * @return Vracia nadmorskú výšku, ktorá prilieha k danému súboru
     */
    public String getElevation() {
        return elevation;
    }

    /**
     * @param elevation - Nadmorská výška, ktorá prilieha k danému súboru
     */
    public void setElevation(String elevation) {
        this.elevation = elevation;
    }

    /**
     * @return Vracia index trackpointu, ku ktorému daný súbor patrí
     */
    public int getTrackPointIndex() {
        return trackPointIndex;
    }

    /**
     * @param trackPointIndex - Index trackpointu, ku ktorému daný súbor patrí
     */
    public void setTrackPointIndex(int trackPointIndex) {
        this.trackPointIndex = trackPointIndex;
    }
    
}
