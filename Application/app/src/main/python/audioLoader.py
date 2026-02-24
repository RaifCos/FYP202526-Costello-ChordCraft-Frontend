import numpy as np
from math import gcd

def loadAudio(audioPath, targetSr=22050):
    # Read WAV manually to avoid any native library dependencies.
    import wave
    with wave.open(audioPath, 'rb') as wf:
        nChannels = wf.getnchannels()
        sampWidth = wf.getsampwidth()
        sr = wf.getframerate()
        rawData = wf.readframes(wf.getnframes())

    # Decode PCM bytes to float32.
    if sampWidth == 1:
        y = (np.frombuffer(rawData, dtype=np.uint8).astype(np.float32) - 128.0) / 128.0
    elif sampWidth == 2:
        y = np.frombuffer(rawData, dtype=np.int16).astype(np.float32) / 32768.0
    elif sampWidth == 4:
        y = np.frombuffer(rawData, dtype=np.int32).astype(np.float32) / 2147483648.0

    # Convert stereo to mono.
    if nChannels > 1:
        y = y.reshape(-1, nChannels).mean(axis=1)

    # Resample using linear interpolation.
    if sr != targetSr:
        originalLength = len(y)
        targetLength = int(originalLength * targetSr / sr)
        y = np.interp(
            np.linspace(0, originalLength - 1, targetLength),
            np.arange(originalLength),
            y
        )
        sr = targetSr

    return y.astype(np.float32), sr