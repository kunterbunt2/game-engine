import logging
import numpy as np
import os
import parselmouth
import pyworld as pw
import soundfile as sf
import sys
import tempfile
from TTS.api import TTS
from TTS.utils.manage import ModelManager
from flask import Flask, request, send_file, jsonify
from io import BytesIO
from scipy import signal

app = Flask(__name__)


# Function to print to stderr
def eprint(*args, **kwargs):
    print(*args, file=sys.stderr, **kwargs)


# Configure logging to ensure output appears in container logs
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)

# Force Python to flush stdout/stderr immediately
sys.stdout.reconfigure(line_buffering=True)
sys.stderr.reconfigure(line_buffering=True)

# Initialize TTS model globally
tts = None
current_model = None
current_vocoder = None


def initialize_tts(model_name="tts_models/en/vctk/vits", vocoder_name=None, gpu=None):
    print("------------------------------------------------------------")
    """Initialize TTS model with error handling"""
    global tts, current_model, current_vocoder
    try:
        print("List models")
        # Path to where models are stored (Coqui uses ~/.local/share/tts by default)
        manager = ModelManager()
        models = manager.list_models()
        print(f"Available models: {len(models)} models found")
        print(f"Loading TTS model: {model_name}")
        if vocoder_name:
            print(f"Loading vocoder: {vocoder_name}")

        # Try GPU first if not specified, fallback to CPU if GPU not available
        use_gpu = gpu if gpu is not None else True

        try:
            # In newer versions of Coqui TTS, vocoder is specified differently
            if vocoder_name:
                # Try the newer API first - vocoder as separate parameter
                try:
                    tts = TTS(model_name=model_name, gpu=use_gpu)
                    # Set vocoder after initialization if the model supports it
                    if hasattr(tts, 'load_vocoder'):
                        tts.load_vocoder(vocoder_name)
                    elif hasattr(tts, 'vocoder'):
                        # Some versions allow setting vocoder directly
                        from TTS.vocoder.utils.generic_utils import setup_vocoder
                        tts.vocoder = setup_vocoder(vocoder_name)
                    else:
                        print(f"Warning: Vocoder loading not supported in this TTS version")
                except Exception as vocoder_error:
                    print(f"Failed to load separate vocoder: {vocoder_error}")
                    # Fallback: try loading without vocoder
                    print("Falling back to model without separate vocoder")
                    tts = TTS(model_name=model_name, gpu=use_gpu)
                    vocoder_name = None  # Clear vocoder name since it failed
            else:
                tts = TTS(model_name=model_name, gpu=use_gpu)

            current_model = model_name
            current_vocoder = vocoder_name
            print(f"TTS model loaded successfully on {'GPU' if use_gpu else 'CPU'}")

        except Exception as gpu_error:
            if use_gpu and gpu is None:  # Only retry with CPU if GPU was auto-selected
                eprint(f"GPU initialization failed: {gpu_error}, falling back to CPU")
                # Retry with CPU
                if vocoder_name:
                    try:
                        tts = TTS(model_name=model_name, gpu=False)
                        if hasattr(tts, 'load_vocoder'):
                            tts.load_vocoder(vocoder_name)
                        elif hasattr(tts, 'vocoder'):
                            from TTS.vocoder.utils.generic_utils import setup_vocoder
                            tts.vocoder = setup_vocoder(vocoder_name)
                        else:
                            print(f"Warning: Vocoder loading not supported, using model without vocoder")
                            vocoder_name = None
                    except Exception as cpu_vocoder_error:
                        print(f"CPU vocoder loading failed: {cpu_vocoder_error}")
                        tts = TTS(model_name=model_name, gpu=False)
                        vocoder_name = None
                else:
                    tts = TTS(model_name=model_name, gpu=False)

                current_model = model_name
                current_vocoder = vocoder_name
                eprint("TTS model loaded successfully on CPU")
            else:
                raise gpu_error

    except Exception as e:
        eprint(f"Failed to load TTS model: {e}")
        raise


def shift_formants(
        input_path: str,
        output_path: str,
        formant_factor: float = 1.3,
        new_pitch_median: float = 0.0,
        pitch_range_factor: float = 1.0,
        duration_factor: float = 1.0,
        pitch_floor: float = 75.0,
        pitch_ceiling: float = 600.0
):
    snd = parselmouth.Sound(input_path)
    manipulated = parselmouth.praat.call(
        snd,
        "Change gender...",
        pitch_floor,  # Minimum pitch in Hz
        pitch_ceiling,  # Maximum pitch in Hz
        formant_factor,  # Raise formants if >1.0 (toy effect), <1.0 for deeper
        new_pitch_median,  # 0.0 = unchanged median pitch
        pitch_range_factor,  # 1.0 = unchanged pitch range
        duration_factor  # 1.0 = unchanged duration
    )
    manipulated.save(output_path, "WAV")
    return manipulated


def shift_formants_memory(
        audio_data: np.ndarray,
        sample_rate: float,
        formant_factor: float = 1.3,
        new_pitch_median: float = 0.0,
        pitch_range_factor: float = 1.0,
        duration_factor: float = 1.0,
        pitch_floor: float = 75.0,
        pitch_ceiling: float = 600.0
):
    """
    Apply formant shifting to audio data in memory using parselmouth.

    Args:
        audio_data: numpy array of audio samples
        sample_rate: sampling rate of the audio
        formant_factor: >1.0 raises formants (child/toy voice), <1.0 lowers (deep voice)
        new_pitch_median: 0.0 keeps original median; otherwise override in Hz
        pitch_range_factor: 1.0 keeps natural variability; 0 flattens pitch contour
        duration_factor: 1.0 keeps timing; altering changes speed
        pitch_floor: Lower limit for pitch analysis (e.g. 75 Hz)
        pitch_ceiling: Upper limit for pitch analysis (e.g. 600 Hz)

    Returns:
        numpy array of processed audio data
    """
    try:
        print(f"Applying formant shift in memory: factor={formant_factor}")

        # Create parselmouth Sound object from numpy array
        # Ensure audio_data is float64 as required by parselmouth
        if audio_data.dtype != np.float64:
            audio_data = audio_data.astype(np.float64)

        # Create Sound object from array
        snd = parselmouth.Sound(audio_data, sampling_frequency=sample_rate)

        # Apply formant manipulation
        manipulated = parselmouth.praat.call(
            snd,
            "Change gender...",
            pitch_floor,
            pitch_ceiling,
            formant_factor,
            new_pitch_median,
            pitch_range_factor,
            duration_factor
        )

        # Extract the modified audio as numpy array
        modified_audio = manipulated.values[0]  # parselmouth returns 2D array, we want 1D

        print(f"Formant shift completed: input shape {audio_data.shape}, output shape {modified_audio.shape}")
        return modified_audio

    except Exception as e:
        print(f"Error in formant shifting: {e}")
        return audio_data  # Return original data if processing fails


def pitch_shift_audio(data, pitch_shift_factor):
    """Simple pitch shifting using time-domain techniques"""
    try:
        # Use scipy's resample for basic pitch shifting
        new_length = int(len(data) / pitch_shift_factor)
        shifted_data = signal.resample(data, new_length)

        # Pad or trim to match original duration for speed consistency
        if len(shifted_data) < len(data):
            # Pad with zeros
            padded_data = np.zeros(len(data))
            padded_data[:len(shifted_data)] = shifted_data
            return padded_data
        else:
            # Trim to original length
            return shifted_data[:len(data)]

    except Exception as e:
        print(f"Error in pitch shifting: {e}")
        return data


def apply_minion_voice_effects_memory(
        audio_data: np.ndarray,
        sample_rate: float,
        pitch_shift=1.5,
        speed_factor=1.2,
        formant_shift=1.3
):
    """
    Apply minion-like voice effects to audio data in memory.

    Args:
        audio_data: numpy array of audio samples
        sample_rate: sampling rate of the audio
        pitch_shift: Factor to shift pitch (1.5 = 50% higher pitch)
        speed_factor: Factor to change speed (1.2 = 20% faster)
        formant_shift: Factor to shift formants (1.3 = higher formants)

    Returns:
        tuple: (processed_audio_data, sample_rate)
    """
    try:
        print(f"Applying minion voice effects in memory")
        print(f"Pitch shift: {pitch_shift}, Speed: {speed_factor}, Formant: {formant_shift}")
        print(f"Input audio: {len(audio_data)} samples at {sample_rate} Hz")

        # Work with a copy to avoid modifying original
        data = audio_data.copy()

        # Convert to mono if stereo
        if len(data.shape) > 1:
            data = np.mean(data, axis=1)
            print(f"Converted stereo to mono: {data.shape}")

        # Apply formant shifting first (using parselmouth)
        if formant_shift != 1.0:
            data = shift_formants_memory(
                data,
                sample_rate,
                formant_factor=formant_shift
            )
            print(f"Applied formant shift: {formant_shift}")

        # Apply pitch shifting using phase vocoder technique
        if pitch_shift != 1.0:
            data = pitch_shift_audio(data, pitch_shift)
            print(f"Applied pitch shift: {pitch_shift}")

        # Apply speed change (time stretching)
        if speed_factor != 1.0:
            # Simple time stretching by resampling
            new_length = int(len(data) / speed_factor)
            data = signal.resample(data, new_length)
            print(f"Applied speed change: {speed_factor}, new length: {len(data)}")

        # Normalize to prevent clipping
        if np.max(np.abs(data)) > 0:  # Avoid division by zero
            data = data / np.max(np.abs(data)) * 0.95
            print("Applied normalization")

        print(f"Minion voice effects applied in memory. Output: {len(data)} samples")
        return data, sample_rate

    except Exception as e:
        print(f"Error applying minion effects in memory: {e}")
        return audio_data, sample_rate  # Return original data if processing fails


def apply_minion_voice_effects(audio_file_path, pitch_shift=1.5, speed_factor=1.2, formant_shift=1.3):
    """
    Apply minion-like voice effects to an audio file (original file-based version).
    """
    try:
        print(f"Applying minion voice effects to: {audio_file_path}")
        print(f"Pitch shift: {pitch_shift}, Speed: {speed_factor}, Formant: {formant_shift}")

        # Apply formant shifting (simple spectral envelope modification)
        if formant_shift != 1.0:
            shift_formants(audio_file_path, audio_file_path, formant_factor=formant_shift)
            print(f"Applied formant shift: {formant_shift}")

        # Read the audio file
        data, sample_rate = sf.read(audio_file_path)
        print(f"Original audio: {len(data)} samples at {sample_rate} Hz")

        # Convert to mono if stereo
        if len(data.shape) > 1:
            data = np.mean(data, axis=1)

        # Apply pitch shifting using phase vocoder technique
        if pitch_shift != 1.0:
            data = pitch_shift_audio(data, pitch_shift)
            print(f"Applied pitch shift: {pitch_shift}")

        # Apply speed change (time stretching)
        if speed_factor != 1.0:
            # Simple time stretching by resampling
            new_length = int(len(data) / speed_factor)
            data = signal.resample(data, new_length)
            print(f"Applied speed change: {speed_factor}")

        # Normalize to prevent clipping
        data = data / np.max(np.abs(data)) * 0.95

        # Create output file
        output_path = audio_file_path.replace('.wav', '_minion.wav')
        sf.write(output_path, data, sample_rate)
        print(f"Minion voice effect applied, saved to: {output_path}")

        return output_path

    except Exception as e:
        print(f"Error applying minion effects: {e}")
        return audio_file_path  # Return original file if processing fails


@app.route("/health", methods=["GET"])
def health_check():
    print("------------------------------------------------------------")
    """Health check endpoint"""
    return jsonify({
        "status": "healthy",
        "model_loaded": tts is not None,
        "current_model": current_model,
        "current_vocoder": current_vocoder
    })


@app.route("/speak", methods=["POST"])
def speak():
    print("------------------------------------------------------------")
    """Generate speech from text"""
    print("Generate speech from text")
    try:
        if tts is None:
            return jsonify({"error": "TTS model not initialized"}), 500

        # Get text from request
        if not request.json or "text" not in request.json:
            return jsonify({"error": "Missing 'text' field in JSON request"}), 400

        text = request.json["text"]
        if not text.strip():
            return jsonify({"error": "Text cannot be empty"}), 400

        # Get optional parameters
        speaker = request.json.get("speaker")  # For multi-speaker models
        language = request.json.get("language")  # For multi-language models

        print(f"Received request with text: '{text[:50]}...' (length: {len(text)} chars)")
        print(f"Speaker: {speaker}")
        print(f"Language: {language}")

        # Create temporary file for audio output
        tmp = tempfile.NamedTemporaryFile(delete=False, suffix=".wav")
        print(f"Created temporary file: {tmp.name}")

        # Generate speech with optional parameters
        kwargs = {"text": text, "file_path": tmp.name}
        if speaker:
            kwargs["speaker"] = speaker
        if language:
            kwargs["language"] = language

        print(f"Calling tts.tts_to_file with parameters: {kwargs}")
        tts.tts_to_file(**kwargs)
        print(f"TTS generation completed successfully")

        # Check file size to verify generation worked
        file_size = os.path.getsize(tmp.name)
        print(f"Generated audio file size: {file_size} bytes")

        print("------------------------------------------------------------")
        # Return audio file
        return send_file(tmp.name, mimetype="audio/wav", as_attachment=True, download_name="speech.wav")

    except Exception as e:
        eprint(f"Error generating speech: {e}")
        import traceback
        eprint(f"Traceback: {traceback.format_exc()}")
        return jsonify({"error": str(e)}), 500


@app.route("/speak_minion", methods=["POST"])
def speak_minion():
    """Generate speech from text with minion-like voice effects (original file-based version)"""
    print("------------------------------------------------------------")
    print("Generate minion speech from text")
    try:
        if tts is None:
            return jsonify({"error": "TTS model not initialized"}), 500

        # Get text from request
        if not request.json or "text" not in request.json:
            return jsonify({"error": "Missing 'text' field in JSON request"}), 400

        text = request.json["text"]
        if not text.strip():
            return jsonify({"error": "Text cannot be empty"}), 400

        # Get optional parameters
        speaker = request.json.get("speaker")
        language = request.json.get("language")

        # Get minion effect parameters with defaults
        pitch_shift = request.json.get("pitch_shift", 1.5)  # Higher pitch
        speed_factor = request.json.get("speed_factor", 1.2)  # Faster speech
        formant_shift = request.json.get("formant_shift", 1.3)  # Higher formants

        print(f"Received minion request with text: '{text[:50]}...' (length: {len(text)} chars)")
        print(f"Speaker: {speaker}, Language: {language}")
        print(f"Minion effects - Pitch: {pitch_shift}, Speed: {speed_factor}, Formant: {formant_shift}")

        # Create temporary file for audio output
        tmp = tempfile.NamedTemporaryFile(delete=False, suffix=".wav")
        print(f"Created temporary file: {tmp.name}")

        # Generate speech with optional parameters
        kwargs = {"text": text, "file_path": tmp.name}
        if speaker:
            kwargs["speaker"] = speaker
        if language:
            kwargs["language"] = language

        print(f"Calling tts.tts_to_file with parameters: {kwargs}")
        tts.tts_to_file(**kwargs)
        print(f"TTS generation completed successfully")

        # Apply minion voice effects
        minion_file = apply_minion_voice_effects(
            tmp.name,
            pitch_shift=pitch_shift,
            speed_factor=speed_factor,
            formant_shift=formant_shift
        )

        # Check processed file size
        file_size = os.path.getsize(minion_file)
        print(f"Generated minion audio file size: {file_size} bytes")

        print("------------------------------------------------------------")
        # Return processed audio file
        return send_file(minion_file, mimetype="audio/wav", as_attachment=True, download_name="minion_speech.wav")

    except Exception as e:
        eprint(f"Error generating minion speech: {e}")
        import traceback
        eprint(f"Traceback: {traceback.format_exc()}")
        return jsonify({"error": str(e)}), 500


@app.route("/speak_minion_memory", methods=["POST"])
def speak_minion_memory():
    """Generate speech from text with minion-like voice effects - PURE IN-MEMORY VERSION"""
    print("------------------------------------------------------------")
    print("Generate minion speech from text - IN MEMORY")
    try:
        if tts is None:
            return jsonify({"error": "TTS model not initialized"}), 500

        # Get text from request
        if not request.json or "text" not in request.json:
            return jsonify({"error": "Missing 'text' field in JSON request"}), 400

        text = request.json["text"]
        if not text.strip():
            return jsonify({"error": "Text cannot be empty"}), 400

        # Get optional parameters
        speaker = request.json.get("speaker")
        language = request.json.get("language")

        # Get minion effect parameters with defaults
        pitch_shift = request.json.get("pitch_shift", 1.5)  # Higher pitch
        speed_factor = request.json.get("speed_factor", 1.2)  # Faster speech
        formant_shift = request.json.get("formant_shift", 1.3)  # Higher formants

        print(f"Received minion request with text: '{text[:50]}...' (length: {len(text)} chars)")
        print(f"Speaker: {speaker}, Language: {language}")
        print(f"Minion effects - Pitch: {pitch_shift}, Speed: {speed_factor}, Formant: {formant_shift}")

        # STEP 1: Generate speech directly to memory using tts.tts() instead of tts.tts_to_file()
        print("Step 1: Generating speech in memory...")
        kwargs = {"text": text}
        if speaker:
            kwargs["speaker"] = speaker
        if language:
            kwargs["language"] = language

        print(f"Calling tts.tts with parameters: {kwargs}")
        wav_data = tts.tts(**kwargs)
        print(f"TTS generation completed successfully - got {len(wav_data)} samples")

        # Get the sample rate from the synthesizer
        sample_rate = tts.synthesizer.output_sample_rate if hasattr(tts.synthesizer, 'output_sample_rate') else 22050
        print(f"Using sample rate: {sample_rate} Hz")

        # STEP 2: Apply minion voice effects in memory
        print("Step 2: Applying minion voice effects in memory...")
        processed_wav, final_sample_rate = apply_minion_voice_effects_memory(
            wav_data,
            sample_rate,
            pitch_shift=pitch_shift,
            speed_factor=speed_factor,
            formant_shift=formant_shift
        )
        print(f"Minion effects applied - final audio: {len(processed_wav)} samples at {final_sample_rate} Hz")

        # STEP 3: Create BytesIO stream and write WAV data
        print("Step 3: Creating BytesIO stream...")
        audio_buffer = BytesIO()
        sf.write(audio_buffer, processed_wav, final_sample_rate, format='WAV')
        audio_buffer.seek(0)  # Reset pointer to beginning

        buffer_size = audio_buffer.getbuffer().nbytes
        print(f"Created audio buffer: {buffer_size} bytes")

        print("------------------------------------------------------------")
        # STEP 4: Return the BytesIO stream directly using send_file
        return send_file(
            audio_buffer,
            mimetype="audio/wav",
            as_attachment=True,
            download_name="minion_speech_memory.wav"
        )

    except Exception as e:
        eprint(f"Error generating minion speech in memory: {e}")
        import traceback
        eprint(f"Traceback: {traceback.format_exc()}")
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    # Initialize TTS model on startup
    initialize_tts()

    # Start Flask server
    print("Starting TTS server on port 5000...")
    app.run(host="0.0.0.0", port=5000, debug=False)
