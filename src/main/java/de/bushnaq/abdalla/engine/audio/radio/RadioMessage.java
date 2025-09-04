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
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RadioMessage {
    private final long         endTime;
    private final RadioChannel from;
    private final String       messageId;
    @Setter
    private       List<String> messages = new ArrayList<>();
    private       RadioMessage originalRequest;
    private final boolean      silent;
    private final PromptTags   tags;
    @Setter
    private       long         time;//universe time
    private final long         timeSent;
    private final RadioChannel to;

    public RadioMessage(long currentTime, RadioChannel from, RadioChannel to, String messageId, String message, boolean silent, PromptTags tags) {
        time           = currentTime;
        timeSent       = System.currentTimeMillis();
        this.from      = from;
        this.to        = to;
        this.messageId = messageId;
        this.silent    = silent;
        this.tags      = tags;
        this.endTime   = timeSent + estimatedDuration();
        addMessage(message);
    }

    public RadioMessage(boolean silent, RadioChannel from, RadioMessage originalRequest, RadioChannel to, String messageId, PromptTags tags) {
        this.silent          = silent;
        this.from            = from;
        this.originalRequest = originalRequest;
        this.to              = to;
        this.messageId       = messageId;
        this.tags            = tags;
        this.time            = 0;
        this.timeSent        = 0;
        this.endTime         = 0;
    }

//    public static String addCommaAndSpace(String input) {
//        if (input == null || input.isEmpty()) {
//            return input; // return as is
//        }
//
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < input.length(); i++) {
//            sb.append(input.charAt(i));
//            if (i < input.length() - 1) {
//                sb.append(",... ");
//            }
//        }
//        return sb.toString();
//    }

    public void addMessage(String message) {
        String[] strings = message.split("\\.");
        for (String string : strings) {
            if (!string.isBlank())
                messages.add(string.trim());
        }
    }

    public static String createMessage(String message, PromptTags tags) {

//        System.out.printf("Creating message from %s to %s: %s%n", from, to, message);
        return message = tags.replaceAllPreTags(message);
//        return message.replaceAll(fromTag, from).replaceAll(toTag, to);
//        return String.format(message, convertCallerName(from), convertCallerName(to));
    }

    public long estimatedDuration() {
        int duration = 0;
        for (String message : messages)
            duration += message.length() * 120L;
        return duration;
    }

    public String getAggregatedMessages() {
        StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            if (!sb.isEmpty())
                sb.append(". ");
            sb.append(message);
        }
        return sb.toString();
    }

    public boolean isFinished() {
        return System.currentTimeMillis() > endTime;
    }
}
