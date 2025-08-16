# CoquiTTS JEP Integration Setup

## Prerequisites

1. **Python Installation**: Ensure Python 3.8+ is installed and accessible from your system PATH
2. **JEP Native Library**: JEP requires native libraries to be properly configured

## Python Environment Setup

1. Install the required Python packages:
```bash
pip install -r requirements.txt
```

2. Or install individually:
```bash
pip install TTS torch numpy scipy librosa soundfile
```

## JEP Configuration

### Option 1: Set JAVA_HOME and Python Path
```bash
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%PATH%;C:\Python311;C:\Python311\Scripts
```

### Option 2: JEP Configuration (if needed)
You may need to configure JEP's native library path. Add to your JVM arguments:
```
-Djava.library.path="path/to/jep/native/libs"
```

## Usage Example

```java
CoquiTTS tts = new CoquiTTS();
tts.execute(); // This will generate "output.wav" file
```

## Troubleshooting

1. **ModuleNotFoundError**: Ensure TTS is installed in the Python environment that JEP is using
2. **Native Library Issues**: Make sure JEP's native libraries match your Python version
3. **CUDA Support**: For GPU acceleration, install PyTorch with CUDA support

## Advanced Configuration

You can extend the CoquiTTS class to:
- Use different TTS models
- Customize voice parameters
- Handle different output formats
- Implement error recovery mechanisms
