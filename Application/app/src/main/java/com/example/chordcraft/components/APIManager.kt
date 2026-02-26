package com.example.chordcraft.components

import android.content.Context
import android.net.Uri
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


fun callAPI(context: Context, uri: Uri): String {
    val client = OkHttpClient()
    val stream = context.contentResolver.openInputStream(uri) ?: return "Error: couldn't open file"

    val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    } ?: return "Error: couldn't resolve filename."

    val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("file", fileName, stream.readBytes().toRequestBody(mimeType.toMediaType()))
        .build()

    val request = Request.Builder()
        .url("https://fyp202526-costello-chordcraft-backend-production.up.railway.app/run")
        .post(requestBody)
        .build()

    return client.newCall(request).execute().use { it.body?.string() ?: "Error: empty response" }
}