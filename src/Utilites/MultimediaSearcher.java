/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilites;

import datadef.FileImpl;
import datadef.TrackPointImpl;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.tools.ant.DirectoryScanner;
import org.jdesktop.swingx.util.OS;

/**
 * Trieda určená na vyhľadávanie relevantných multimediálnych súborov
 * @author Matej Pazdič
 */
public class MultimediaSearcher {

    private String searchFilePath;
    private String searchFolder;
    private ArrayList<TrackPointImpl> track;

    /**
     * Základný konštruktor triedy MultimediaSearcher
     */
    public MultimediaSearcher() {
        searchFilePath = null;
        searchFolder = null;
        track = null;
    }

    /**
     * Preťažený konštruktor triedy MultimediaSearcher
     * @param searchFilePath - cesta ku súboru so zánamami GPS
     * @param searchFolder - koreň adresárovej štruktúry od ktorého sa vyhľadávajú multimediálne súbory
     */
    public MultimediaSearcher(String searchFilePath, String searchFolder) {
        this.searchFilePath = searchFilePath;
        this.searchFolder = searchFolder;
        track = null;
    }

    /**
     * Preťažený konštruktor triedy MultimediaSearcher
     * @param searchFilePath - cesta ku súboru so záznamami GPS
     * @param searchFolder - koreň adresárovej štruktúry od ktorého sa vyhľadávajú multimediálne súbory
     * @param track - štruktúra v ktorej sú uložené jednotlivé načítané GPS body
     */
    public MultimediaSearcher(String searchFilePath, String searchFolder, ArrayList<TrackPointImpl> track) {
        this.searchFilePath = searchFilePath;
        this.searchFolder = searchFolder;
        this.track = track;
    }

    /**
     * @return Vracia cestu ku súboru so záznamami GPS
     */
    public String getSearchFilePath() {
        return searchFilePath;
    }

    /**
     * @param searchFilePath - cesta ku súboru so záznamami GPS
     */
    public void setSearchFilePath(String searchFilePath) {
        this.searchFilePath = searchFilePath;
    }

    /**
     * @return Vracia koreňový adresár vyhľadavácej štruktúry
     */
    public String getSearchFolder() {
        return searchFolder;
    }

    /**
     * @param searchFolder - koreň adresárovej štruktúry od ktorého sa vyhľadávajú multimediálne súbory
     */
    public void setSearchFolder(String searchFolder) {
        this.searchFolder = searchFolder;
    }
    
    

    /**
     * Metóda určená na vyhľadávanie relevantných multimediálnych súborov
     * @return Vracia zoznam relevantných multimediálnych súborov
     */
    public ArrayList<FileImpl> startSearch() {

        ArrayList<FileImpl> files = new ArrayList<FileImpl>();
        String os = System.getProperty("os.name");

        files.clear();
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setFollowSymlinks(false);

        if (OS.isLinux()) {
            String str1 = "**/*.jpg";
            String str2 = "**/*.jpeg";
            String str3 = "**/*.avi";
            String str4 = "**/*.mov";
            String str5 = "**/*.mp4";
            String str6 = "**/*.3gp";
            String str7 = "**/*.mp3";
            String str8 = "**/*.wav";
            String str9 = "**/*.amr";
            String str10 = "**/*.txt";
            String str11 = "**/*.pdf";
            scanner.setIncludes(new String[]{str1, str2, str3, str4, str5, str6, str7, str8, str9, str10, str11});
            scanner.setBasedir(this.searchFolder);
            scanner.setCaseSensitive(false);
            scanner.scan();
            String[] tempFiles = scanner.getIncludedFiles();

            for (int i = 0; i < tempFiles.length; i++) {
                FileImpl fileimpl = new FileImpl();
                String temp = null;
                if (!this.searchFolder.endsWith("/")) {
                    temp = scanner.getBasedir() + "/" + tempFiles[i];
                } else {
                    temp = scanner.getBasedir() + tempFiles[i];
                }
                Date first = new Date(track.get(0).getTime().getTime());
                first.setSeconds(first.getSeconds() - 1);
                Date last = new Date(track.get(track.size() - 1).getTime().getTime());
                last.setSeconds(last.getSeconds() + 1);
                if (track != null) {
                    File file = new File(temp);
                    if (temp.toLowerCase().endsWith(".jpg") || temp.toLowerCase().endsWith(".jpeg")) {
                        IImageMetadata metadata = null;
                        try {
                            metadata = Sanselan.getMetadata(file);
                        } catch (ImageReadException e) {
                            System.out.println("ERROR: Cannot read EXIF metadata with Sanselan!!!");
                        } catch (IOException e) {
                            System.out.println("ERROR: Cannot read EXIF metadata with Sanselan!!!");
                        }
                        if (metadata instanceof JpegImageMetadata) {
                            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                            TiffField createDateField = jpegMetadata.findEXIFValue(TiffConstants.EXIF_TAG_CREATE_DATE);

                            if (createDateField == null) {
                                fileimpl.setDate(new Date(file.lastModified()));
                            } else {
                                try {
                                    String createDateStr = createDateField.getValueDescription();
                                    createDateStr = createDateStr.substring(createDateStr.indexOf("'") + 1, createDateStr.lastIndexOf("'"));
                                    DateFormat dateForm = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                                    Date date = (Date) dateForm.parse(createDateStr);
                                    fileimpl.setDate(date);
                                } catch (ParseException ex) {
                                    System.out.println("ERROR: Cannot parse creation date from picture!!!");
                                }
                            }
                            TiffImageMetadata exifMetadata = jpegMetadata.getExif();
                            if (exifMetadata != null) {
                                try {
                                    TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
                                    if (gpsInfo != null) {
                                        fileimpl.setLatitude(String.valueOf(gpsInfo.getLatitudeAsDegreesNorth()));
                                        fileimpl.setLongitude(String.valueOf(gpsInfo.getLongitudeAsDegreesEast()));
                                    }
                                } catch (ImageReadException ex) {
                                    System.out.println("ERROR: Cannot read GPS metadata from jpeg EXIF!!! Using no coordinates!!!");
                                }
                            }
                        } else {
                            fileimpl.setDate(new Date(file.lastModified()));
                        }
                    } else {
                        fileimpl.setDate(new Date(file.lastModified()));
                    }
                    fileimpl.setPath(temp);

                    ArrayList<FileImpl> goodFiles = new ArrayList<FileImpl>();
                    for (int m = 0; m < files.size(); m++) {
                        Date fileDate = files.get(m).getDate();
                        for (int j = 1; j < track.size(); j++) {
                            Date prevTrackPointDate = track.get(j - 1).getTime();
                            prevTrackPointDate.setSeconds(track.get(j - 1).getTime().getSeconds() - 1);
                            Date nextTrackPointDate = track.get(j).getTime();
                            nextTrackPointDate.setSeconds(track.get(j).getTime().getSeconds() + 1);
                            if (files.get(m).getLongitude() != null && files.get(m).getLatitude() != null) {
                                if ((fileDate.after(prevTrackPointDate) && fileDate.before(nextTrackPointDate)) || (fileDate.equals(prevTrackPointDate) || (fileDate.equals(nextTrackPointDate)))) {
                                    double deltaLat1 = Math.abs(Double.parseDouble(files.get(m).getLatitude()) - track.get(j - 1).getLatitude());
                                    double deltaLon1 = Math.abs(Double.parseDouble(files.get(m).getLongitude()) - track.get(j - 1).getLongitude());
                                    double deltaLat2 = Math.abs(Double.parseDouble(files.get(m).getLatitude()) - track.get(j).getLatitude());
                                    double deltaLon2 = Math.abs(Double.parseDouble(files.get(m).getLongitude()) - track.get(j).getLongitude());

                                    if ((deltaLat1 <= 0.0007 && deltaLon1 <= 0.0007) || (deltaLat2 <= 0.0007 && deltaLon2 <= 0.0007)) {
                                        System.out.println(m + ". Obrazok ma dobru GPS, k bodu " + (j - 1) + "!!!");
                                        goodFiles.add(files.get(m));
                                        break;
                                    }
                                }
                            } else {
                                if ((fileDate.after(prevTrackPointDate) && fileDate.before(nextTrackPointDate))) {
                                    goodFiles.add(files.get(m));
                                    break;
                                }
                            }
                        }
                    }

                    return goodFiles;
                }
            }
        }

        if (OS.isWindows()) {
            String str1 = "**" + System.getProperty("file.separator") + "*.jpg";
            String str2 = "**" + System.getProperty("file.separator") + "*.jpeg";
            String str3 = "**\\*.avi";
            String str4 = "**\\*.mov";
            String str5 = "**\\*.mp4";
            String str6 = "**\\*.3gp";
            String str7 = "**\\*.mp3";
            String str8 = "**\\*.wav";
            String str9 = "**\\*.amr";
            String str10 = "**\\*.txt";
            String str11 = "**\\*.pdf";
            scanner.setIncludes(new String[]{str1, str2, str3, str4, str5, str6, str7, str8, str9, str10, str11});
            File f = new File(this.getSearchFolder());
            scanner.setBasedir(f);
            scanner.setCaseSensitive(false);
            scanner.scan();
            String[] tempFiles = scanner.getIncludedFiles();

            System.out.println(tempFiles.length);
            Date first = track.get(0).getTime();
            first.setSeconds(first.getSeconds() - 1);
            Date last = track.get(track.size() - 1).getTime();
            last.setSeconds(last.getSeconds() + 1);
            for (int i = 0; i < tempFiles.length; i++) {
                FileImpl fileimpl = new FileImpl();
                String temp = scanner.getBasedir() + "\\" + tempFiles[i];
                if (track != null) {
                    File file = new File(temp);
                    if (temp.toLowerCase().endsWith(".jpg") || temp.toLowerCase().endsWith(".jpeg")) {
                        IImageMetadata metadata = null;
                        try {
                            metadata = Sanselan.getMetadata(file);
                        } catch (ImageReadException e) {
                            System.out.println("ERROR: Cannot read EXIF metadata with Sanselan!!!");
                        } catch (IOException e) {
                            System.out.println("ERROR: Cannot read EXIF metadata with Sanselan!!!");
                        }
                        if (metadata instanceof JpegImageMetadata) {
                            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                            TiffField createDateField = jpegMetadata.findEXIFValue(TiffConstants.EXIF_TAG_CREATE_DATE);

                            if (createDateField == null) {
                                fileimpl.setDate(new Date(file.lastModified()));
                            } else {
                                try {
                                    String createDateStr = createDateField.getValueDescription();
                                    createDateStr = createDateStr.substring(createDateStr.indexOf("'") + 1, createDateStr.lastIndexOf("'"));
                                    DateFormat dateForm = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                                    Date date = (Date) dateForm.parse(createDateStr);
                                    fileimpl.setDate(date);
                                } catch (ParseException ex) {
                                    System.out.println("ERROR: Cannot parse creation date from picture!!!");
                                }
                            }
                            TiffImageMetadata exifMetadata = jpegMetadata.getExif();
                            if (exifMetadata != null) {
                                try {
                                    TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
                                    if (gpsInfo != null) {
                                        fileimpl.setLatitude(String.valueOf(gpsInfo.getLatitudeAsDegreesNorth()));
                                        fileimpl.setLongitude(String.valueOf(gpsInfo.getLongitudeAsDegreesEast()));
                                    }
                                } catch (ImageReadException ex) {
                                    System.out.println("ERROR: Cannot read GPS metadata from jpeg EXIF!!! Using no coordinates!!!");
                                }
                            }
                        } else {
                            fileimpl.setDate(new Date(file.lastModified()));
                        }
                    } else {
                        fileimpl.setDate(new Date(file.lastModified()));
                    }
                    fileimpl.setPath(temp);
                    if (fileimpl.getDate().after(first) && fileimpl.getDate().before(last)) {
                        if (temp.substring(0, 4).lastIndexOf("\\") != temp.substring(0, 4).indexOf("\\")) {
                            temp = scanner.getBasedir() + tempFiles[i];
                            fileimpl.setPath(temp);
                        }
                            files.add(fileimpl);
                    }
                }
            }
        }
        
        ArrayList<FileImpl> goodFiles = new ArrayList<FileImpl>();
        for (int i = 0; i < files.size(); i++) {
            Date fileDate = files.get(i).getDate();
            for (int j = 1; j < track.size(); j++) {
                Date prevTrackPointDate = track.get(j - 1).getTime();
                prevTrackPointDate.setSeconds(track.get(j - 1).getTime().getSeconds() - 1);
                Date nextTrackPointDate = track.get(j).getTime();
                nextTrackPointDate.setSeconds(track.get(j).getTime().getSeconds() + 1);
                if (files.get(i).getLongitude() != null && files.get(i).getLatitude() != null) {
                    if ((fileDate.after(prevTrackPointDate) && fileDate.before(nextTrackPointDate)) || (fileDate.equals(prevTrackPointDate) || (fileDate.equals(nextTrackPointDate)))) {
                        double deltaLat1 = Math.abs(Double.parseDouble(files.get(i).getLatitude()) - track.get(j - 1).getLatitude());
                        double deltaLon1 = Math.abs(Double.parseDouble(files.get(i).getLongitude()) - track.get(j - 1).getLongitude());
                        double deltaLat2 = Math.abs(Double.parseDouble(files.get(i).getLatitude()) - track.get(j).getLatitude());
                        double deltaLon2 = Math.abs(Double.parseDouble(files.get(i).getLongitude()) - track.get(j).getLongitude());

                        if ((deltaLat1 <= 0.0007 && deltaLon1 <= 0.0007) || (deltaLat2 <= 0.0007 && deltaLon2 <= 0.0007)) {
                            goodFiles.add(files.get(i));
                            break;
                        }
                    }
                } else {
                    if ((fileDate.after(prevTrackPointDate) && fileDate.before(nextTrackPointDate))) {
                        goodFiles.add(files.get(i));
                        break;
                    }
                }
            }
        }
        
        return goodFiles;
    }

    /**
     * @param track - Zoznam jednotlivých trackpointov danej terasy
     */
    public void setTrackPoints(ArrayList<TrackPointImpl> track) {
        this.track = track;
    }
}
