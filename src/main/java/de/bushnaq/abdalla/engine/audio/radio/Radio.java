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

package de.bushnaq.abdalla.engine.audio.radio;

import de.bushnaq.abdalla.engine.ai.PromptTags;
import de.bushnaq.abdalla.engine.ai.ollama.LLMPrompt;
import de.bushnaq.abdalla.engine.ai.ollama.OllamaClient;
import de.bushnaq.abdalla.engine.ai.ollama.OllamaException;
import de.bushnaq.abdalla.engine.audio.AudioEngine;
import de.bushnaq.abdalla.engine.audio.OpenAlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
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
    private static final String                    ANSI_BLUE       = "\u001B[36m";
    private static final String                    ANSI_GRAY       = "\u001B[37m";
    private static final String                    ANSI_GREEN      = "\u001B[32m";
    private static final String                    ANSI_RED        = "\u001B[31m";
    private static final String                    ANSI_RESET      = "\u001B[0m";    // Declaring ANSI_RESET so that we can reset the color
    private static final String                    ANSI_YELLOW     = "\u001B[33m";
    private static final Pattern                   KEY_PATTERN     = Pattern.compile("(.+)\\.(\\d+)$");
    private static final String                    LLM_MODEL       = "dolphin-mistral";//7b, fantastic, but slow 1500ms for short sentence
    private static final OllamaClient              client          = new OllamaClient();
    private final        AudioEngine               audioEngine;
    private final        Logger                    logger          = LoggerFactory.getLogger(this.getClass());
    private final        Object                    messagesLock    = new Object(); // Lock for thread-safe access to messages
    private final        List<RadioMessage>        radioRequests   = new ArrayList<>();
    private final        Random                    random          = new Random();
    private              ScheduledExecutorService  spokenMessageChecker;
    private final        Map<String, List<String>> stringOptions   = new HashMap<>();//every id can have a list of string options
    private final        Map<String, LLMPrompt>    systemPromptMap = new HashMap<>();//have to be registered, can be used to generate radio messages via AI
    private final        TTSPlayer                 ttsPlayer;

    public Radio(AudioEngine audioEngine, String name) throws OpenAlException {
        this.audioEngine = audioEngine;
        this.ttsPlayer   = audioEngine.createAudioProducer(TTSPlayer.class, name);
        this.ttsPlayer.setGain(1f);
        try {
            client.pullModel(LLM_MODEL, Duration.ofMinutes(20));
        } catch (OllamaException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("initialized tts");
        startSpokenMessageChecker();
    }

    private String askAi(String id, String prompt, PromptTags tags) {
        try {
            LLMPrompt systemPrompt = new LLMPrompt(systemPromptMap.get(id), tags);
            if (prompt != null && !prompt.isEmpty())
                systemPrompt.setPrompt(prompt);//- overwrite default prompt
            String response = client.generate(LLM_MODEL, systemPrompt.getPrompt(), systemPrompt.getSystemPrompt()).getResponse();
            logger.info(response);
            return response;
        } catch (OllamaException e) {
            logger.error("Error generating ai ratio message: {}", e.getMessage(), e);
            return null;
        }
    }

    private String cleanupAiAnswer(String answer) {
        return removeUnwantedCharactersFromReponse(removeThinkingFromResponse(unquote(answer)));
    }

    public String generateLlmAnswer(String id, String prompt, PromptTags tags, boolean silent) {
        //lets not use ai for silent messages
        if (!silent) {
            long   time = System.currentTimeMillis();
            String text = cleanupAiAnswer(askAi(id, prompt, tags));
            if (text != null) {
                long delta = System.currentTimeMillis() - time;
                logger.info(String.format("LLM: '%s' - %dms", text, delta));
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

    /**
     * Process messages one by one asynchronously.
     */
    private void generateSpokenMessages() {
        try {
            synchronized (messagesLock) {
                while (!radioRequests.isEmpty()) {
                    RadioMessage rm = radioRequests.removeFirst();
                    rm.getFrom().processRadioMessage(rm);//processed by the sender
                }
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
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
    public void queueRadioMessageGeneration(RadioMessage rm) {
        if (rm.isSilent()) {
            rm.getFrom().processRadioMessage(rm);
        } else {
            radioRequests.add(rm);//queue spoken message generation
        }
    }

    @Override
    public void radio(RadioMessage rm) {
//        rm.to.notifyStartedTalking(rm);// send to partner
        say(rm);
    }

    public void registerSystemPrompt(String id, LLMPrompt systemPrompt) {
        systemPromptMap.put(id, systemPrompt);
    }

    private String removeHtmlTags(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            // Check if there's a capturing group before accessing it
            if (matcher.groupCount() > 0) {
                String content = matcher.group(1).trim();
                logger.info(ANSI_BLUE + content + ANSI_RESET);
            } else {
                // For patterns without capturing groups, log the entire match
                String content = matcher.group(0).trim();
                logger.info(ANSI_BLUE + content + ANSI_RESET);
            }
        }
        // Then remove the blocks
        return input.replaceAll(regex, "").trim();
    }

    /**
     * Extract the actual answer from AI response by removing thinking process.
     */
    private String removeThinkingFromResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return rawResponse;
        }

        String response = rawResponse.trim();

        // Remove content between thinking tags
        response = removeHtmlTags(response, "(?s)<think>.*?</think>");
        response = removeHtmlTags(response, "(?s)<thinking>.*?</thinking>");
        response = removeHtmlTags(response, "(?s)<!--\\s*thinking.*?-->");
        // Remove lines that start with reasoning markers
        response = removeHtmlTags(response, "(?m)^(Thinking:|Let me think:).*$");

        // Extract content after answer markers
        if (response.matches("(?s).*\\b(Answer|Result|Output):\\s*(.*)")) {
            String[] parts = response.split("\\b(?:Answer|Result|Output):\\s*", 2);
            if (parts.length > 1) {
                response = parts[1].trim();
            }
        }

        // Remove only <think>, </think>, <thinking>, </thinking> tags
        response = response.replaceAll("</?think(?:ing)?>", "");

        return response.isEmpty() ? rawResponse : response;
    }

    private String removeUnwantedCharactersFromReponse(String rawResponse) {
        return rawResponse.replaceAll("<[^>]+>", "").trim();
    }

    @Override
    public void renderRadio() throws OpenAlException {
        ttsPlayer.play();
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

    String unquote(String input) {
        if (input == null || input.isEmpty()) {
            return input; // return as is
        }
        input = input.trim();
        input = input.replaceAll("^(\"|')|(\"|')$", "");
        return input;
    }

}
