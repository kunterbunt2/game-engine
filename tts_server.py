import logging
import os
import sys
import tempfile

from TTS.api import TTS
from TTS.utils.manage import ModelManager
from flask import Flask, request, send_file, jsonify

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
