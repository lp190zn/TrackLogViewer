/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package players;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import mapps.MAPPSView;
import mapps.Presentator;
import org.jdesktop.swingx.util.OS;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;
import usersettings.SettingsLoader;

/**
 * objekt na zobrazovanie fotografií
 * @author Ľubomír Petrus
 */
public class PictureViewer {

  private final JPanel contentPane, controlsPane;
  
  /**
   * hlavné okno zobrazovača
   */
  public static JFrame frame;
  private final Canvas canvas;

  private final MediaPlayerFactory mediaPlayerFactory;
  
  /**
   * zobrazovač fotografií
   */
  public static EmbeddedMediaPlayer mediaPlayer;
  private final CanvasVideoSurface videoSurface;
  
  /**
   * časovač dĺžky zobrazenia fotografií
   */
  public static Timer timer;
  
  /**
   * premenná indikujúca, či bola už zobrazená aspoň jedna fotografia
   */
  public static boolean WasPictureViewed = false;
  
  /**
   * premenná indikujúca momentálne zobrazenie fotografie
   */
  public static boolean isPViewed;


  /**
   * konštruktor objektu PictureViewer
   */
  public PictureViewer() {

    System.setProperty("jna.library.path","." + System.getProperty("file.separator") + "VLC");
    System.setProperty("VLC_PLUGIN_PATH","." + System.getProperty("file.separator") + "VLC"
            + System.getProperty("file.separator") + "plugins");

    Toolkit toolkit =  Toolkit.getDefaultToolkit ();
    Dimension dim = toolkit.getScreenSize();

    canvas = new Canvas();
    canvas.setBackground(Color.black);

    contentPane = new JPanel();
    contentPane.setBackground(Color.black);
    contentPane.setLayout(new BorderLayout());
    contentPane.add(canvas, BorderLayout.CENTER);

    controlsPane = new JPanel();
    controlsPane.setBorder(new EmptyBorder(8, 8, 8, 8));
    controlsPane.setLayout(new BoxLayout(controlsPane, BoxLayout.X_AXIS));

    frame = new JFrame("Picture Viewer");
    frame.setContentPane(contentPane);

    frame.setSize(dim.width/4+30,dim.height/3+30);
    frame.setLocation(dim.width - ((dim.width/4)+50), 100);
    
    frame.setAlwaysOnTop(true);
    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    
      if (OS.isWindows()) {
          mediaPlayerFactory = new MediaPlayerFactory(new String[]{"--no-video-title-show",
                      "--directx-use-sysmem", "--quiet", "--no-directx-3buffering"});
      } else {
          mediaPlayerFactory = new MediaPlayerFactory(new String[]{"--no-video-title-show"});
      }

    mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();

    videoSurface = mediaPlayerFactory.newVideoSurface(canvas);

    mediaPlayer.setVideoSurface(videoSurface);

    mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

            @Override
            public void playing(MediaPlayer mediaPlayer) {
                int time = (int) (SettingsLoader.getInstance().getPictureShowingLength()*1000);
                timer.schedule(new RemindTask(), time);
                isPViewed = true;
            }
        });


    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        Presentator.secondMultimediaSearchTimer.restart();
        timer.cancel();
        MAPPSView.isPaused = false;
        mediaPlayer.stop();
        isPViewed  = false;        
      }
    });

    frame.setVisible(false);
  }

  /**
   * začiatok zobrazenia fotografie
   * @param file - cesta k súboru na zobrazenie
   */
  public void start(String file) {


        timer = new Timer();
        try {
            WasPictureViewed = true;

            String str = new String(file.getBytes("UTF-8"), Charset.defaultCharset());

            frame.setVisible(true);
            mediaPlayer.playMedia(str);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(PictureViewer.class.getName()).log(Level.SEVERE, null, ex);
        }

}


  /**
   * skrytie okna zobrazovača
   */
  public static void setVisibleFalse() {
      frame.setVisible(false);
  }

  /**
   * opätovné spustenie časovača dĺžky zobrazenia fotografie
   */
  public static void restartTimer() {
      timer = new Timer();
      int time = (int) (SettingsLoader.getInstance().getPictureShowingLength()*1000);
      timer.schedule(new RemindTask(), time);
  }

 static class RemindTask extends TimerTask {

        public void run() {

            timer.cancel();

            if (Presentator.isClosingWindows == true) {
                    PictureViewer.frame.setVisible(false);
                }

            if (Presentator.showLastPicture == false) {
            mediaPlayer.stop(); }

            isPViewed = false;

            Presentator.secondMultimediaSearchTimer.restart();

        }
    }

}
