package cchcc.simplertc.ext

import android.content.SharedPreferences

inline fun SharedPreferences.save(block: SharedPreferences.Editor.() -> Unit) = apply {
    val editor = edit()
    editor.block()
    editor.apply()
}
