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

import de.bushnaq.abdalla.engine.audio.synthesis.BassBoost;

public class Filters {
    private final AbstractAudioProducer producer;
    private final int                   samplerate;
    public        BassBoost             bassBoost          = null;
    public        boolean               enableFilter;
    public        Filter                filter             = null;
    public        float                 highGain           = 0.0f;
    public        float                 lowGain            = 1.0f;
    protected     float                 bassBoostDbGain    = 12;
    protected     float                 bassBoostFrequency = 440;
    protected     float                 filterFrequency    = 1500;//12db
    private       boolean               enableBassBoost    = false;

    public Filters(int samplerate, AbstractAudioProducer producer) {
        this.samplerate = samplerate;
        this.producer   = producer;
    }

    protected void createBassBoost() {
        bassBoost = new BassBoost(bassBoostFrequency, bassBoostDbGain, samplerate);
    }

    protected void createFilter() {
        filter = new Filter(filterFrequency, samplerate, Filter.PassType.Highpass, .8f);
    }

    private void removeBasBoost() {
        bassBoost = null;
    }

    private void removeFilter() {
        filter = null;
    }


    public void setBassBoost(final boolean enableBassBoost) throws OpenAlException {
        this.enableBassBoost = enableBassBoost;
        if (producer.isEnabled())
            this.updateBassBoost(enableFilter, lowGain, highGain);
    }

    public void setBassBoostGain(final float bassBoostFrequency, final float bassBoostDbGain) {
        this.bassBoostFrequency = bassBoostFrequency;
        this.bassBoostDbGain    = bassBoostDbGain;
    }

    public void setFilter(final boolean enableFilter) throws OpenAlException {
        this.enableFilter = enableFilter;
        if (producer.isEnabled())
            this.updateFilter(enableFilter, lowGain, highGain);
    }

    void updateBassBoost(final boolean enableBassBoost, final float bassBoostFrequency, final float bassBoostDbGain) throws OpenAlException {
        if (bassBoost != null) {
            if (enableBassBoost) {
                bassBoost.set(bassBoostFrequency, bassBoostDbGain, samplerate);
            } else {
                removeBasBoost();
            }
        } else {
            //no filter
            if (enableBassBoost) {
                createBassBoost();
            } else {
                //ok
            }
        }
    }

    private void updateFilter(boolean enableFilter, float lowGain, float highGain) {
        if (filter != null) {
            if (enableFilter) {
                filter.set(filterFrequency, samplerate, Filter.PassType.Highpass, .8f);
            } else {
                removeBasBoost();
            }
        } else {
            //no filter
            if (enableFilter) {
                createFilter();
            } else {
                //ok
            }
        }
    }

}
