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

package de.bushnaq.abdalla.engine.audio.synthesis.util;

import de.bushnaq.abdalla.engine.audio.OpenAlException;
import de.bushnaq.abdalla.engine.audio.synthesis.SinOscillator;
import de.bushnaq.abdalla.engine.audio.synthesis.Synthesizer;

public class SinSynthesizer extends Synthesizer {
    public SinOscillator sin1;

    public SinSynthesizer() throws OpenAlException {
        super(44100);
        sin1 = new SinOscillator();
        add(sin1);
    }

    @Override
    public void adaptToVelocity(float speed) throws OpenAlException {
        speed = Math.min(50, speed);
        speed = Math.max(5, speed);

        final float speedFactor = speed / 5;
        final float frequency   = 440f * speedFactor;
        sin1.setOscillator(frequency);
        setGain(2.0f);
        //		add(new SawGen(29f * speedFactor));
        //		add(new Lfo1(2f * speedFactor, 1.0f));
        //		add(new SinGen(frequency * 2));
        //		final float bassGain = 1 - (speed - 10) / 40;
        //		setGain(5f + bassGain * 10);
        //		setFilterGain(bassGain, (speed - 10) / 40);
        //				createFilter();
        //		createBassBoost();
        //		setBassBoostGain(frequency, bassGain * 48);
        //				createBassBoost();
        //		begin();

    }

}
