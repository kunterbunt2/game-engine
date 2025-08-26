package de.bushnaq.abdalla.engine.ai;

import java.util.HashMap;
import java.util.Map;

public class PromptTags {
    public static final String              PAUSE_TAG = "<pause>";
    protected           Map<String, String> postTags  = new HashMap<>();
    protected           Map<String, String> preTags   = new HashMap<>();

    public PromptTags() {
        addPostTag(PAUSE_TAG, "...");
    }

    public void addPostTag(String name, String value) {
        postTags.put(name, value);
    }

    public void addPreTag(String name, String value) {
        preTags.put(name, value);
    }

    /**
     * Remove all post tags from the message.
     *
     * @param message the message with post tags
     * @return the message without post tags
     */
    public String removeAllPostTags(String message) {
        for (String key : postTags.keySet()) {
            message = message.replaceAll(key, "");
        }
        return message;
    }

    /**
     * Remove all pre tags from the message.
     *
     * @param message the message with pre tags
     * @return the message without pre tags
     */
    public String removeAllPreTags(String message) {
        for (String key : preTags.keySet()) {
            message = message.replaceAll(key, "");
        }
        return message;
    }

    /**
     * Replace tags after receiving from TTS engine.
     *
     * @param message the message with tags
     * @return the message with replaced tags
     */
    public String replaceAllPostTags(String message) {
        for (String key : postTags.keySet()) {
            message = message.replaceAll(key, postTags.get(key));
        }
        return message;
    }

    /**
     * Replace tags before sending to TTS engine.
     *
     * @param message the message with tags
     * @return the message with replaced tags
     */
    public String replaceAllPreTags(String message) {
        for (String key : preTags.keySet()) {
            message = message.replaceAll(key, preTags.get(key));
        }
        return message;
    }
}
