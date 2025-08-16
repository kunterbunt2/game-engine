from TTS.api import TTS
import sys

# Arguments from Java: text to speak and output file
text = sys.argv[1]
output_file = sys.argv[2]

# Load a TTS model (GPU will be used automatically)
tts = TTS(model_name="tts_models/en/ljspeech/tacotron2-DDC")
tts.tts_to_file(text=text, file_path=output_file)
