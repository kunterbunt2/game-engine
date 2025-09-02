import logging
import numpy as np
import os
import soundfile as sf
import sys
import tempfile
from TTS.api import TTS
from TTS.utils.manage import ModelManager
from flask import Flask, request, send_file, jsonify
from scipy import signal
from scipy.io import wavfile

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


def apply_minion_voice_effects(audio_file_path, pitch_shift=1.5, speed_factor=1.2, formant_shift=1.3):
    """
    Apply minion-like voice effects to an audio file.

    Args:
        audio_file_path: Path to the input WAV file
        pitch_shift: Factor to shift pitch (1.5 = 50% higher pitch)
        speed_factor: Factor to change speed (1.2 = 20% faster)
        formant_shift: Factor to shift formants (1.3 = higher formants)

    Returns:
        Path to the processed audio file
    """
    try:
        print(f"Applying minion voice effects to: {audio_file_path}")
        print(f"Pitch shift: {pitch_shift}, Speed: {speed_factor}, Formant: {formant_shift}")

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

        # Apply formant shifting (simple spectral envelope modification)
        if formant_shift != 1.0:
            data = apply_formant_shift(data, sample_rate, formant_shift)
            print(f"Applied formant shift: {formant_shift}")

        # Add slight chipmunk-like harmonics
        data = add_chipmunk_harmonics(data, sample_rate)

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


def apply_formant_shift(data, sample_rate, formant_factor):
    """Apply formant shifting using spectral envelope modification"""
    try:
        # Simple formant shifting using frequency domain manipulation
        fft_data = np.fft.fft(data)
        freqs = np.fft.fftfreq(len(data), 1 / sample_rate)

        # Shift the spectral envelope
        shifted_fft = np.zeros_like(fft_data)
        for i, freq in enumerate(freqs):
            if freq > 0:
                new_idx = int(i / formant_factor)
                if new_idx < len(shifted_fft):
                    shifted_fft[new_idx] += fft_data[i]

        # Convert back to time domain
        modified_data = np.real(np.fft.ifft(shifted_fft))
        return modified_data

    except Exception as e:
        print(f"Error in formant shifting: {e}")
        return data


def add_chipmunk_harmonics(data, sample_rate, harmonic_strength=0.1):
    """Add subtle harmonic content to make voice more chipmunk-like"""
    try:
        # Generate harmonic content
        t = np.arange(len(data)) / sample_rate

        # Add second harmonic with reduced amplitude
        harmonic = data * harmonic_strength * np.sin(2 * np.pi * 800 * t)

        # Mix with original
        mixed_data = data + harmonic

        return mixed_data

    except Exception as e:
        print(f"Error adding harmonics: {e}")
        return data


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


@app.route("/load_model", methods=["POST"])
def load_model():
    """Load a specific TTS model and optionally vocoder"""
    print("------------------------------------------------------------")
    print("Loading new TTS model")
    try:
        if not request.json:
            return jsonify({"error": "JSON request body required"}), 400

        model_name = request.json.get("model_name")
        if not model_name:
            return jsonify({"error": "Missing 'model_name' field in JSON request"}), 400

        vocoder_name = request.json.get("vocoder_name")
        gpu = request.json.get("gpu")  # Can be True, False, or None for auto

        print(f"Loading model: {model_name}")
        if vocoder_name:
            print(f"Loading vocoder: {vocoder_name}")

        # Initialize the new model
        initialize_tts(model_name, vocoder_name, gpu)

        return jsonify({
            "status": "success",
            "message": f"Model '{model_name}' loaded successfully",
            "current_model": current_model,
            "current_vocoder": current_vocoder
        })

    except Exception as e:
        eprint(f"Error loading model: {e}")
        return jsonify({"error": str(e)}), 500


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
        print(f"Using model: {current_model}")
        print(f"Using vocoder: {current_vocoder}")

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

        # Let's also check the actual sample rate of the generated file
        try:
            import wave
            with wave.open(tmp.name, 'rb') as wav_file:
                actual_sample_rate = wav_file.getframerate()
                channels = wav_file.getnchannels()
                frames = wav_file.getnframes()
                duration = frames / actual_sample_rate
                print(f"Actual WAV file sample rate: {actual_sample_rate} Hz")
                print(f"Channels: {channels}, Frames: {frames}, Duration: {duration:.2f}s")
        except Exception as wav_error:
            print(f"Could not read WAV file info: {wav_error}")

        print("------------------------------------------------------------")
        # Return audio file
        return send_file(tmp.name, mimetype="audio/wav", as_attachment=True, download_name="speech.wav")

    except Exception as e:
        eprint(f"Error generating speech: {e}")
        eprint(f"Exception type: {type(e)}")
        import traceback
        eprint(f"Traceback: {traceback.format_exc()}")
        return jsonify({"error": str(e)}), 500


@app.route("/speak_minion", methods=["POST"])
def speak_minion():
    """Generate speech from text with minion-like voice effects"""
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
        eprint(f"Exception type: {type(e)}")
        import traceback
        eprint(f"Traceback: {traceback.format_exc()}")
        return jsonify({"error": str(e)}), 500


@app.route("/models", methods=["GET"])
def list_models():
    """List available TTS models"""
    try:
        # Use the ModelManager to list models, not the TTS class
        manager = ModelManager()
        models = manager.list_models()

        # Create a pretty-formatted response
        response = app.response_class(
            response=jsonify({"models": models}).get_data(as_text=True),
            status=200,
            mimetype='application/json'
        )

        # Use Flask's jsonify with pretty printing
        import json
        formatted_response = json.dumps({"models": models}, indent=2, sort_keys=True)

        return app.response_class(
            response=formatted_response,
            status=200,
            mimetype='application/json'
        )

    except Exception as e:
        logger.error(f"Error listing models: {e}")
        error_response = json.dumps({"error": str(e)}, indent=2)
        return app.response_class(
            response=error_response,
            status=500,
            mimetype='application/json'
        )


@app.route("/speakers", methods=["GET"])
def list_speakers():
    """List available speakers for the current TTS model"""
    try:
        if tts is None:
            return jsonify({"error": "TTS model not initialized"}), 500

        # Check if the current model supports multiple speakers
        speakers = []
        is_multi_speaker = False

        try:
            # Try to access speaker information from the TTS model
            if hasattr(tts, 'synthesizer') and hasattr(tts.synthesizer, 'tts_model'):
                model = tts.synthesizer.tts_model

                # Check for speaker manager or speaker embeddings
                if hasattr(model, 'speaker_manager') and model.speaker_manager is not None:
                    is_multi_speaker = True
                    if hasattr(model.speaker_manager, 'speaker_names'):
                        speakers = list(model.speaker_manager.speaker_names)
                    elif hasattr(model.speaker_manager, 'speakers'):
                        speakers = list(model.speaker_manager.speakers.keys())
                elif hasattr(model, 'speaker_embeddings') and model.speaker_embeddings is not None:
                    is_multi_speaker = True
                    speakers = list(range(len(model.speaker_embeddings)))
                    speakers = [f"speaker_{i}" for i in speakers]  # Convert to string format

        except Exception as e:
            print(f"Error checking speaker info: {e}")

        # Use Flask's JSON formatting with pretty printing
        import json
        response_data = {
            "current_model": current_model,
            "is_multi_speaker": is_multi_speaker,
            "speakers": speakers,
            "speaker_count": len(speakers)
        }

        formatted_response = json.dumps(response_data, indent=2, sort_keys=True)

        return app.response_class(
            response=formatted_response,
            status=200,
            mimetype='application/json'
        )

    except Exception as e:
        logger.error(f"Error listing speakers: {e}")
        import json
        error_response = json.dumps({"error": str(e)}, indent=2)
        return app.response_class(
            response=error_response,
            status=500,
            mimetype='application/json'
        )


@app.route("/languages", methods=["GET"])
def list_languages():
    """List available languages for the current TTS model"""
    try:
        if tts is None:
            return jsonify({"error": "TTS model not initialized"}), 500

        # Check if the current model supports multiple languages
        languages = []
        is_multi_lingual = False

        try:
            # Try to access language information from the TTS model
            if hasattr(tts, 'synthesizer') and hasattr(tts.synthesizer, 'tts_model'):
                model = tts.synthesizer.tts_model

                # Check for language manager or language embeddings
                if hasattr(model, 'language_manager') and model.language_manager is not None:
                    is_multi_lingual = True
                    if hasattr(model.language_manager, 'language_names'):
                        languages = list(model.language_manager.language_names)
                    elif hasattr(model.language_manager, 'languages'):
                        languages = list(model.language_manager.languages.keys())
                elif hasattr(model, 'language_embeddings') and model.language_embeddings is not None:
                    is_multi_lingual = True
                    languages = list(range(len(model.language_embeddings)))
                    languages = [f"lang_{i}" for i in languages]  # Convert to string format
                elif hasattr(model, 'args') and hasattr(model.args, 'language'):
                    # Some models have a single language specified in args
                    if model.args.language:
                        languages = [model.args.language]
                        is_multi_lingual = False

        except Exception as e:
            print(f"Error checking language info: {e}")

        # Use Flask's JSON formatting with pretty printing
        import json
        response_data = {
            "current_model": current_model,
            "is_multi_lingual": is_multi_lingual,
            "languages": languages,
            "language_count": len(languages)
        }

        formatted_response = json.dumps(response_data, indent=2, sort_keys=True)

        return app.response_class(
            response=formatted_response,
            status=200,
            mimetype='application/json'
        )

    except Exception as e:
        logger.error(f"Error listing languages: {e}")
        import json
        error_response = json.dumps({"error": str(e)}, indent=2)
        return app.response_class(
            response=error_response,
            status=500,
            mimetype='application/json'
        )


@app.route("/vocoders", methods=["GET"])
def list_vocoders():
    """List available vocoder models"""
    try:
        # Use the ModelManager to list vocoder models
        manager = ModelManager()
        models = manager.list_models()

        # Filter models to get only vocoders
        vocoders = [model for model in models if "vocoder" in model.lower()]

        # Use Flask's JSON formatting with pretty printing
        import json
        response_data = {
            "vocoders": vocoders,
            "vocoder_count": len(vocoders)
        }

        formatted_response = json.dumps(response_data, indent=2, sort_keys=True)

        return app.response_class(
            response=formatted_response,
            status=200,
            mimetype='application/json'
        )

    except Exception as e:
        logger.error(f"Error listing vocoders: {e}")
        import json
        error_response = json.dumps({"error": str(e)}, indent=2)
        return app.response_class(
            response=error_response,
            status=500,
            mimetype='application/json'
        )


if __name__ == "__main__":
    # Initialize TTS model on startup
    initialize_tts()

    # Start Flask server
    print("Starting TTS server on port 5000...")
    app.run(host="0.0.0.0", port=5000, debug=False)
