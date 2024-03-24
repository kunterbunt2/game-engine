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

package de.bushnaq.abdalla.engine.audio.synthesis;

import com.scottlogic.util.UnsortedList;
import de.bushnaq.abdalla.engine.audio.AudioEngine;
import de.bushnaq.abdalla.engine.audio.OpenAlException;

import java.util.List;

public abstract class AbstractSynthesizerFactory<T> implements SynthesizerFactory<T> {
    private final List<T> synthsCache = new UnsortedList<>();

    @Override
    public void cacheSynth(final T synth) {
        synthsCache.add(synth);
    }

    @Override
    public T createSynth(AudioEngine audioEngine) throws OpenAlException {
        if (synthsCache.size() != 0) {
            final T synth = synthsCache.remove(0);
            return synth;
        } else {
            final T synth = uncacheSynth(audioEngine);
            return synth;
        }
    }

}
