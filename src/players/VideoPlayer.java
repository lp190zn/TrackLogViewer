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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mapps.MAPPSView;
import mapps.Presentator;
import uk.co.caprica.vlcj.binding.LibVlcConst;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;

/**
 * objekt na prehrávanie zvukových záznamov
 * @author Ľubomír Petrus
 */
public class VideoPlayer {

  private final JButton playButton,pauseButton,exitButton;
  private JSlider volumeSlider;
  private final JPanel contentPane, controlsPane;
  
  /**
   * hlavné okno prehrávača
   */
  public static JFrame frame;
  private final Canvas canvas;
  private final MediaPlayerFactory mediaPlayerFactory;
  
  /**
   * prehrávač videa
   */
  public static EmbeddedMediaPlayer mediaPlayer;
  private final CanvasVideoSurface videoSurface;
  private String subor;
  
  /**
   * premenná indikujúca momentálne prehrávanie video súboru
   */
  public static boolean isVPlaying;

  /**
   * konštruktor objektu VideoPlayer
   */
  public VideoPlayer() {

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

        volumeSlider = new JSlider();
        volumeSlider.setOrientation(JSlider.HORIZONTAL);
        volumeSlider.setMinimum(LibVlcConst.MIN_VOLUME);
        volumeSlider.setMaximum(30);
        volumeSlider.setPreferredSize(new Dimension(100, 30));
        volumeSlider.setMinimumSize(new Dimension (100,30));
        volumeSlider.setMaximumSize(new Dimension (100,30));
        volumeSlider.setToolTipText("Change volume");
        volumeSlider.setValue(15);

        volumeSlider.addChangeListener(new ChangeListener() {

          @Override
          public void stateChanged(ChangeEvent e) {
              JSlider source = (JSlider) e.getSource();
        if(!source.getValueIsAdjusting()) {
              mediaPlayer.setVolume(source.getValue());
        }
          }
      });

        ImageIcon play = new ImageIcon(getClass().getResource("resources/25Graphic3.png"));

        playButton = new JButton ("",play);
        playButton.setMinimumSize(new Dimension (25,(25)));
        playButton.setMaximumSize(new Dimension (25,(25)));
        playButton.setPreferredSize(new Dimension (25,(25)));


        playButton.setMnemonic('f');
        playButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (subor != null) {
                start(subor);
            }}
        });

        ImageIcon pause = new ImageIcon(getClass().getResource("resources/25Graphic1.png"));

        pauseButton = new JButton ("",pause);
        pauseButton.setMinimumSize(new Dimension (25,(25)));
        pauseButton.setMaximumSize(new Dimension (25,(25)));
        pauseButton.setPreferredSize(new Dimension (25,(25)));


        pauseButton.setMnemonic('p');
        pauseButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayer.pause();
            }
        });


        ImageIcon exit = new ImageIcon(getClass().getResource("resources/25Graphic2.png"));

        exitButton = new JButton("",exit);
        exitButton.setMinimumSize(new Dimension (25,(25)));
        exitButton.setMaximumSize(new Dimension (25,(25)));
        exitButton.setPreferredSize(new Dimension (25,(25)));


        exitButton.setMnemonic('e');
        exitButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (VideoPlayer.isVPlaying == true) {

                Presentator.secondMultimediaSearchTimer.restart();
                MAPPSView.isPaused = false;
                VideoPlayer.mediaPlayer.stop();
                VideoPlayer.isVPlaying = false;

                if (Presentator.isClosingWindows == true) {
                    VideoPlayer.frame.setVisible(false);
                }
                }
            }
        });


    ImageIcon vollo = new ImageIcon(getClass().getResource("resources/25Graphic4.png"));
    ImageIcon volhi = new ImageIcon(getClass().getResource("resources/25Graphic5.png"));

    JLabel label = new JLabel(volhi);
    label.setHorizontalAlignment(SwingConstants.LEFT);

    controlsPane.add(playButton);
    controlsPane.add(Box.createHorizontalStrut(5));
    controlsPane.add(pauseButton);
    controlsPane.add(Box.createHorizontalStrut(5));
    controlsPane.add(exitButton);
    controlsPane.add(Box.createHorizontalStrut(5));
    controlsPane.add(new JLabel(vollo));
    controlsPane.add(volumeSlider);
    controlsPane.add(Box.createHorizontalStrut(5));
    controlsPane.add(label);
    controlsPane.add(Box.createHorizontalStrut(0));
    contentPane.add(controlsPane, BorderLayout.SOUTH);


    frame = new JFrame("Video Player");
    frame.setContentPane(contentPane);

    frame.setSize(dim.width/4+30,dim.height/3+60);
    frame.setLocation(dim.width - ((dim.width/4)+50), 80 + (dim.height /3+60));
    
    frame.setAlwaysOnTop(true);
    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    mediaPlayerFactory = new MediaPlayerFactory(new String[] {"--no-video-title-show", "--quiet"});

    mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
    videoSurface = mediaPlayerFactory.newVideoSurface(canvas);

    mediaPlayer.setVideoSurface(videoSurface);

    mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                isVPlaying = false;
                Presentator.secondMultimediaSearchTimer.restart();

                if (Presentator.isClosingWindows == true) {
                    VideoPlayer.frame.setVisible(false);
                }

            }
        });

        mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

            @Override
            public void playing(MediaPlayer mediaPlayer) {
               mediaPlayer.setVolume(15);
            }
        });

      frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        Presentator.secondMultimediaSearchTimer.restart();
        MAPPSView.isPaused = false;
        mediaPlayer.stop();
        VideoPlayer.isVPlaying = false;

      }
    });

    frame.setVisible(false);
  }

  /**
   * začiatok prehrávania video súboru
   * @param file - cesta k súboru na prehratie
   */
  public void start(String file) {

        isVPlaying = true;

        try {
            subor = file;
            
            String str = new String(file.getBytes("UTF-8"), Charset.defaultCharset());
            frame.setVisible(true);
            
            mediaPlayer.playMedia(str);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(VideoPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }

}

  /**
   * skrytie okna prehrávača
   */
  public static void setVisibleFalse() {
      frame.setVisible(false);

  }

  /**
   * koniec prehrávania video súboru
   */
  public static void stop() {
      mediaPlayer.stop();
  }

  /**
   * pozastavenie prípadne opätovné spustenie prehrávania video súboru
   */
  public static void pause() {
      if (mediaPlayer.isPlaying()) {
          MAPPSView.isPaused = false;
          mediaPlayer.pause();
      }
  }

}
