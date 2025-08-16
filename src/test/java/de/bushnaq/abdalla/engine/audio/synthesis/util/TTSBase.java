package de.bushnaq.abdalla.engine.audio.synthesis.util;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class TTSBase {
    protected static void playBlocking(String fileName) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File(fileName)));
            clip.start();
            while (!clip.isRunning())
                Thread.sleep(10);
            while (clip.isRunning())
                Thread.sleep(10);
            clip.close();
        } catch (Exception exc) {
            exc.printStackTrace(System.out);
        }
    }

}
