/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filefilters;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * Pomocná trieda určená k filtrácii súborov s príponou .tlv
 * @author Matej Pazdič
 */
public class TlvFilter extends FileFilter {

    @Override
    public boolean accept(File file) {
        if(file.isDirectory()){
            return true;
        }
        
        if(file.isFile()){
            if(file.getAbsolutePath().toLowerCase().endsWith(".tlv")){
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "*.tlv";
    }
    
}
