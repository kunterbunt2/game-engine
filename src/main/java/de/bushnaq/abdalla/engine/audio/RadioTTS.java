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

import de.bushnaq.abdalla.engine.LLMPrompt;
import de.bushnaq.abdalla.engine.ai.ollama.OllamaClient;
import de.bushnaq.abdalla.engine.ai.ollama.OllamaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class to manage TTS snippets
 */
public class RadioTTS implements IRadio {
    private static final Pattern                   KEY_PATTERN     = Pattern.compile("(.+)\\.(\\d+)$");
    public static final  String                    SHIP_TAG        = "<ship>";
    public static final  String                    STATION_TAG     = "<station>";
    private static final OllamaClient              client          = new OllamaClient();
    //    private final        String                    assetFolderName;
    private final        AudioEngine               audioEngine;
    //    private              Set<String>               audioFiles;
    private final        Logger                    logger          = LoggerFactory.getLogger(this.getClass());
    //    private final        Map<String, FileHandle>   mp3Map                          = new HashMap<>();
    private final        List<String>              radioMessages   = new ArrayList<>();
    private final        Random                    random          = new Random();
    private final        Map<String, List<String>> stringOptions   = new HashMap<>();//every id can have a list of string options
    private final        Map<String, LLMPrompt>    systemPromptMap = new HashMap<>();//have to be registered, can be used to generate radio messages via AI

    public RadioTTS(AudioEngine audioEngine, String assetFolderName) throws OpenAlException {
        this.audioEngine = audioEngine;
//        this.assetFolderName = assetFolderName;


        logger.info("initialized tts");
    }

    private String askAi(String id) {
        try {
            LLMPrompt systemPrompt = systemPromptMap.get(id);
            if (systemPrompt != null)
                return client.generate("llama3.2:3b", systemPrompt.getPrompt(), systemPrompt.getSystemPrompt()).getResponse();
            return null;
        } catch (OllamaException e) {
            logger.error("Error generating ai ratio message: {}", e.getMessage(), e);
            return null;
        }
    }

    private String cleanupAiAnswer(String answer) {
        return answer;
    }

//    private List<FileHandle> creaetMp3List(List<String> tokens) {
//        List<FileHandle> mp3List = new ArrayList<>();
//        for (String token : tokens) {
//            mp3List.add(mp3Map.get(token));
//        }
//        return mp3List;
//    }

    public void dispose() {
//        helloVoice.deallocate();
    }

    public void loadResource(Class<?> clazz) throws IOException {
        try {
            final Properties radioProperties = new Properties();
//            radioProperties.clear();
            InputStream resourceAsStream = clazz.getResourceAsStream("/radio.properties");
            radioProperties.load(resourceAsStream);
//            try (FileInputStream fis = new FileInputStream(propertiesFile)) {
//                props.load(fis);
//            }

            for (String key : radioProperties.stringPropertyNames()) {
                Matcher m = KEY_PATTERN.matcher(key);
                if (m.matches()) {
                    String baseKey = m.group(1);
                    String value   = radioProperties.getProperty(key);

                    stringOptions.computeIfAbsent(baseKey, k -> new ArrayList<>())
                            .add(value);
                }
            }

            // Optional: shuffle each list to avoid predictable ordering
            stringOptions.values().forEach(Collections::shuffle);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    private void handleTokenEnd(String token, String value) {
//        logger.info(value);
//        switch (token) {
//            case "name":
//                break;
//            case "pause":
//                break;
//        }
//    }

//    private void handleTokenEnd(String value) {
//        renderTTSString(value.trim(), value);
//    }

//    public void listAllVoices() {
//        logger.info("All voices available:");
//        VoiceManager voiceManager = VoiceManager.getInstance();
//        Voice[]      voices       = voiceManager.getVoices();
//        for (int i = 0; i < voices.length; i++) {
//            logger.info("    " + voices[i].getName() + " (" + voices[i].getDomain() + " domain)");
//        }
//    }

//    private String insertPause(String name) {
//        return name.replace("-", ". ");
//    }

//    public FileHandle getFileHandle(String name) {
//        return mp3Map.get(name);
//    }
//
//    private boolean handleTokenStart(String token) {
//        switch (token) {
//            case "pause":
//                return true;
//        }
//        return false;
//    }

//    public void loadAudio() {
//        audioFiles = listFiles(assetFolderName + "/radio");
//        for (String file : audioFiles) {
//            mp3Map.put(removeFileExtension(file), Gdx.files.internal(assetFolderName + "/radio/" + file));
//        }
//    }

//    public void loadResource(Class<?> clazz) {
//        try {
//            radioProperties.clear();
//            InputStream resourceAsStream = clazz.getResourceAsStream("/radio.properties");
//            radioProperties.load(resourceAsStream);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

//    public Set<String> listFiles(String dir) {
//        return Stream.of(new File(dir).listFiles()).filter(file -> !file.isDirectory()).map(File::getName).collect(Collectors.toSet());
//    }

    public void registerSystemPrompt(String id, LLMPrompt systemPrompt) {
        systemPromptMap.put(id, systemPrompt);
    }

    //    public String resolveString(String stringID) {
//        return radioProperties.getProperty(stringID);
//    }
    public String resolveString(String id, boolean silent) {
        //lets not use ai for silent messages
        if (!silent) {

            String text = cleanupAiAnswer(askAi(id));
            if (text != null) {
                return text;
            }
        }
        //not an AI message id
        List<String> options = stringOptions.get(id);
        if (options == null || options.isEmpty()) {
            return null;
        }
        return options.get(random.nextInt(options.size()));
    }

//    private String removeFileExtension(String file) {
//        return file.substring(0, file.lastIndexOf('.'));
//    }

    /**
     * Renders all possible radio messages to wave files.
     */
//    private void renderAllResourceStrings() {
//        String[] tags = {"name", "pause"};
//        for (Object key : radioProperties.stringPropertyNames()) {
//            radioMessages.add((String) radioProperties.get(key));
//        }
//
//        for (String msg : radioMessages) {
//            logger.info(msg);
//            int start = -1;
//            int end   = -1;
//            int head  = 0;
//            int i     = 0;
//            for (i = 0; i < msg.length(); i++) {
//                String substring = msg.substring(i);
//                for (String token : tags) {
//                    String st = String.format("{%s}", token);
//                    if (substring.startsWith(st)) {
//                        if (head != i) {
//                            String value = msg.substring(head, i);
//                            handleTokenEnd(value);
//                        }
//                        if (handleTokenStart(token)) {
//                            head = i + st.length();
//                        }
//                        start = i + st.length();
//                    }
//                    String et = String.format("{/%s}", token);
//                    if (substring.startsWith(et)) {
//                        end = i;
//                        String value = msg.substring(start, end);
//                        handleTokenEnd(token, value);
//                        head = i + et.length();
//                    }
//                }
//            }
//            if (head != msg.length()) {
//                String value = msg.substring(head, i);
//                handleTokenEnd(value);
//            }
//            logger.info("end");
//        }
//    }

//    public void renderAllTTSStrings(List<String> nameList) {
//        for (String name : nameList) {
//            renderTTSString(name.trim(), insertPause(name));
//        }
//        renderAllResourceStrings();
//    }

//    private void renderTTSString(String key, String value) {
//        String                fileName    = assetFolderName + "/radio/" + key;
//        SingleFileAudioPlayer audioPlayer = new SingleFileAudioPlayer(fileName, AudioFileFormat.Type.WAVE);
//        helloVoice.setAudioPlayer(audioPlayer);
//        helloVoice.speak(value);
//        audioPlayer.close();
//        //convert to ogg
////        wavToOgg(fileName);
//    }

//    public List<String> tokenize(String msg) {
//        String[]     tags   = {"name", "pause"};
//        List<String> tokens = new ArrayList<>();
//        int          start  = -1;
//        int          end    = -1;
//        int          head   = 0;
//        int          i      = 0;
//        for (i = 0; i < msg.length(); i++) {
//            String substring = msg.substring(i);
//            for (String token : tags) {
//                String st = String.format("{%s}", token);
//                String et = String.format("{/%s}", token);
//                if (substring.startsWith(st)) {
//                    if (head != i) {
//                        String value = msg.substring(head, i);
//                        tokens.add(value.trim());
////                        handleTokenEnd(value);
//                    }
//                    if (handleTokenStart(token)) {
//                        head = i + st.length();
//                    }
//                    start = i + st.length();
//                } else if (substring.startsWith(et)) {
//                    end = i;
//                    String value = msg.substring(start, end);
//                    tokens.add(value.trim());
//                    handleTokenEnd(token, value);
//                    head = i + et.length();
//                }
//            }
//        }
//        if (head != msg.length()) {
//            String value = msg.substring(head, i);
//            tokens.add(value.trim());
////                handleTokenEnd(value);
//        }
//        return tokens;
//    }

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
