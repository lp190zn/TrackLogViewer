package players;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import mapps.MAPPSView;
import mapps.Presentator;
import net.sf.ghost4j.document.DocumentException;
import net.sf.ghost4j.document.PDFDocument;
import net.sf.ghost4j.renderer.RendererException;
import net.sf.ghost4j.renderer.SimpleRenderer;
import org.jdesktop.swingx.util.OS;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;
import usersettings.SettingsLoader;

/**
 * objekt na zobrazovanie elektronických dokumentov (pdf)
 * @author Ľubomír Petrus
 */
public class PDFViewer {

  private final JPanel contentPane;
  
  /**
   * hlavné okno zobrazovača
   */
  public static JFrame frame;
  private final Canvas canvas;

  private final MediaPlayerFactory mediaPlayerFactory;
  
  /**
   *  zobrazovač elektronických dokumentov
   */
  public static EmbeddedMediaPlayer mediaPlayer;
  private final CanvasVideoSurface videoSurface;
  
  /**
   * časovač dĺžky zobrazenia dokumentu prípadne jednej jeho strany
   */
  public static Timer timer;
 
  /**
   * premenná indikujúca, či bol už zobrazený aspoň jeden dokument
   */
  public static boolean WasPDFViewed = false;
  
  /**
   * premenná indikujúca momentálne zobrazenie dokumentu
   */
  public static boolean isPDFViewed;
  private static List<Image> images;
  private static int currentPage = 0;
  private Dimension dim;

  /**
   * konštruktor objektu PDFViewer
   */
  public PDFViewer() {

    System.setProperty("jna.library.path","." + System.getProperty("file.separator") + "VLC");
    System.setProperty("VLC_PLUGIN_PATH","." + System.getProperty("file.separator") + "VLC"
            + System.getProperty("file.separator") + "plugins");

    if (OS.isWindows()) {
        File ex = new File("gsdll32.dll");
        Runtime.getRuntime().load(ex.getAbsolutePath());
    }

    Toolkit toolkit =  Toolkit.getDefaultToolkit ();
    dim = toolkit.getScreenSize();

    canvas = new Canvas();
    canvas.setBackground(Color.black);


    contentPane = new JPanel();
    contentPane.setBackground(Color.black);
    contentPane.setLayout(new BorderLayout());
    contentPane.add(canvas, BorderLayout.CENTER);

 
    frame = new JFrame("PDF Viewer");
    frame.setContentPane(contentPane);

    frame.setSize((int)(dim.height / 1.5) - 100,dim.height-200);
    frame.setLocation(20,100);

    frame.setAlwaysOnTop(true);
    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

      if (OS.isWindows()) {
          mediaPlayerFactory = new MediaPlayerFactory(new String[]{"--no-video-title-show",
                      "--directx-use-sysmem"});
      } else {
          mediaPlayerFactory = new MediaPlayerFactory(new String[]{"--no-video-title-show"});
      }

    mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();

    videoSurface = mediaPlayerFactory.newVideoSurface(canvas);

    mediaPlayer.setVideoSurface(videoSurface);

    mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

            @Override
            public void playing(MediaPlayer mediaPlayer) {

                isPDFViewed = true;
            }
        });

    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        Presentator.secondMultimediaSearchTimer.restart();
        timer.cancel();
        MAPPSView.isPaused = false;
        isPDFViewed  = false;
      }
    });

  }


  /**
   * začiatok zobrazenia elektronického dokumentu
   * @param file - cesta k súboru na zobrazenie
   */
  public void start(String file) {
        try {

            int PdfDpi = SettingsLoader.getInstance().getPdfDpi();

            currentPage = 0;
            PDFDocument docum = new PDFDocument();
            //String str = new String(file.getBytes("UTF-8"), Charset.defaultCharset());
            
            docum.load(new File(file));
            SimpleRenderer sR = new SimpleRenderer();

            sR.setResolution(PdfDpi);
            images = sR.render(docum);

            File pdf = new File("1.jpg");

            ImageIO.write((RenderedImage) images.get(0), "jpg", pdf);

            int time = (int) ((SettingsLoader.getInstance().getDocumentsShowingLength()*1000) + 3000);

            timer = new Timer();
            timer.scheduleAtFixedRate(new RemindTask(), time, time);

            WasPDFViewed = true;

            frame.setVisible(true);

            String correctpath = new String (pdf.getAbsolutePath().getBytes("UTF-8"), Charset.defaultCharset());

            mediaPlayer.playMedia(correctpath);


        } catch (RendererException ex) {
            Logger.getLogger(PDFViewer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DocumentException ex) {
            Logger.getLogger(PDFViewer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(frame, "ERROR: Cannot load file at path. Please parse gps file again ", "ERROR", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            Logger.getLogger(PDFViewer.class.getName()).log(Level.SEVERE, null, ex);
        }
            
}


  /**
   * skrytie okna zobrazovača
   */
  public static void setVisibleFalse() {
      frame.setVisible(false);
  }

  /**
   * opätovné spustenie časovača dĺžky zobrazenia dokumentu
   */
  public static void restartTimer() {
      timer = new Timer();
      int time = (int) ((SettingsLoader.getInstance().getPictureShowingLength()*1000)  + 3000);
      timer.scheduleAtFixedRate(new RemindTask(), time, time);
  }

 static class RemindTask extends TimerTask {

        public void run() {

            if (images.size() > currentPage+1) {
                try {
                    currentPage++;
                    File pdf = new File("1.jpg");
                    ImageIO.write((RenderedImage) images.get(currentPage), "jpg", pdf);

                    String correctpath = new String (pdf.getAbsolutePath().getBytes("UTF-8"), Charset.defaultCharset());
                    mediaPlayer.playMedia(correctpath);
                } catch (IOException ex) {
                    Logger.getLogger(PDFViewer.class.getName()).log(Level.SEVERE, null, ex);
                }


            } else {

                timer.cancel();
                isPDFViewed = false;
                Presentator.secondMultimediaSearchTimer.restart();
                if (Presentator.isClosingWindows == true) {
                    frame.setVisible(false);
                }

            }
        }
    }
}
