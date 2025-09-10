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
import de.bushnaq.abdalla.engine.ai.coqui.CoquiTTS;
import de.bushnaq.abdalla.engine.audio.radio.Radio;
import de.bushnaq.abdalla.engine.audio.radio.TTSPlayer;
import de.bushnaq.abdalla.engine.audio.synthesis.AbstractSynthesizerFactory;
import de.bushnaq.abdalla.engine.camera.MovingCamera;
import lombok.Getter;
import org.lwjgl.openal.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.EXTEfx.*;

/**
 * SoundEngine manages Synthesizer instances and caches them when they are not used
 * Synthesizer need to be all of the same type
 * Synthesizer must be reinitialized when reusing them
 *
 * @author abdalla bushnaq
 */
public class AudioEngine {
    private static final int                                                              START_RADIUS            = 1500;
    private static final int                                                              STOP_RADIUS             = 2000;
    private static final Logger                                                           logger                  = LoggerFactory.getLogger(AudioEngine.class);
    private static       ALCapabilities                                                   alCapabilities;
    private static       ALCCapabilities                                                  alcCapabilities;
    private final        int                                                              bits;
    private              long                                                             context;
    public               CoquiTTS                                                         coquiTTS;
    private              String                                                           currentDeviceName;
    private static       long                                                             device;//- the current device we use to output audio
    private final        Vector3                                                          direction               = new Vector3();//direction of the listener (what direction is he looking to)
    private final        float                                                            disableRadius2          = STOP_RADIUS * STOP_RADIUS;//all audio streams that are located further away will be stopped and removed
    private              int                                                              distortionEffectSlot;
    private final        float                                                            enableRadius2           = START_RADIUS * START_RADIUS;//an audio streams that gets closer will get added and started
    @Getter
    private              int                                                              enabledAudioSourceCount = 0;
    private final        Map<String, AbstractSynthesizerFactory<? extends AudioProducer>> factoryMap              = new HashMap<>();
    private final        Vector3                                                          listenerPosition        = new Vector3();//position of the listener, usually the camera
    private final        Vector3                                                          listenerVelocity        = new Vector3();//the velocity of the listener, usually the camera
    private              int                                                              mainEffectSlot;
    @Getter
    private              int                                                              maxMonoSources          = 0;
    @Getter
    private              int                                                              numberOfSources         = 0;
    public               Radio                                                            radio;
    @Getter
    private final        int                                                              samplerate;
    @Getter
    private final        int                                                              samples;
    private final        List<AudioProducer>                                              synths                  = new UnsortedList<>();
    private final        List<OpenAlSource>                                               unusedSources           = new ArrayList<>();
    private final        Vector3                                                          up                      = new Vector3();//what is up direction for the listener?

    public AudioEngine(final int samples, final int samplerate, final int bits/*, final int channels*/) {
        this.samples    = samples;
        this.samplerate = samplerate;
        this.bits       = bits;
        //		this.channels = channels;
    }

    public void add(final AbstractSynthesizerFactory<? extends AudioProducer> factory) {
        factoryMap.put(factory.getClass().getSimpleName(), factory);
    }

    public void begin(final MovingCamera camera, boolean enabled) throws OpenAlException {
        detectDisconnectedDevice();
        followDefaultDevice();
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
            radio.renderRadio();
        }
    }

    public static void checkAlError(final String name, final String message) throws OpenAlException {
        final int error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            final String msg = message + error + " " + getALErrorString(error) + " in " + name;
            logger.error(msg);
            throw new OpenAlException(msg);
        }
    }

    public static void checkAlcError(final String name, final String message) throws OpenAlException {
        final int error = ALC10.alcGetError(device);
        if (error != ALC10.ALC_NO_ERROR) {
            final String msg = message + error + " " + getALCErrorString(error) + " in " + name;
            logger.error(msg);
            throw new OpenAlcException(msg);
        }
    }

    public static void checkAlcError(final boolean result, final String name, final String message) throws OpenAlException {
        final int error = ALC10.alcGetError(device);
        if (error != ALC10.ALC_NO_ERROR) {
            final String msg = "Alc operation failed " + message + error + " " + getALCErrorString(error) + " in " + name;
            logger.error(msg);
            throw new OpenAlcException(msg);
        }
    }

    public void create() throws OpenAlException {
        logger.info("----------------------------------------------------------------------------------");
        initOpenAL();
        radio    = new Radio(this, "main-radio");
        coquiTTS = new CoquiTTS();
        logger.info("----------------------------------------------------------------------------------");
    }

    public <T extends AudioProducer> T createAudioProducer(final Class<T> clazz, String name) throws OpenAlException {

        for (final AbstractSynthesizerFactory<? extends AudioProducer> factory : factoryMap.values()) {
            if (factory.handles().isAssignableFrom(clazz)) {
                final T audioProducer = (T) factory.createSynth(this, name);
                synths.add(audioProducer);
                return audioProducer;
            }
        }

        return null;
    }

    private void createAuxiliaryEffectSlots() throws OpenAlException {
        distortionEffectSlot = alGenAuxiliaryEffectSlots();
        checkAlError("audio-engine", "Failed to create auxiliary effect slot with error #");

        mainEffectSlot = alGenAuxiliaryEffectSlots();
        checkAlError("audio-engine", "Failed to create auxiliary effect slot with error #");

        createDistortionEffect(distortionEffectSlot);
//        createReverbEffect(mainEffectSlot);
    }

    private void createDistortionEffect(int auxiliaryEffectSlot) throws OpenAlException {
        if (EXTEfx.alIsAuxiliaryEffectSlot(auxiliaryEffectSlot)) {
            int distortionEffect = alGenEffects();
            checkAlError("audio-engine", "Failed to create auxiliary AL_EFFECT_DISTORTION slot with error #");
            if (EXTEfx.alIsEffect(distortionEffect)) {
                alEffecti(distortionEffect, AL_EFFECT_TYPE, AL_EFFECT_DISTORTION);
                alEffectf(distortionEffect, AL_DISTORTION_EDGE, .2f);
                alEffectf(distortionEffect, AL_DISTORTION_GAIN, .5f);
                alEffectf(distortionEffect, AL_DISTORTION_LOWPASS_CUTOFF, 3000f);
                alEffectf(distortionEffect, AL_DISTORTION_EQCENTER, 3000f);
                alEffectf(distortionEffect, AL_DISTORTION_EQBANDWIDTH, 300f);
                alAuxiliaryEffectSloti(auxiliaryEffectSlot, AL_EFFECTSLOT_EFFECT, distortionEffect);
            }
//            int ringModulatorEffect = alGenEffects();
//            checkAlError("Failed to create auxiliary AL_EFFECT_PITCH_SHIFTER slot with error #");
//            if (EXTEfx.alIsEffect(ringModulatorEffect)) {
//                alEffecti(ringModulatorEffect, AL_EFFECT_TYPE, AL_EFFECT_RING_MODULATOR);
//                alEffectf(ringModulatorEffect, AL_RING_MODULATOR_FREQUENCY, 2000);
//                alEffectf(ringModulatorEffect, AL_RING_MODULATOR_HIGHPASS_CUTOFF, 2000);
////                alEffectf(ringModulatorEffect, AL_RING_MODULATOR_WAVEFORM, 0);
//                alAuxiliaryEffectSloti(auxiliaryEffectSlot, AL_EFFECTSLOT_EFFECT, ringModulatorEffect);
//            }
//            int pitchShiftEffect = alGenEffects();
//            checkAlError("Failed to create auxiliary AL_EFFECT_PITCH_SHIFTER slot with error #");
//            if (EXTEfx.alIsEffect(pitchShiftEffect)) {
//                alEffecti(pitchShiftEffect, AL_EFFECT_TYPE, AL_EFFECT_PITCH_SHIFTER);
//                alEffectf(pitchShiftEffect, AL_PITCH_SHIFTER_COARSE_TUNE, 12q);
//                alAuxiliaryEffectSloti(auxiliaryEffectSlot, AL_EFFECTSLOT_EFFECT, pitchShiftEffect);
//            }
        }
    }

    private void createReverbEffect(int auxiliaryEffectSlot) throws OpenAlException {
        if (EXTEfx.alIsAuxiliaryEffectSlot(auxiliaryEffectSlot)) {
            int reverbEffect = EXTEfx.alGenEffects();
            checkAlError("audio-engine", "Failed to create auxiliary reverbEffect slot with error #");

            if (EXTEfx.alIsEffect(reverbEffect)) {
                //reverb reverbEffect
                EXTEfx.alEffecti(reverbEffect, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_REVERB);
                checkAlError("audio-engine", "Failed to create auxiliary reverbEffect slot with error #");

                EXTEfx.alEffectf(reverbEffect, EXTEfx.AL_REVERB_DECAY_TIME, 8.0f);
                checkAlError("audio-engine", "Failed to create auxiliary reverbEffect slot with error #");

                EXTEfx.alEffectf(reverbEffect, EXTEfx.AL_REVERB_GAIN, 0.02f);
                checkAlError("audio-engine", "Failed to create auxiliary reverbEffect slot with error #");

                EXTEfx.alAuxiliaryEffectSloti(auxiliaryEffectSlot, EXTEfx.AL_EFFECTSLOT_EFFECT, reverbEffect);
                checkAlError("audio-engine", "Failed to create auxiliary reverbEffect slot with error #");
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
            if (synth.isEnabled() && (!synth.isOptIn() || (!synth.isAmbient() && listenerPosition.dst2(synth.getPosition()) > disableRadius2))) {
                //disable synth
                disableSynth(synth);
            } else if (!synth.isEnabled() && (synth.isOptIn() && (synth.isAmbient() || listenerPosition.dst2(synth.getPosition()) < enableRadius2))) {
                //enable synth
                enableSynth(synth);
            } else {
                //synth should stay as it is now
                if (synth.isEnabled())
                    enabledAudioSourceCount++;
            }
        }
    }

    private void detectDisconnectedDevice() throws OpenAlException {
        if (device == 0L) return;
        if (ALC10.alcIsExtensionPresent(device, "ALC_EXT_disconnect")) {
            int[] connected = new int[1];
            alcGetIntegerv(device, EXTDisconnect.ALC_CONNECTED, connected);
            if (connected[0] == ALC_FALSE) {
                System.out.println("OpenAL device disconnected. Reinitializing...");
                shutdownOpenAL();
                initOpenAL();
            }
        }
    }

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
            checkAlcError("audio-engine", String.format("Failed to reset device: %s", device));
        //				printf("Failed to reset device: %s\n", alcGetString(device, alcGetError(device)));
    }

    public void disableSynth(final AudioProducer synth) throws OpenAlException {
        if (synth.isEnabled()) {
            final OpenAlSource source = synth.disable();
            if (source != null) {
                source.pause();
                unusedSources.add(source);
            }
        } else {
            //do nothing
        }
    }

    public void dispose() throws OpenAlException {
        shutdownOpenAL();
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
            checkAlcError("audio-engine", String.format("Failed to reset device: %s", device));
        //				printf("Failed to reset device: %s\n", alcGetString(device, alcGetError(device)));
        queryHrtfEnabled();
    }

    //	MercatorSynthesizerFactory mercatorSynthesizerFactory = new MercatorSynthesizerFactory();
    //	Mp3PlayerFactory mp3PlayerFactory = new Mp3PlayerFactory();

    public void enableSynth(final AudioProducer ap) throws OpenAlException {
        if (ap.isEnabled() || ap.isIgnore()) {
            //do nothing
        } else {
            OpenAlSource source;
            if (!unusedSources.isEmpty()) {
                logger.info("******************** reusing al source");
                //TODO we cannot reuse sources without reconfiguring them, e.g. mono/stereo, ambient,...
                source = unusedSources.remove(unusedSources.size() - 1);
                if (ap instanceof TTSPlayer) {
                    source.reset(samples, bits, distortionEffectSlot, ap);
                } else {
                    source.reset(samples, bits, mainEffectSlot, ap);
                }
                ap.enable(source);
            } else {
                if (numberOfSources < 255) {
                    if (ap instanceof TTSPlayer) {
                        source = new OpenAlSource(samples, bits, distortionEffectSlot, ap);
                    } else {
                        source = new OpenAlSource(samples, bits, mainEffectSlot, ap);
                    }
                    numberOfSources++;
                    ap.enable(source);
                } else {
                    logger.error("Max openal source number (255) reached. Source not created!");
                }
            }
        }
        enabledAudioSourceCount++;
    }

    public void end() {
    }

    private void followDefaultDevice() throws OpenAlException {
        if (alcIsExtensionPresent(0L, "ALC_ENUMERATE_ALL_EXT")) {
            String systemDefault = alcGetString(0L, ALC11.ALC_DEFAULT_ALL_DEVICES_SPECIFIER);

            if (systemDefault != null && !systemDefault.equals(currentDeviceName)) {
                System.out.println("System default device changed to: " + systemDefault);

                shutdownOpenAL();
                initOpenAL();
            }
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

    public int getDisabledAudioSourceCount() {
        return unusedSources.size();
    }

    public Vector3 getListenerPosition() {
        return listenerPosition;
    }

    public int getNumberOfAudioProducers() {
        return synths.size();
    }

    private void initOpenAL() throws OpenAlException {
        String defaultDevice = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
//        List<String> list = ALUtil.getStringList(0, ALC10.ALC_DEVICE_SPECIFIER/*, EnumerateAllExt.ALC_DEFAULT_ALL_DEVICES_SPECIFIER*/);
        device = ALC10.alcOpenDevice((ByteBuffer) null);
        if (device == 0)
            throw new RuntimeException("Failed to find openAL device");

        currentDeviceName = alcGetString(device, EnumerateAllExt.ALC_DEFAULT_ALL_DEVICES_SPECIFIER);//what device did we get?
        logger.info("Using openAL device '" + currentDeviceName + "'");

        final int[] attributes = new int[]{ALC11.ALC_MONO_SOURCES, 1, 0};
        context = ALC10.alcCreateContext(device, attributes);
        if (context == 0L) {
            alcCloseDevice(device);
            throw new RuntimeException("Failed to create OpenAL context");
        }
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


        }

        for (int i = 0; i < attrs.length; ++i) {
            if (attrs[i] == ALC11.ALC_MONO_SOURCES) {
                maxMonoSources = attrs[i + 1];
            }
        }
        setListenerOrientation(new Vector3(0, 0, -1), new Vector3(0, 1, 0));
        createAuxiliaryEffectSlots();
        for (final AudioProducer synth : synths) {
            synth.dispose();
        }
        for (final OpenAlSource source : unusedSources) {
            source.dispose();
        }

    }

    private void queryHrtfEnabled() {
        /* Check if HRTF is enabled, and show which is being used. */
        final int hrtf_state = ALC10.alcGetInteger(device, SOFTHRTF.ALC_HRTF_SOFT);
        if (hrtf_state == 0)
            logger.error("HRTF not enabled!");
        else {
            final String name = alcGetString(device, SOFTHRTF.ALC_HRTF_SPECIFIER_SOFT);
            logger.info(String.format("HRTF enabled, using %s", name));
        }
    }

    public void remove(final AudioProducer audioProducer) {
        synths.remove(audioProducer);
        for (final AbstractSynthesizerFactory factory : factoryMap.values()) {
            if (factory.handles().isInstance(audioProducer)) {
                factory.cacheSynth(audioProducer);
            }
        }
    }

    private void removeAuxiliaryEffectSlot() throws OpenAlException {
        EXTEfx.alDeleteAuxiliaryEffectSlots(mainEffectSlot);
        AudioEngine.checkAlError("audio-engine", "Failed to delete auxiliary effect slot with error #");
        mainEffectSlot = 0;
        EXTEfx.alDeleteAuxiliaryEffectSlots(distortionEffectSlot);
        AudioEngine.checkAlError("audio-engine", "Failed to delete auxiliary effect slot with error #");
        distortionEffectSlot = 0;
    }

    private void setListenerGain(final float gain) throws OpenAlException {
        AL10.alListenerf(AL10.AL_GAIN, gain);
        checkAlError("audio-engine", "Failed to set listener gain with error #");
    }

    private void setListenerOrientation(final Vector3 direction, final Vector3 up) throws OpenAlException {
        final float[] array = new float[]{direction.x, direction.y, direction.z, up.x, up.y, up.z};
        AL10.alListenerfv(AL10.AL_ORIENTATION, array);
        checkAlError("audio-engine", "Failed to set listener orientation with error #");
    }

    private void setListenerPositionAndVelocity(final Vector3 position, final Vector3 velocity) throws OpenAlException {
        AL10.alListener3f(AL10.AL_POSITION, position.x, position.y, position.z);
        checkAlError("audio-engine", "Failed to set listener position with error #");
        AL10.alListener3f(AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);
        checkAlError("audio-engine", "Failed to set listener velocity with error #");
    }

    private void shutdownOpenAL() throws OpenAlException {
        for (final AudioProducer synth : synths) {
            if (synth.isEnabled())
                synth.disable();
            synth.dispose();
        }
        for (final OpenAlSource source : unusedSources) {
            source.dispose();
        }
//        removeAuxiliaryEffectSlot();
        //		AudioEngine.checkAlError("Openal error #");
        {
            ALC10.alcSuspendContext(context);
            checkAlcError("audio-engine", "Openal error #");
        }
        //		AudioEngine.checkAlError("Openal error #");
        {
            final boolean result = ALC10.alcMakeContextCurrent(0);
            checkAlcError(result, "audio-engine", "Openal error #");
        }
        //all calls to AL10.alGetError from this point will fail with #40964 AL_INVALID_OPERATION, as it needs the context to work properly
        if (context != 0L) {
            alcMakeContextCurrent(0L);
            ALC10.alcDestroyContext(context);
            checkAlcError("audio-engine", "Openal error #");
            context = 0L;
        }
        if (device != 0L) {
            final boolean result = alcCloseDevice(device);
            checkAlcError(result, "audio-engine", "Openal error #");
            device = 0L;
        }
    }

    private void updateCamera() throws OpenAlException {
        setListenerOrientation(direction, up);
//        logger.info(String.format("listenerPosition= %f %f %f", listenerPosition.x, listenerPosition.y, listenerPosition.z));
        setListenerPositionAndVelocity(listenerPosition, listenerVelocity);
    }

}
