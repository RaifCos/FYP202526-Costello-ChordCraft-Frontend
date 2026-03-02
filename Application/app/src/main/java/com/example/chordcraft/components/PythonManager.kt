package com.example.chordcraft.components
import com.chaquo.python.Python

fun callPython(module: String) {
    val python = Python.getInstance()
    val module = python.getModule(module)
    module.callAttr("main")
}

fun callPythonReturn(module: String, parameterValue: String): String {
    val python = Python.getInstance()
    val module = python.getModule(module)
    val result = module.callAttr("main", parameterValue).toString()
    return result
}