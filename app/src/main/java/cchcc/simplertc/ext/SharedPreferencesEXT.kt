package cchcc.simplertc.ext

import android.content.SharedPreferences

fun SharedPreferences.save(block: (SharedPreferences.Editor) -> Unit): SharedPreferences
        = edit().let {
    block(it)
    it.apply()
    this
}