package cchcc.simplertc.ext

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.widget.Toast
import cchcc.simplertc.R
import cchcc.simplertc.ui.LoadingDialog

fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT): Unit
        = Toast.makeText(this, text, duration).show()

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