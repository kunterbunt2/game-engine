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
import de.bushnaq.abdalla.engine.ai.PromptTags;
import de.bushnaq.abdalla.engine.ai.ollama.OllamaClient;
import de.bushnaq.abdalla.engine.ai.ollama.OllamaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * used to communicate between CommunicationPartners.
 */
public class Radio implements IRadio {
    private static final Pattern                   KEY_PATTERN     = Pattern.compile("(.+)\\.(\\d+)$");
    private static final OllamaClient              client          = new OllamaClient();
    private final        AudioEngine               audioEngine;
    private final        Logger                    logger          = LoggerFactory.getLogger(this.getClass());
    private final        Object                    messagesLock    = new Object(); // Lock for thread-safe access to messages
    private final        List<RadioRequest>        radioRequests   = new ArrayList<>();
    private final        Random                    random          = new Random();
    private              ScheduledExecutorService  spokenMessageChecker;
    private final        Map<String, List<String>> stringOptions   = new HashMap<>();//every id can have a list of string options
    private final        Map<String, LLMPrompt>    systemPromptMap = new HashMap<>();//have to be registered, can be used to generate radio messages via AI
    private final        TTSPlayer                 ttsPlayer;

    public Radio(AudioEngine audioEngine) throws OpenAlException {
        this.audioEngine = audioEngine;
        this.ttsPlayer   = audioEngine.createAudioProducer(TTSPlayer.class);
        this.ttsPlayer.setGain(1f);
        logger.info("initialized tts");
        startSpokenMessageChecker();
    }

    private String askAi(String id, PromptTags tags) {
        try {
            LLMPrompt systemPrompt = new LLMPrompt(systemPromptMap.get(id), tags);
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

    public void dispose() {
    }

    /**
     * Process messages one by one asynchronously.
     */
    private void generateSpokenMessages() {
        synchronized (messagesLock) {
            while (!radioRequests.isEmpty()) {
                RadioRequest rr = radioRequests.removeFirst();
                rr.getFrom().processRadioMessage(rr);//processed by the sender
            }
        }
    }

    public void loadResource(Class<?> clazz) throws IOException {
        try {
            final Properties radioProperties  = new Properties();
            InputStream      resourceAsStream = clazz.getResourceAsStream("/radio.properties");
            radioProperties.load(resourceAsStream);

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

    @Override
    public void queueRadioMessageGeneration(RadioRequest rr) {
        if (rr.isSilent()) {
            rr.getFrom().processRadioMessage(rr);
        } else {
            radioRequests.add(rr);//queue spoken message generation
        }
    }

    @Override
    public void radio(RadioMessage rm) {
        rm.to.notifyStartedTalking(rm);// send to partner
        say(rm);
    }

    public void registerSystemPrompt(String id, LLMPrompt systemPrompt) {
        systemPromptMap.put(id, systemPrompt);
    }

    @Override
    public void renderRadio() throws OpenAlException {
        ttsPlayer.play();
    }

    public String resolveString(String id, PromptTags tags, boolean silent) {
        //lets not use ai for silent messages
        if (!silent) {

            String text = cleanupAiAnswer(askAi(id, tags));
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

    public void say(RadioMessage msg) {
//        if (Debug.isFilterPlanet(planet.getName())) {
//            logger.info(String.format("say %s selected=%b", msg, isSelected()));
//        }
        ttsPlayer.speak(msg);
    }

    /**
     * Start the background thread that checks for expired silent messages
     */
    private void startSpokenMessageChecker() {
        if (spokenMessageChecker == null) {
            spokenMessageChecker = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "TTSPlayer-SilentMessageChecker");
                t.setDaemon(true); // Don't prevent JVM shutdown
                return t;
            });

            // Check every 100ms for expired silent messages
            spokenMessageChecker.scheduleAtFixedRate(this::generateSpokenMessages, 0, 100, TimeUnit.MILLISECONDS);
//            logger.info("Silent message checker thread started");
        }
    }

    /**
     * Stop the silent message checker thread
     */
    public void stopSpokenMessageChecker() {
        if (spokenMessageChecker != null && !spokenMessageChecker.isShutdown()) {
            spokenMessageChecker.shutdown();
            try {
                if (!spokenMessageChecker.awaitTermination(1, TimeUnit.SECONDS)) {
                    spokenMessageChecker.shutdownNow();
                }
            } catch (InterruptedException e) {
                spokenMessageChecker.shutdownNow();
                Thread.currentThread().interrupt();
            }
//            logger.info("Silent message checker thread stopped");
            spokenMessageChecker = null;
        }
    }

}
