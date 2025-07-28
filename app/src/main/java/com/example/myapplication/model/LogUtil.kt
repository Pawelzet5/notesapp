package com.example.myapplication.model

import android.util.Log

object LogUtil {

    private const val DEFAULT_TAG = "NotesApp"

    fun debug(message: String, tag: String = DEFAULT_TAG) {
        Log.d(tag, message)
    }

    fun error(message: String, tag: String = DEFAULT_TAG, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }
}