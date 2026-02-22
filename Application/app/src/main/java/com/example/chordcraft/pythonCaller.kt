package com.example.chordcraft

import androidx.compose.runtime.Composable
import com.chaquo.python.Python

@Composable
fun pythonLauncher(): String {
    val python = Python.getInstance()
    val module = python.getModule("test")
    val result = module.callAttr("main").toString()
    return result
}