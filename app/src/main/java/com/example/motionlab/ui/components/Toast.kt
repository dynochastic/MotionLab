package com.example.motionlab.ui.components

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import android.widget.Toast

fun CustomToast(context: Context, message: String) {
    val toast = Toast(context)
    val textView = TextView(context).apply {
        text = message
        setTextColor(android.graphics.Color.WHITE)
        textSize = 16f
        setPadding(32, 16, 32, 16)
        background = GradientDrawable().apply {
            cornerRadius = 42f
            setColor(android.graphics.Color.GRAY)
        }
    }
    toast.view = textView
    toast.duration = Toast.LENGTH_SHORT
    toast.show()
}