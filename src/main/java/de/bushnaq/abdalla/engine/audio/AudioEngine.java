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
import com.scottlogic.util.UnsortedList;
import de.bushnaq.abdalla.engine.audio.synthesis.AbstractSynthesizerFactory;
import de.bushnaq.abdalla.engine.camera.MovingCamera;
import org.lwjgl.openal.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SoundEngine manages Synthesizer instances and caches them when they are not used
 * Synthesizer need to be all of the same type
 * Synthesizer must be reinitialized when reusing them
 *
 * @author abdalla bushnaq
 */
public class AudioEngine {
    private static final int                 START_RADIUS     = 1500;
    private static final int                 STOP_RADIUS      = 2000;
    private static       ALCapabilities      alCapabilities;
    private static       ALCCapabilities     alcCapabilities;
    private static       long                device;
    private static       Logger              logger           = LoggerFactory.getLogger(AudioEngine.class);
    private final        int                 bits;
    private final        Vector3             direction        = new Vector3();//direction of the listener (what direction is he looking to)
    private final        float               disableRadius2   = STOP_RADIUS * STOP_RADIUS;//all audio streams that are located further away will be stopped and removed
    private final        float               enableRadius2    = START_RADIUS * START_RADIUS;//an audio streams that gets closer will get added and started
    private final        Vector3             listenerPosition = new Vector3();//position of the listener, usually the camera
    private final        Vector3             listenerVelocity = new Vector3();//the velocity of the listener, usually the camera
    private final        int                 samplerate;
    private final        int                 samples;
    //	private MovingCamera camera;
    //	private final SynthesizerFactory<T> synthFactory;
    private final        List<AudioProducer> synths           = new UnsortedList<>();
    private final        List<OpenAlSource>  unusedSources    = new ArrayList<>();
    private final        Vector3             up               = new Vector3();//what is up direction for the listener?
    public               RadioTTS            radioTTS;
    int                                                              auxiliaryEffectSlot;
    Map<String, AbstractSynthesizerFactory<? extends AudioProducer>> factoryMap = new HashMap<>();
    private long context;
    private int  effect;
    private int  enabledAudioSourceCount = 0;
    private int  maxMonoSources          = 0;
    private int  numberOfSources         = 0;

    public AudioEngine(final int samples, final int samplerate, final int bits/*, final int channels*/) {
        this.samples    = samples;
        this.samplerate = samplerate;
        this.bits       = bits;
        //		this.channels = channels;
    }

    public static void checkAlError(final String message) throws OpenAlException {
        final int error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            final String msg = message + error + " " + getALErrorString(error);
            logger.error(msg);
            throw new OpenAlException(msg);
        }
    }

    public static void checkAlcError(final String message) throws OpenAlException {
        final int error = ALC10.alcGetError(device);
        if (error != ALC10.ALC_NO_ERROR) {
            final String msg = message + error + " " + getALCErrorString(error);
            logger.error(msg);
            throw new OpenAlcException(msg);
        }
    }

    public static void checkAlcError(final boolean result, final String message) throws OpenAlException {
        final int error = ALC10.alcGetError(device);
        if (error != ALC10.ALC_NO_ERROR) {
            final String msg = "Alc operation failed " + message + error + " " + getALCErrorString(error);
            logger.error(msg);
            throw new OpenAlcException(msg);
        }
    }

    /**
     * 1) Identify the error code.
     * 2) Return the error as a string.
     */
    public static String getALCErrorString(final int err) {
        switch (err) {
            case ALC10.ALC_NO_ERROR:
                return "AL_NO_ERROR";
            case ALC10.ALC_INVALID_DEVICE:
                return "ALC_INVALID_DEVICE";
            case ALC10.ALC_INVALID_CONTEXT:
                return "ALC_INVALID_CONTEXT";
            case ALC10.ALC_INVALID_ENUM:
                return "ALC_INVALID_ENUM";
            case ALC10.ALC_INVALID_VALUE:
                return "ALC_INVALID_VALUE";
            case ALC10.ALC_OUT_OF_MEMORY:
                return "ALC_OUT_OF_MEMORY";
            default:
                return "no such error code";
        }
    }

    /**
     * 1) Identify the error code.
     * 2) Return the error as a string.
     */
    public static String getALErrorString(final int err) {
        switch (err) {
            case AL10.AL_NO_ERROR:
                return "AL_NO_ERROR";
            case AL10.AL_INVALID_NAME:
                return "AL_INVALID_NAME";
            case AL10.AL_INVALID_ENUM:
                return "AL_INVALID_ENUM";
            case AL10.AL_INVALID_VALUE:
                return "AL_INVALID_VALUE";
            case AL10.AL_INVALID_OPERATION:
                return "AL_INVALID_OPERATION";
            case AL10.AL_OUT_OF_MEMORY:
                return "AL_OUT_OF_MEMORY";
            default:
                return "No such error code";
        }
    }

    public void add(final AbstractSynthesizerFactory<? extends AudioProducer> factory) {
        factoryMap.put(factory.getClass().getSimpleName(), factory);
    }

    public void begin(final MovingCamera camera, boolean enabled) throws OpenAlException {
        //		this.camera = camera;
        //did we move since last update?
        //		if (!position.equals(camera.position) || !up.equals(camera.up) || !direction.equals(camera.direction) || !velocity.equals(camera.velocity)) {
        //			position.set(camera.position.x, camera.position.y, camera.position.z);
        //			up.set(camera.up.x, camera.up.y, camera.up.z);
        //			direction.set(camera.direction.x, camera.direction.y, camera.direction.z);
        //			velocity.set(camera.velocity.x, camera.velocity.y, camera.velocity.z);
        //			updateCamera();
        //		}
        if (!enabled) {
            setListenerGain(0f);
        } else {
            setListenerGain(1f);
            if (!listenerPosition.equals(camera.position) || !up.equals(camera.up) || !direction.equals(camera.direction) || !listenerVelocity.equals(camera.velocity)) {
                listenerPosition.set(camera.position.x, camera.position.y, camera.position.z);//isometric view with camera hight but lookat location
                up.set(camera.up.x, camera.up.y, camera.up.z);
                direction.set(camera.direction.x, camera.direction.y, camera.direction.z);//ignore y axis in isometric game?
                listenerVelocity.set(camera.velocity.x, camera.velocity.y, camera.velocity.z);
                updateCamera();
            }
            cullSynths();
        }
    }

    public void create(String assetFolderName) throws OpenAlException {
        logger.info("----------------------------------------------------------------------------------");
        //		List<String> list = ALUtil.getStringList(0, ALC10.ALC_DEVICE_SPECIFIER/*, EnumerateAllExt.ALC_DEFAULT_ALL_DEVICES_SPECIFIER*/);
        final String deviceinfo = ALC10.alcGetString(0, EnumerateAllExt.ALC_DEFAULT_ALL_DEVICES_SPECIFIER);
        logger.info("Device: " + deviceinfo);
        device = ALC10.alcOpenDevice(deviceinfo);
        if (device == 0)
            throw new RuntimeException("Couldn't find such device");
        final int[] attributes = new int[]{ALC11.ALC_MONO_SOURCES, 1, 0};
        context = ALC10.alcCreateContext(device, attributes);
        final boolean b = ALC10.alcMakeContextCurrent(context);
        alcCapabilities = ALC.createCapabilities(device);
        AL.createCapabilities(alcCapabilities);
        alCapabilities = AL.getCapabilities();

        final int   size  = ALC10.alcGetInteger(device, ALC10.ALC_ATTRIBUTES_SIZE);
        final int[] attrs = new int[size];

        ALC10.alcGetIntegerv(device, ALC10.ALC_ALL_ATTRIBUTES, attrs);

        if (!ALC10.alcIsExtensionPresent(device, "ALC_SOFT_HRTF")) {
            dispose();
            throw new OpenAlException("Error: ALC_SOFT_HRTF not supported");
        }
        final int num_hrtf = ALC10.alcGetInteger(device, SOFTHRTF.ALC_NUM_HRTF_SPECIFIERS_SOFT);
        if (num_hrtf == 0)
            logger.error("No HRTFs found.");
        else {
            for (int i = 0; i < num_hrtf; i++) {
                final String name = SOFTHRTF.alcGetStringiSOFT(device, SOFTHRTF.ALC_HRTF_SPECIFIER_SOFT, i);
                logger.info(String.format("    %d: %s.", i, name));
            }
            final int index = 0;

            //enable hrtf
            //			enableHrtf(index);

        }
        //		disableHrtf(0);

        for (int i = 0; i < attrs.length; ++i) {
            if (attrs[i] == ALC11.ALC_MONO_SOURCES) {
                maxMonoSources = attrs[i + 1];
            }
        }
        setListenerOrientation(new Vector3(0, 0, -1), new Vector3(0, 1, 0));
        createAuxiliaryEffectSlot();
        radioTTS = new RadioTTS(this, assetFolderName);
        logger.info("----------------------------------------------------------------------------------");
    }

    public <T extends AudioProducer> T createAudioProducer(final Class<T> clazz) throws OpenAlException {

        for (final AbstractSynthesizerFactory<? extends AudioProducer> factory : factoryMap.values()) {
            if (factory.handles().isAssignableFrom(clazz)) {
                final T audioProducer = (T) factory.createSynth();
                synths.add(audioProducer);
                return audioProducer;
            }
        }

        //		if (MercatorSynthesizer.class.isAssignableFrom(clazz)) {
        //			T audioProducer = (T) mercatorSynthesizerFactory.createSynth();
        //			synths.add(audioProducer);
        //			return audioProducer;
        //		} else if (Mp3Player.class.isAssignableFrom(clazz)) {
        //			T audioProducer = (T) mp3PlayerFactory.createSynth();
        //			synths.add(audioProducer);
        //			return audioProducer;
        //		}
        return null;
    }

    private void createAuxiliaryEffectSlot() throws OpenAlException {
        auxiliaryEffectSlot = EXTEfx.alGenAuxiliaryEffectSlots();
        checkAlError("Failed to create auxiliary effect slot with error #");
        logger.trace("created  auxiliary effect slot " + auxiliaryEffectSlot);

        if (EXTEfx.alIsAuxiliaryEffectSlot(auxiliaryEffectSlot)) {
            effect = EXTEfx.alGenEffects();
            checkAlError("Failed to create auxiliary effect slot with error #");

            if (EXTEfx.alIsEffect(effect)) {
                EXTEfx.alEffecti(effect, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_REVERB);
                checkAlError("Failed to create auxiliary effect slot with error #");

                EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DECAY_TIME, 8.0f);
                checkAlError("Failed to create auxiliary effect slot with error #");

                EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_GAIN, 0.02f);
                checkAlError("Failed to create auxiliary effect slot with error #");

                EXTEfx.alAuxiliaryEffectSloti(auxiliaryEffectSlot, EXTEfx.AL_EFFECTSLOT_EFFECT, effect);
                checkAlError("Failed to create auxiliary effect slot with error #");
            }
        }
    }

    /**
     * There is a limit of supported audio sources
     * All synthesizers that are further away than disableRadius will be disabled and their audio source unassigned.
     * All synthesizers that are nearer than enableRadius will be enabled and assigned an audio source.
     *
     * @throws OpenAlException
     */
    private void cullSynths() throws OpenAlException {
        enabledAudioSourceCount = 0;
        for (final AudioProducer synth : synths) {
            if (synth.isEnabled() && (!synth.isAmbient() && listenerPosition.dst2(synth.getPosition()) > disableRadius2)) {
                //disable synth
                disableSynth(synth);
            } else if (!synth.isEnabled() && (synth.isAmbient() || listenerPosition.dst2(synth.getPosition()) < enableRadius2)) {
                //enable synth
                enableSynth(synth);
            } else {
                //synth should stay as it is now
                if (synth.isEnabled())
                    enabledAudioSourceCount++;
            }
        }
    }

    //	MercatorSynthesizerFactory mercatorSynthesizerFactory = new MercatorSynthesizerFactory();
    //	Mp3PlayerFactory mp3PlayerFactory = new Mp3PlayerFactory();

    public void disableHrtf(final int index) throws OpenAlException {
        int         i    = 0;
        final int[] attr = new int[5];
        attr[i++] = SOFTHRTF.ALC_HRTF_SOFT;
        attr[i++] = ALC10.ALC_FALSE;
        {
            logger.info(String.format("Disabling HRTF %d...", index));
            attr[i++] = SOFTHRTF.ALC_HRTF_ID_SOFT;
            attr[i++] = index;
        }
        attr[i] = 0;
        if (!SOFTHRTF.alcResetDeviceSOFT(device, attr))
            checkAlcError(String.format("Failed to reset device: %s", device));
        //				printf("Failed to reset device: %s\n", alcGetString(device, alcGetError(device)));
    }

    private void disableSynth(final AudioProducer synth) throws OpenAlException {
        if (synth.isEnabled()) {
            final OpenAlSource source = synth.disable();
            source.pause();
            unusedSources.add(source);
        } else {
            //do nothing
        }
    }

    public void dispose() throws OpenAlException {
        radioTTS.dispose();
        for (final AudioProducer synth : synths) {
            synth.dispose();
        }
        for (final OpenAlSource source : unusedSources) {
            source.dispose();
        }
        removeAuxiliaryEffectSlot();
        //		AudioEngine.checkAlError("Openal error #");
        {
            ALC10.alcSuspendContext(context);
            checkAlcError("Openal error #");
        }
        //		AudioEngine.checkAlError("Openal error #");
        {
            final boolean result = ALC10.alcMakeContextCurrent(0);
            checkAlcError(result, "Openal error #");
        }
        //all calls to AL10.alGetError from this point will fail with #40964 AL_INVALID_OPERATION, as it needs the context to work properly
        {
            ALC10.alcDestroyContext(context);
            checkAlcError("Openal error #");
        }
        {
            final boolean result = ALC10.alcCloseDevice(device);
            checkAlcError(result, "Openal error #");
        }
        //		{
        //			ALC.destroy();
        //		}
    }

    public void enableHrtf(final int index) throws OpenAlException {
        int         i    = 0;
        final int[] attr = new int[5];
        attr[i++] = SOFTHRTF.ALC_HRTF_SOFT;
        attr[i++] = ALC10.ALC_TRUE;
        {
            logger.info(String.format("Enabling HRTF %d...", index));
            attr[i++] = SOFTHRTF.ALC_HRTF_ID_SOFT;
            attr[i++] = index;
        }
        attr[i] = 0;
        if (!SOFTHRTF.alcResetDeviceSOFT(device, attr))
            checkAlcError(String.format("Failed to reset device: %s", device));
        //				printf("Failed to reset device: %s\n", alcGetString(device, alcGetError(device)));
        queryHrtfEnabled();
    }

    private void enableSynth(final AudioProducer synth) throws OpenAlException {
        if (synth.isEnabled()) {
            //do nothing
        } else {
            OpenAlSource source;
            if (unusedSources.size() > 0)
                source = unusedSources.remove(unusedSources.size() - 1);
            else {
                if (numberOfSources < 255) {
                    source = new OpenAlSource(samples, samplerate, bits, synth.getChannels(), auxiliaryEffectSlot);
                    synth.enable(source);
                    numberOfSources++;
                } else {
                    logger.error("Max openal source number (255) reached. Source not created!");
                }
            }
        }
        enabledAudioSourceCount++;
    }

    public void end() {
    }

    public int getDisabledAudioSourceCount() {
        return unusedSources.size();
    }

    public int getEnabledAudioSourceCount() {
        return enabledAudioSourceCount;
    }

    public Vector3 getListenerPosition() {
        return listenerPosition;
    }

    public int getMaxMonoSources() {
        return maxMonoSources;
    }

    public int getNumberOfAudioProducers() {
        return synths.size();
    }

    public int getNumberOfSources() {
        return numberOfSources;
    }

    public int getSamplerate() {
        return samplerate;
    }

    public int getSamples() {
        return samples;
    }

    private void queryHrtfEnabled() {
        /* Check if HRTF is enabled, and show which is being used. */
        final int hrtf_state = ALC10.alcGetInteger(device, SOFTHRTF.ALC_HRTF_SOFT);
        if (hrtf_state == 0)
            logger.error("HRTF not enabled!");
        else {
            final String name = ALC10.alcGetString(device, SOFTHRTF.ALC_HRTF_SPECIFIER_SOFT);
            logger.info(String.format("HRTF enabled, using %s", name));
        }
    }

    public void remove(final AudioProducer audioProducer) {
        synths.remove(audioProducer);
        //		synthFactory.cacheSynth(Synth);
        //		if (MercatorSynthesizer.class.isInstance(audioProducer)) {
        //			mercatorSynthesizerFactory.cacheSynth((MercatorSynthesizer) audioProducer);
        //		} else if (Mp3Player.class.isInstance(audioProducer)) {
        //			mp3PlayerFactory.cacheSynth((Mp3Player) audioProducer);
        //		}
        for (final AbstractSynthesizerFactory factory : factoryMap.values()) {
            if (factory.handles().isInstance(audioProducer)) {
                factory.cacheSynth(audioProducer);
            }
        }
    }

    private void removeAuxiliaryEffectSlot() throws OpenAlException {
        EXTEfx.alDeleteAuxiliaryEffectSlots(auxiliaryEffectSlot);
        AudioEngine.checkAlError("Failed to delete auxiliary effect slot with error #");
        auxiliaryEffectSlot = 0;
    }

    public void say(RadioMessage rm) {
        if (rm.from.isSelected() || rm.to.isSelected()) {
            radioTTS.speak(rm.message);
            logger.info(rm.message);
        }
    }

    private void setListenerGain(final float gain) throws OpenAlException {
        AL10.alListenerf(AL10.AL_GAIN, gain);
        checkAlError("Failed to set listener gain with error #");
    }

    private void setListenerOrientation(final Vector3 direction, final Vector3 up) throws OpenAlException {
        final float[] array = new float[]{direction.x, direction.y, direction.z, up.x, up.y, up.z};
        AL10.alListenerfv(AL10.AL_ORIENTATION, array);
        checkAlError("Failed to set listener orientation with error #");
    }

    //	public void setListenerPosition(final Vector3 position) throws OpenAlException {
    //		AL10.alListener3f(AL10.AL_POSITION, position.x, position.y, position.z);
    //		checkAlError("Failed to set listener position with error #");
    //	}

    private void setListenerPositionAndVelocity(final Vector3 position, final Vector3 velocity) throws OpenAlException {
        AL10.alListener3f(AL10.AL_POSITION, position.x, position.y, position.z);
        checkAlError("Failed to set listener position with error #");
        AL10.alListener3f(AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);
        checkAlError("Failed to set listener velocity with error #");
    }

    private void updateCamera() throws OpenAlException {
        setListenerOrientation(direction, up);
//        logger.info(String.format("listenerPosition= %f %f %f", listenerPosition.x, listenerPosition.y, listenerPosition.z));
        setListenerPositionAndVelocity(listenerPosition, listenerVelocity);
    }

}
