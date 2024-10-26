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

package de.bushnaq.abdalla.engine.util;

import com.badlogic.gdx.graphics.Color;

public class ColorUtil {
    public static Color mix(Color c1, Color c2, float f1) {
        f1 = Math.max(f1, 0f);
        f1 = Math.min(f1, 1f);
        float f2 = 1 - f1;
//        return new Color(c1.r * f1 + c2.r * f2, c1.g * f1 + c2.g * f2, c1.b * f1 + c2.b * f2, c1.a * f1 + c2.a * f2);
        return new Color(c1.r * f1 + c2.r * f2, c1.g * f1 + c2.g * f2, c1.b * f1 + c2.b * f2, 1f);
    }
}
