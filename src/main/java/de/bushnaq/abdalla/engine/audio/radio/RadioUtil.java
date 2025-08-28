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

import java.util.Map;

public class RadioUtil {
    private static final Map<Character, String> NATO_MAP = Map.ofEntries(
            Map.entry('A', "Alfa"),
            Map.entry('B', "Bravo"),
            Map.entry('C', "Charlie"),
            Map.entry('D', "Delta"),
            Map.entry('E', "Echo"),
            Map.entry('F', "Foxtrot"),
            Map.entry('G', "Golf"),
            Map.entry('H', "Hotel"),
            Map.entry('I', "India"),
            Map.entry('J', "Juliett"),
            Map.entry('K', "Kilo"),
            Map.entry('L', "Lima"),
            Map.entry('M', "Mike"),
            Map.entry('N', "November"),
            Map.entry('O', "Oscar"),
            Map.entry('P', "Papa"),
            Map.entry('Q', "Quebec"),
            Map.entry('R', "Romeo"),
            Map.entry('S', "Sierra"),
            Map.entry('T', "Tango"),
            Map.entry('U', "Uniform"),
            Map.entry('V', "Victor"),
            Map.entry('W', "Whiskey"),
            Map.entry('X', "X-ray"),
            Map.entry('Y', "Yankee"),
            Map.entry('Z', "Zulu")
    );

    public static String addCommaAndSpace(String input) {
        if (input == null || input.isEmpty()) {
            return input; // return as is
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            sb.append(input.charAt(i));
            if (i < input.length() - 1) {
                sb.append(",... ");
            }
        }
        return sb.toString();
    }

    private static String convertCallerName(String from) {
        char upper = Character.toUpperCase(from.charAt(0));
//        return NATO_MAP.getOrDefault(upper, String.valueOf(upper)) + ",... " + addCommaAndSpace(from.substring(2));
        return NATO_MAP.getOrDefault(upper, String.valueOf(upper)) + ",... " + from.substring(2);
    }

}
