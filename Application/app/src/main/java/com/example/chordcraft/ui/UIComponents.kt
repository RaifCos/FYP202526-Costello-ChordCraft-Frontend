package com.example.chordcraft.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp


import com.example.chordcraft.ChordExtractionActivity
import com.example.chordcraft.ChordPlayingActivity

@Composable
fun BorderBar(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.primary)
    )
}

@Composable
fun 3Menu () {
    val currContext = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Row (
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button( { moveActivity(currContext, ChordExtractionActivity::class.java) } ) { }
            Button( { moveActivity(currContext, ChordPlayingActivity::class.java) } ) { }
        }
    }
}

fun moveActivity(context: Context, target: Class<out Activity>) {
    val intent = Intent(context, target)
    context.startActivity(intent)
}