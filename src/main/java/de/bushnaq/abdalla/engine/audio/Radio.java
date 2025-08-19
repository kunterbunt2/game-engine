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
    private final        Random                    random          = new Random();
    private final        Map<String, List<String>> stringOptions   = new HashMap<>();//every id can have a list of string options
    private final        Map<String, LLMPrompt>    systemPromptMap = new HashMap<>();//have to be registered, can be used to generate radio messages via AI

    public Radio(AudioEngine audioEngine) throws OpenAlException {
        this.audioEngine = audioEngine;


        logger.info("initialized tts");
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

    public void registerSystemPrompt(String id, LLMPrompt systemPrompt) {
        systemPromptMap.put(id, systemPrompt);
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

    @Override
    public void talk(RadioMessage rm) {
        rm.to.radio(rm);// send to partner
    }

}
