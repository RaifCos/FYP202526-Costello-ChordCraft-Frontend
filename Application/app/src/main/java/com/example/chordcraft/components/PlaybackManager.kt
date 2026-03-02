package com.example.chordcraft.components

import android.content.Context
import android.media.SoundPool

fun playbackAudio(context: Context) {
    val soundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .build()

    val note = soundPool.load(context.assets.openFd("soundbank/C4.wav"), 1)
    soundPool.setOnLoadCompleteListener { pool, sampleId, status ->
        if (status == 0) {
            pool.play(sampleId, 1f, 1f, 0, 0, 1f)
        }
    }
}