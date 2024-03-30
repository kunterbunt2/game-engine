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

import com.badlogic.gdx.math.Vector3;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface AudioProducer {

    void adaptToVelocity(final float speed) throws OpenAlException;

    OpenAlSource disable() throws OpenAlException;

    void dispose() throws OpenAlException;

    void enable(final OpenAlSource source) throws OpenAlException;

    int getChannels();

    float getGain();

    int getOpenAlFormat();

    Vector3 getPosition();

    int getSamplerate();

    boolean isAmbient();

    boolean isEnabled();

    boolean isOptIn();// can opt out, in that case the system will keep them disabled

    boolean isPlaying() throws OpenAlException;

    void pause() throws OpenAlException;

    void play() throws OpenAlException;

    void processBuffer(ByteBuffer byteBuffer) throws OpenAlcException;

    void setAmbient(boolean enabled);

    //	public short process(long l);

    void setGain(final float gain) throws OpenAlException;

    void setPositionAndVelocity(final float[] position, final float[] velocity) throws OpenAlException;

    void waitForPlay() throws InterruptedException, OpenAlException;

    void writeWav(final String fileName) throws IOException, OpenAlcException;
}
