package com.example.chordcraft.components

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext

import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope

@Composable
fun filePickerLauncher(selectedFileUri: MutableState<Uri?>): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val mimeType = context.contentResolver.getType(uri)

            if (mimeType != "audio/mpeg" && mimeType != "audio/wav" && mimeType != "audio/x-wav") {
                Toast.makeText(context, "Please select an MP3 or WAV file.", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }

            if (mimeType == "audio/mpeg") {
                CoroutineScope(Dispatchers.IO).launch {
                    val wavFile = convertToWAV(context, uri)
                    withContext(Dispatchers.Main) {
                        if (wavFile != null) {
                            selectedFileUri.value = Uri.fromFile(wavFile)
                            Toast.makeText(context, "Converted to WAV: ${wavFile.name}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Conversion failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                selectedFileUri.value = uri
                Toast.makeText(context, "File selected: $uri", Toast.LENGTH_SHORT).show()
            }
        }
    }

    return { launcher.launch("audio/mpeg,audio/wav,audio/x-wav") }
}

/**
 * Decodes an MP3 [Uri] to raw PCM using [MediaCodec], then writes a
 * properly-headered WAV file to the app cache directory.
 * Returns the output [File] on success, or null on failure.
 */
fun convertToWAV(context: Context, mp3Uri: Uri): File? {
    val outputFile = File(context.cacheDir, "converted_${System.currentTimeMillis()}.wav")

    return try {
        val extractor = MediaExtractor()
        context.contentResolver.openFileDescriptor(mp3Uri, "r")?.use { pfd ->
            extractor.setDataSource(pfd.fileDescriptor)
        } ?: return null

        // Find the audio track
        var trackIndex = -1
        var inputFormat: MediaFormat? = null
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                trackIndex = i
                inputFormat = format
                break
            }
        }
        if (trackIndex < 0 || inputFormat == null) {
            extractor.release()
            return null
        }
        extractor.selectTrack(trackIndex)

        val mime = inputFormat.getString(MediaFormat.KEY_MIME)!!
        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(inputFormat, null, null, 0)
        codec.start()

        val pcmData = mutableListOf<Byte>()
        var sampleRate = 44100
        var channelCount = 2
        val timeoutUs = 10_000L
        var outputDone = false
        var inputDone = false

        while (!outputDone) {
            // Feed input
            if (!inputDone) {
                val inputBufferId = codec.dequeueInputBuffer(timeoutUs)
                if (inputBufferId >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputBufferId)!!
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
                    if (sampleSize < 0) {
                        codec.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                    } else {
                        codec.queueInputBuffer(inputBufferId, 0, sampleSize, extractor.sampleTime, 0)
                        extractor.advance()
                    }
                }
            }

            // Drain output
            val bufferInfo = MediaCodec.BufferInfo()
            val outputBufferId = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            when {
                outputBufferId >= 0 -> {
                    val outputBuffer = codec.getOutputBuffer(outputBufferId)!!
                    val chunk = ByteArray(bufferInfo.size)
                    outputBuffer.get(chunk)
                    pcmData.addAll(chunk.toList())
                    codec.releaseOutputBuffer(outputBufferId, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        outputDone = true
                    }
                }
                outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    val newFormat = codec.outputFormat
                    sampleRate = newFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    channelCount = newFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                }
            }
        }

        codec.stop()
        codec.release()
        extractor.release()

        // Write WAV file
        val pcmBytes = pcmData.toByteArray()
        writeWAV(outputFile, pcmBytes, sampleRate, channelCount)

        outputFile
    } catch (e: Exception) {
        e.printStackTrace()
        outputFile.delete()
        null
    }
}

fun writeWAV(file: File, pcmData: ByteArray, sampleRate: Int, channelCount: Int) {
    val bitsPerSample = 16
    val byteRate = sampleRate * channelCount * bitsPerSample / 8
    val blockAlign = channelCount * bitsPerSample / 8
    val dataSize = pcmData.size
    val totalSize = 36 + dataSize

    FileOutputStream(file).use { fos ->
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF chunk
        header.put("RIFF".toByteArray())
        header.putInt(totalSize)
        header.put("WAVE".toByteArray())

        // fmt sub-chunk
        header.put("fmt ".toByteArray())
        header.putInt(16)                      // Sub-chunk size (PCM = 16)
        header.putShort(1)                     // Audio format (1 = PCM)
        header.putShort(channelCount.toShort())
        header.putInt(sampleRate)
        header.putInt(byteRate)
        header.putShort(blockAlign.toShort())
        header.putShort(bitsPerSample.toShort())

        // data sub-chunk
        header.put("data".toByteArray())
        header.putInt(dataSize)

        fos.write(header.array())
        fos.write(pcmData)
    }
}

@Composable
fun getFileName(uri: Uri): String {
    val context = LocalContext.current
    var result = ""

    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    result = it.getString(nameIndex)
                }
            }
        }
    }
    return result
}