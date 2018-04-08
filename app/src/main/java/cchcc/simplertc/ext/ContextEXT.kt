package cchcc.simplertc.ext

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import cchcc.simplertc.R
import cchcc.simplertc.ui.LoadingDialog
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.with

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
    get() = appKodein().with(this).instance()

val Context.audioManager: AudioManager
    get() = appKodein().with(this).instance()