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


def initialize_tts():
    print("------------------------------------------------------------")
    """Initialize TTS model with error handling"""
    global tts
    try:
        print("List models")
        # Path to where models are stored (Coqui uses ~/.local/share/tts by default)
        manager = ModelManager()
        models = manager.list_models()
        print(models)
        print("Loading TTS model...")
        # Try GPU first, fallback to CPU if GPU not available
        try:
            tts = TTS(model_name="tts_models/en/ljspeech/tacotron2-DDC", gpu=True)
            print("TTS model loaded successfully on GPU")
        except Exception as gpu_error:
            eprint(f"GPU initialization failed: {gpu_error}, falling back to CPU")
            tts = TTS(model_name="tts_models/en/ljspeech/tacotron2-DDC", gpu=False)
            eprint("TTS model loaded successfully on CPU")
    except Exception as e:
        eprint(f"Failed to load TTS model: {e}")
        raise


@app.route("/health", methods=["GET"])
def health_check():
    print("------------------------------------------------------------")
    """Health check endpoint"""
    return jsonify({"status": "healthy", "model_loaded": tts is not None})


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

        # Get optional sample rate parameter (default to 22050 for better quality)
        sample_rate = request.json.get("sample_rate", 22050)

        print(f"Received request with text: '{text[:50]}...' (length: {len(text)} chars)")
        print(f"Sample rate parameter: {sample_rate} (type: {type(sample_rate)})")
        print(f"Full request JSON: {request.json}")

        # Create temporary file for audio output
        tmp = tempfile.NamedTemporaryFile(delete=False, suffix=".wav")
        print(f"Created temporary file: {tmp.name}")

        # Generate speech with specified sample rate
        print(f"Calling tts.tts_to_file with sample_rate={sample_rate}")

        # Let's also check what the TTS object's current configuration is
        print(f"TTS synthesizer sample rate: {getattr(tts.synthesizer, 'output_sample_rate', 'Not available')}")
        print(f"TTS synthesizer audio config: {getattr(tts.synthesizer, 'ap', 'Not available')}")

        tts.tts_to_file(text=text, file_path=tmp.name, sample_rate=sample_rate)
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
        models = TTS.list_models()
        return jsonify({"models": models})
    except Exception as e:
        logger.error(f"Error listing models: {e}")
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    # Initialize TTS model on startup
    initialize_tts()

    # Start Flask server
    print("Starting TTS server on port 5000...")
    app.run(host="0.0.0.0", port=5000, debug=False)
