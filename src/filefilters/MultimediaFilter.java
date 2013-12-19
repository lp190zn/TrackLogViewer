/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filefilters;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * Pomocná trieda určená k filtrácii multimediálnych súborov
 * @author Matej Pazdič
 */
public class MultimediaFilter extends FileFilter {

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }

        if (file.isFile()) {
            String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("mpg") || ext.equalsIgnoreCase("avi") || ext.equalsIgnoreCase("mov") || ext.equalsIgnoreCase("mp4") || ext.equalsIgnoreCase("3gp") || ext.equalsIgnoreCase("mp3") || ext.equalsIgnoreCase("wav") || ext.equalsIgnoreCase("amr") || ext.equalsIgnoreCase("txt") || ext.equalsIgnoreCase("pdf")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Multimedia files";
    }
}
