/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datadef;

import java.util.Date;

/**
 * Pomocná trieda určená na reprezentáciu GPS záznamu tzv. trackpointu
 * @author Matej Pazdič
 */
public class TrackPointImpl {
    private double latitude;
    private double longitude;
    private int deviceElevation;
    private int internetElevation;
    private Date time;
    
    /**
     * Základný konštruktor triedy TrackPointImpl
     */
    public TrackPointImpl(){
        
    }
    
    /**
     * Preťažený konštruktor triedy TrackPointImpl
     * @param latitude - Zemepisná šírka
     * @param longitude - Zemepisná dĺžka
     */
    public TrackPointImpl(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    /**
     * Preťažený konštruktor triedy TrackPointImpl
     * @param latitude - Zemepisná šírka
     * @param longitude - Zemepisná dĺžka
     * @param deviceElevation - Nadmorská výška získaná z GPS zariadenia
     */
    public TrackPointImpl(double latitude, double longitude, int deviceElevation){
        this.latitude = latitude;
        this.longitude = longitude;
        this.deviceElevation = deviceElevation;
    }
    
    /**
     * Preťažený konštruktor triedy TrackPointImpl
     * @param latitude - Zemepisná šírka
     * @param longitude - Zemepisná dĺžka
     * @param deviceElevation - Nadmorská výška získaná z GPS zariadenia
     * @param time - Dátum a čas vytvorenia daného trackpointu
     */
    public TrackPointImpl(double latitude, double longitude, int deviceElevation, Date time){
        this.latitude = latitude;
        this.longitude = longitude;
        this.deviceElevation = deviceElevation;
        this.time = time;
    }
    
    /**
     * Preťažený konštruktor triedy TrackPointImpl
     * @param latitude - Zemepisná šírka
     * @param longitude - Zemepisná dĺžka
     * @param deviceElevation - Nadmorská výška získaná z GPS zariadenia
     * @param internetElevation - Nadmorská výška získaná z mapového servera
     */
    public TrackPointImpl(double latitude, double longitude, int deviceElevation, int internetElevation){
        this.latitude = latitude;
        this.longitude = longitude;
        this.deviceElevation = deviceElevation;
        this.internetElevation = internetElevation;
    }
    
    /**
     * Preťažený konštruktor triedy TrackPointImpl
     * @param latitude - Zemepisná šírka
     * @param longitude - Zemepisná dĺžka
     * @param deviceElevation - Nadmorská výška získaná z GPS zariadenia
     * @param internetElevation - Nadmorská výška získaná z mapového servera
     * @param time - Dátum a čas vytvorenia daného trackpointu
     */
    public TrackPointImpl(double latitude, double longitude, int deviceElevation, int internetElevation, Date time){
        this.latitude = latitude;
        this.longitude = longitude;
        this.deviceElevation = deviceElevation;
        this.internetElevation = internetElevation;
        this.time = time;
    }

    /**
     * @return Vracia zemepisnú šírku daného trackpointu
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude - Zemepisná šírka
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return Vracia zemepisnú dĺžku daného trackpointu
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude - Zemepisná dĺžka
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return Vracia nadmorskú výšku daného trackpointu získanú z GPS zariadenia
     */
    public int getDeviceElevation() {
        return deviceElevation;
    }

    /**
     * @param deviceElevation - Nadmorská výška získaná z GPS zariadenia
     */
    public void setDeviceElevation(int deviceElevation) {
        this.deviceElevation = deviceElevation;
    }

    /**
     * @return Vracia nadmorskú výšku daného trackpointu získanú z mapového servera
     */
    public int getInternetElevation() {
        return internetElevation;
    }

    /**
     * @param internetElevation  - Nadmorská výška získaná z mapového servera
     */
    public void setInternetElevation(int internetElevation) {
        this.internetElevation = internetElevation;
    }

    /**
     * @return Vracia čas vytvorenia daného trackpointu
     */
    public Date getTime() {
        return time;
    }

    /**
     * @param time - Čas vytvorenia daného trackpointu
     */
    public void setTime(Date time) {
        this.time = time;
    }
    
}
