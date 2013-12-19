/*
 * MAPPSApp.java
 */

package mapps;

import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * hlavná trieda aplikácie
 * @author Matej Pazdič, Ľubomír Petrus
 */
public class MAPPSApp extends SingleFrameApplication {

    /**
     * pri štarte aplikácie zobrazí hlavné okno
     */
    @Override protected void startup() {

        try {
            InetAddress addr = InetAddress.getByName("www.google.com");
            show(new MAPPSView(this));
        } catch (UnknownHostException ex) {
             JOptionPane.showMessageDialog(null, "Error: Unable to find internet connection. Program will now exit.", "Error", JOptionPane.ERROR_MESSAGE);
             System.exit(0);
        }
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     * @param root
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of MAPPSApp
     */
    public static MAPPSApp getApplication() {
        return Application.getInstance(MAPPSApp.class);
    }

    /**
     * Main method launching the application.
     * @param args
     */
    public static void main(String[] args) {
        launch(MAPPSApp.class, args);
    }
}

