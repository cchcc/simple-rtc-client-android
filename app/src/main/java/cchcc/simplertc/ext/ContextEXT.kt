package cchcc.simplertc.ext

import android.app.Service
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import cchcc.simplertc.R
import cchcc.simplertc.ui.LoadingDialog

fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT): Unit
        = Toast.makeText(this, text, duration).show()

fun Context.toast(resId: Int, duration: Int = Toast.LENGTH_SHORT): Unit
        = toast(getText(resId), duration)

val Context.preferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)

fun Context.simpleAlert(text: CharSequence): AlertDialog =
   AlertDialog.Builder(this)
        .setMessage(text)
        .setPositiveButton(R.string.ok) { dlg, w -> dlg.dismiss() }
        .show()

private var loadingDialog: LoadingDialog? = null
fun Context.showLoading() {
    hideLoading()
    loadingDialog = LoadingDialog(this).apply { show() }
}

fun Context.hideLoading() {
    loadingDialog?.dismiss()
    loadingDialog = null
}

val Context.inputMethodManager: InputMethodManager
    get() = getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager

val Context.audioManager: AudioManager
    get() = getSystemService(Context.AUDIO_SERVICE) as AudioManager