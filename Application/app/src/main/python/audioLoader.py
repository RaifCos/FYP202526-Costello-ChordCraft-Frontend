import numpy as np
import miniaudio
import wave

# Use File Signatures to identify the Audio Format of a given file. 
def getAudioFormat(audioPath):
    with open(audioPath, 'rb') as f:
        header = f.read(12)
    if header[:4] == b'RIFF' and header[8:12] == b'WAVE':
        return 'wav'
    elif header[:3] == b'ID3' or header[:2] == b'\xff\xfb' or header[:2] == b'\xff\xf3' or header[:2] == b'\xff\xf2':
        return 'mp3'
    else:
        return 'unknown'

# Load given Audio File for ACR Model. 
def loadAudio(audioPath, targetSr=22050):
    audioFormat = getAudioFormat(audioPath)

    # Load MP3 Audio using miniaudio.
    if audioFormat == 'mp3':
        decoded = miniaudio.mp3_read_file_f32(audioPath)
        y = np.array(decoded.samples, dtype=np.float32)
        sr = decoded.sample_rate
        nChannels = decoded.nchannels
        if nChannels > 1:
            y = y.reshape(-1, nChannels).mean(axis=1)

    # Load WAV Audio using wave.
    elif audioFormat == 'wav':
        with wave.open(audioPath, 'rb') as wf:
            nChannels = wf.getnchannels()
            sampWidth = wf.getsampwidth()
            sr = wf.getframerate()
            rawData = wf.readframes(wf.getnframes())
        if sampWidth == 1:
            y = (np.frombuffer(rawData, dtype=np.uint8).astype(np.float32) - 128.0) / 128.0
        elif sampWidth == 2:
            y = np.frombuffer(rawData, dtype=np.int16).astype(np.float32) / 32768.0
        elif sampWidth == 4:
            y = np.frombuffer(rawData, dtype=np.int32).astype(np.float32) / 2147483648.0

    else:
        raise ValueError(f"{audioPath} is not in a supported audio format.")

    # Resample using Linear Interpolation.
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