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

package de.bushnaq.abdalla.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;

public class Fog {
    private float        beginDistance   = 15;
    private Color        color           = Color.BLACK;
    private boolean      enabled;
    private Environment  environment;
    private FogAttribute equation        = null;
    private float        falloffGradiant = 0.5f;
    private float        fullDistance    = 30;

    public Fog(Color color, float fogMinDistance, float fogMaxDistance, float fogMixValue) {
        this.color = color;
        this.setBeginDistance(fogMinDistance);
        this.fullDistance    = fogMaxDistance;
        this.falloffGradiant = fogMixValue;

    }

    public void createFog(Environment environment) {
        this.environment = environment;
        if (enabled) {
            environment.set(new ColorAttribute(ColorAttribute.Fog, getColor()));
            environment.set(new FogAttribute(FogAttribute.FogEquation));
            setFogEquation(environment);
        } else {
            environment.remove(ColorAttribute.Fog);
            environment.remove(FogAttribute.FogEquation);
        }
    }

    public float getBeginDistance() {
        return beginDistance;
    }

    public Color getColor() {
        return color;
    }

    public float getFalloffGradiant() {
        return falloffGradiant;
    }

    public float getFullDistance() {
        return fullDistance;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setBeginDistance(float beginDistance) {
        this.beginDistance = beginDistance;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        createFog(environment);//update environment
    }

    public void setFalloffGradiant(float falloffGradiant) {
        this.falloffGradiant = falloffGradiant;
    }

    public void setFogEquation(Environment environment) {
        equation = environment.get(FogAttribute.class, FogAttribute.FogEquation);
    }

    public void setFullDistance(float fullDistance) {
        this.fullDistance = fullDistance;
    }

    public void updateFog(Environment environment) {
        if (enabled) {
            if (equation != null) {
                // fogEquation.x is where the fog begins
                // .y should be where it reaches 100%
                // then z is how quickly it falls off
                // fogEquation.value.set(MathUtils.lerp(sceneManager.camera.near,
                // sceneManager.camera.far, (FOG_X + 1f) / 2f),
                // MathUtils.lerp(sceneManager.camera.near, sceneManager.camera.far, (FAG_Y +
                // 1f) / 2f),
                // 1000f * (FOG_Z + 1f) / 2f);
                equation.value.set(getBeginDistance(), fullDistance, falloffGradiant);
                environment.set(new ColorAttribute(ColorAttribute.Fog, getColor()));
            }
        } else {

        }
    }
}
