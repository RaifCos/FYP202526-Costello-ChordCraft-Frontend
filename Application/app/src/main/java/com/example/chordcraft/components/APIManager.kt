package com.example.chordcraft.components

import android.content.Context
import android.net.Uri
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody


fun callAPI(context: Context, uri: Uri): String {
    val client = OkHttpClient()
    val stream = context.contentResolver.openInputStream(uri) ?: return "Error: couldn't open file"

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("file", uri.lastPathSegment ?: "audio", stream.readBytes().toRequestBody())
        .build()

    val request = Request.Builder()
        .url(" https://fyp202526-costello-chordcraft-backend-production.up.railway.app/run")
        .post(requestBody)
        .build()

    return client.newCall(request).execute().use { it.body?.string() ?: "Error: empty response" }
}