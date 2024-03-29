package views;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

// https://www.baeldung.com/java-play-sound
public class SoundPlayer implements LineListener {
    private FileInputStream fileInputStream;
    private BufferedInputStream bufferedInputStream;
    private Clip clip;
    private AudioInputStream audioInputStream;
    private final String fileName;
    private final boolean loop;
    private volatile boolean stopped = false;

    public SoundPlayer(String fileName, boolean loop) {
        this.fileName = fileName;
        this.loop = loop;
    }

    @Override
    public void update(LineEvent event) {
        if (event.getType() == LineEvent.Type.STOP) {
            try {
                close();
                if (loop && !stopped) {
                    start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        stop();
        stopped = false;
        startPlaying();
    }

    private synchronized void startPlaying() {
        try {
            fileInputStream = new FileInputStream(fileName);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.addLineListener(this);
            clip.start();
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        stopped = true;
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void close() throws IOException {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip.removeLineListener(this);
            clip = null;
        }

        if (audioInputStream != null) {
            audioInputStream.close();
            audioInputStream = null;
        }
        if (bufferedInputStream != null) {
            bufferedInputStream.close();
            bufferedInputStream = null;
        }
        if (fileInputStream != null) {
            fileInputStream.close();
            fileInputStream = null;
        }
    }
}
