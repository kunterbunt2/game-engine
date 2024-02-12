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

    public void adaptToVelocity(final float speed) throws OpenAlException;

    public OpenAlSource disable() throws OpenAlException;

    public void dispose() throws OpenAlException;

    public void enable(final OpenAlSource source) throws OpenAlException;

    public int getChannels();

    public int getOpenAlFormat();

    public Vector3 getPosition();

    public boolean isEnabled();

    public boolean isPlaying() throws OpenAlException;

    public void pause() throws OpenAlException;

    public void play() throws OpenAlException;

    public void processBuffer(ByteBuffer byteBuffer) throws OpenAlcException;

    public void setGain(final float gain) throws OpenAlException;

    public void setPositionAndVelocity(final float[] position, final float[] velocity) throws OpenAlException;

    public void waitForPlay() throws InterruptedException, OpenAlException;

    //	public short process(long l);

    public void writeWav(final String fileName) throws IOException, OpenAlcException;

}
