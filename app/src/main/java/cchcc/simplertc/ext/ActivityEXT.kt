package cchcc.simplertc.ext

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

private val requestedPermissions by lazy { mutableMapOf<Int/*request code, 16bits only*/, ()->Unit/*permission granted block*/>() }

/**
 *  퍼미션을 획득했는지 확인후 없으면 요청함. 퍼미션이 있거나 사용자 수락후 최종적으로 permissionGrantedBlock 을 호출함
 */
fun Activity.checkOrRequestPermissions(vararg permissions: String, permissionGrantedBlock:()->Unit): Boolean {
    val permissionAllGranted = permissions.filter {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED
    }.isEmpty()

    if (permissionAllGranted) {
        permissionGrantedBlock()
        return true
    } else {
        val requestCode = Math.abs(permissions.hashCode()) shr 16
        requestedPermissions.put(requestCode, permissionGrantedBlock)
        ActivityCompat.requestPermissions(this, permissions, requestCode)
        return false
    }
}

/**
 *  onRequestPermissionsResult 에서 호출 할것
 */
fun Activity.requestedPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    requestedPermissions.remove(requestCode)?.let {
        val permissionAllGranted = grantResults.size == grantResults
                .filter { it == PackageManager.PERMISSION_GRANTED }.size
        if (permissionAllGranted)
            it()
    }
}