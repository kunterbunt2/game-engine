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
import de.bushnaq.abdalla.engine.audio.synthesis.*;

/**
 * use 2 saw oscillators with 5 cent vibrato, one generator is detuned -2.5 cent while the other is detuned 2.5 cent
 *
 * @author abdal
 */
public class ExampleSynthesizer extends Synthesizer {

    private static final float HIGHEST_FREQUENCY = 2 * 261.6256f;//C5
    private static final float LOWEST_FREQUENCY  = 32.70320f;//C1

    private final Lfo           lfo1;
    private final SawOscillator saw1;
    private final SawOscillator saw2;
    private final SawOscillator saw3;
    private final SawOscillator saw4;
    private final SinOscillator sin1;
    private final SinOscillator sin2;

    public ExampleSynthesizer() throws OpenAlException {
        super(44100);

        saw1 = new SawOscillator();
        add(saw1);
        saw2 = new SawOscillator();
        add(saw2);
        saw3 = new SawOscillator();
        add(saw3);
        saw4 = new SawOscillator();
        add(saw4);
        sin1 = new SinOscillator();
        add(sin1);
        sin2 = new SinOscillator();
        add(sin2);

        lfo1 = new Lfo1();
        add(lfo1);
        adaptToVelocity(15);
    }

    //c = 1200 × log2(f2 / f1)
    //f1*2^(c/1200) = f2
    //log 2 = 0.301029995

    @Override
    public void adaptToVelocity(float speed) throws OpenAlException {
        speed = Math.min(TranslationUtil.MAX_ENGINE_SPEED, speed);
        speed = Math.max(TranslationUtil.MIN_ENGINE_SPEED, speed);

        final float frequency  = LOWEST_FREQUENCY + (HIGHEST_FREQUENCY - LOWEST_FREQUENCY) * (speed - TranslationUtil.MIN_ENGINE_SPEED) / (TranslationUtil.MAX_ENGINE_SPEED - TranslationUtil.MIN_ENGINE_SPEED);
        final float detune     = 2.5f;//cent
        final float frequency1 = (float) (frequency * Math.pow(2, detune / 1200));

        final float bassGain = 1 - (speed - TranslationUtil.MIN_ENGINE_SPEED) / (TranslationUtil.MAX_ENGINE_SPEED - TranslationUtil.MIN_ENGINE_SPEED);

        //		sin1.setOscillator(frequency1);
        //		sin1.setLfo(1f, 5f);
        saw1.setOscillator(frequency1);
        saw1.setLfo(.87f, 5f);
        //		System.out.println(String.format("speed=%f frequency=%f", speed, frequency));

        final float frequency2 = (float) (frequency * Math.pow(2, -detune / 1200));
        saw2.setOscillator(frequency2);
        saw2.setLfo(0.86f, 5f);

        saw3.setOscillator(frequency1 / 2);
        saw3.setLfo(.88f, 15f);
        //
        saw4.setOscillator(frequency2 / 2);
        saw4.setLfo(.85f, 15f);
        //
        sin1.setOscillator(frequency1 * 2);
        sin1.setLfo(.88f, 150f);
        //
        sin2.setOscillator(frequency2 * 2);
        sin2.setLfo(.85f, 150f);

        final float factor = (speed - TranslationUtil.MIN_ENGINE_SPEED) / (TranslationUtil.MAX_ENGINE_SPEED - TranslationUtil.MIN_ENGINE_SPEED);
        //		setGain( 10.0f);
        setGain(0.1f + bassGain * 20);
        lfo1.setFrequency(1f + 5f * factor, 0.1f);
        //		lfo1.setFrequency(1f, 1.0f);

        final float lowGain  = bassGain;
        final float highGain = (1 - bassGain);
        //		setFilter(true);
        setFilterGain(lowGain, highGain);
        if (lowGain < 0.5f) {
            setFilter(false);
            setBassBoost(false);
        } else {
            setFilter(true);
            setBassBoost(true);
        }
        setBassBoostGain(frequency / 2, bassGain * (24));
    }
    //	public void init(float speed) throws OpenAlException {
    //		speed = Math.min(MAX_SPEED, speed);
    //		speed = Math.max(MIN_SPEED, speed);
    //
    //		final float speedFactor = speed / MIN_SPEED;
    //		final float frequency = 30f * speedFactor;
    //		add(new SawOscillator(frequency));
    //		add(new SawOscillator(29f * speedFactor));
    //		//		add(new Lfo1(2f * speedFactor, 1.0f));
    //		//		add(new SinGen(frequency * 2));
    //		final float bassGain = 1 - (speed - MIN_SPEED) / (MAX_SPEED - MIN_SPEED);
    //		setGain(5f + bassGain * 10);
    //		setFilterGain(bassGain, (speed - MIN_SPEED) / (MAX_SPEED - MIN_SPEED));
    //		//				createFilter();
    //		//		createBassBoost();
    //		setBassBoostGain(frequency, bassGain * 48);
    //		//				createBassBoost();
    //		//		begin();
    //	}

}
