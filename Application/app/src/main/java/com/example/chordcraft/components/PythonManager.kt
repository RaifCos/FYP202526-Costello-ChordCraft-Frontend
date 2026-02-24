package com.example.chordcraft.components

import androidx.compose.runtime.Composable
import com.chaquo.python.Python

@Composable
fun callPython(): String {
    val python = Python.getInstance()
    val module = python.getModule("test")
    val result = module.callAttr("main").toString()
    return result
}

fun callPython(parameterValue: String): String {
    val python = Python.getInstance()
    val module = python.getModule("modelCustom")
    val result = module.callAttr("main", parameterValue).toString()
    return result
}