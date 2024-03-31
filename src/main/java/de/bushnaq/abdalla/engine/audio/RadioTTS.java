/*
 * Copyright (C) 2024 Abdalla Bushnaq
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.bushnaq.abdalla.engine.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFileFormat;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main class to manage TTS snippets
 */
public class RadioTTS {
    public static final String                  REQUESTING_APPROVAL_TO_DOCK_01 = "REQUESTING_APPROVAL_TO_DOCK_01";
    public static final String                  REQUEST_TO_DOCK_APPROVED_01    = "REQUEST_TO_DOCK_APPROVED_01";
    private final       String                  assetFolderName;
    private final       AudioEngine             audioEngine;
    private final       Voice                   helloVoice;
    private final       Logger                  logger                         = LoggerFactory.getLogger(this.getClass());
    private final       Map<String, FileHandle> mp3Map                         = new HashMap<>();
    private final       List<String>            radioMessages                  = new ArrayList<>();
    Properties radioProperties = new Properties();
    private Set<String> audioFiles;

    public RadioTTS(AudioEngine audioEngine, String assetFolderName) throws OpenAlException {
        this.audioEngine     = audioEngine;
        this.assetFolderName = assetFolderName;


        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        listAllVoices();
        VoiceManager voiceManager = VoiceManager.getInstance();
        helloVoice = voiceManager.getVoice("kevin16");
        helloVoice.allocate();
//        loadAudio();
        logger.info("initialized tts");

    }

    private List<FileHandle> creaetMp3List(List<String> tokens) {
        List<FileHandle> mp3List = new ArrayList<>();
        for (String token : tokens) {
            mp3List.add(mp3Map.get(token));
        }
        return mp3List;
    }

    public void dispose() {
        helloVoice.deallocate();
    }

    public FileHandle getFileHandle(String name) {
        return mp3Map.get(name);
    }

    private void handleTokenEnd(String token, String value) {
        logger.info(value);
        switch (token) {
            case "name":
                break;
            case "pause":
                break;
        }
    }

    private void handleTokenEnd(String value) {
        renderTTSString(value.trim(), value);
    }

    private boolean handleTokenStart(String token) {
        switch (token) {
            case "pause":
                return true;
        }
        return false;
    }

    private String insertPause(String name) {
        return name.replace("-", ". ");
    }

    public void listAllVoices() {
        logger.info("All voices available:");
        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice[]      voices       = voiceManager.getVoices();
        for (int i = 0; i < voices.length; i++) {
            logger.info("    " + voices[i].getName() + " (" + voices[i].getDomain() + " domain)");
        }
    }

    public Set<String> listFiles(String dir) {
        return Stream.of(new File(dir).listFiles()).filter(file -> !file.isDirectory()).map(File::getName).collect(Collectors.toSet());
    }

    public void loadAudio() {
        audioFiles = listFiles(assetFolderName + "/radio");
        for (String file : audioFiles) {
            mp3Map.put(removeFileExtension(file), Gdx.files.internal(assetFolderName + "/radio/" + file));
        }
    }

    public void loadResource(Class<?> clazz) {
        try {
            radioProperties.clear();
            InputStream resourceAsStream = clazz.getResourceAsStream("/radio.properties");
            radioProperties.load(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String removeFileExtension(String file) {
        return file.substring(0, file.lastIndexOf('.'));
    }

    /**
     * Renders all possible radio messages to wave files.
     */
    private void renderAllResourceStrings() {
        String[] tags = {"name", "pause"};
        for (Object key : radioProperties.stringPropertyNames()) {
            radioMessages.add((String) radioProperties.get(key));
        }

        for (String msg : radioMessages) {
            logger.info(msg);
            int start = -1;
            int end   = -1;
            int head  = 0;
            int i     = 0;
            for (i = 0; i < msg.length(); i++) {
                String substring = msg.substring(i);
                for (String token : tags) {
                    String st = String.format("{%s}", token);
                    if (substring.startsWith(st)) {
                        if (head != i) {
                            String value = msg.substring(head, i);
                            handleTokenEnd(value);
                        }
                        if (handleTokenStart(token)) {
                            head = i + st.length();
                        }
                        start = i + st.length();
                    }
                    String et = String.format("{/%s}", token);
                    if (substring.startsWith(et)) {
                        end = i;
                        String value = msg.substring(start, end);
                        handleTokenEnd(token, value);
                        head = i + et.length();
                    }
                }
            }
            if (head != msg.length()) {
                String value = msg.substring(head, i);
                handleTokenEnd(value);
            }
            logger.info("end");
        }
    }

    public void renderAllTTSStrings(List<String> nameList) {
        for (String name : nameList) {
            renderTTSString(name.trim(), insertPause(name));
        }
        renderAllResourceStrings();
    }

    private void renderTTSString(String key, String value) {
        String                fileName    = assetFolderName + "/radio/" + key;
        SingleFileAudioPlayer audioPlayer = new SingleFileAudioPlayer(fileName, AudioFileFormat.Type.WAVE);
        helloVoice.setAudioPlayer(audioPlayer);
        helloVoice.speak(value);
        audioPlayer.close();
        //convert to ogg
//        wavToOgg(fileName);
    }

    public String resolveString(String stringID) {
        return radioProperties.getProperty(stringID);
    }

    public List<String> tokenize(String msg) {
        String[]     tags   = {"name", "pause"};
        List<String> tokens = new ArrayList<>();
        int          start  = -1;
        int          end    = -1;
        int          head   = 0;
        int          i      = 0;
        for (i = 0; i < msg.length(); i++) {
            String substring = msg.substring(i);
            for (String token : tags) {
                String st = String.format("{%s}", token);
                String et = String.format("{/%s}", token);
                if (substring.startsWith(st)) {
                    if (head != i) {
                        String value = msg.substring(head, i);
                        tokens.add(value.trim());
//                        handleTokenEnd(value);
                    }
                    if (handleTokenStart(token)) {
                        head = i + st.length();
                    }
                    start = i + st.length();
                } else if (substring.startsWith(et)) {
                    end = i;
                    String value = msg.substring(start, end);
                    tokens.add(value.trim());
                    handleTokenEnd(token, value);
                    head = i + et.length();
                }
            }
        }
        if (head != msg.length()) {
            String value = msg.substring(head, i);
            tokens.add(value.trim());
//                handleTokenEnd(value);
        }
        return tokens;
    }

//    public void speak(String message) {
//        List<String>     tokens      = tokenize(message);
//        List<FileHandle> fileHandles = creaetMp3List(tokens);
//        int              index       = 0;
//        TTSPlayer        mp3Player;
//        try {
//            mp3Player = audioEngine.createAudioProducer(TTSPlayer.class);
//            mp3Player.speak(fileHandles.get(index));
//            mp3Player.setGain(150.0f);
//            mp3Player.play();
//        } catch (OpenAlException e) {
//            logger.info(e.getMessage(), e);
//        }
//    }

//    private void wavToOgg(String fileName) {
//        FileHandle           file  = new FileHandle(fileName);
//        Wav.WavOutputtStream input = new Wav.WavOutputtStream(file);
//
//
//        OggOutputStream output     = new OggInputStream(file.read());
//        int             channels   = input.getChannels();
//        int             format     = channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;
//        int             sampleRate = input.getSampleRate();
////        maxSecondsPerBuffer = (float) bufferSize / (bytesPerSample * channels * sampleRate);
//        new Wav.WavOutputtStream(file);
//        int value = 0;
//        do {
//            value = input.read();
//        }
//        while (value != -1);
//
//    }

}
