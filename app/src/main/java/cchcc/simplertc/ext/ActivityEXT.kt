package cchcc.simplertc.ext

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.SparseArray

private val requestedPermissions by lazy { SparseArray<(Boolean) -> Unit>() }

/**
 *  퍼미션을 획득했는지 확인후 없으면 사용자에게 요청함.
 *  @permissionGrantedBlock 이미 퍼미션이 있거나 요청후 사용자가 수락을 하면 파라매터에 true 로 콜백
 */
fun Activity.checkOrRequestPermissions(vararg permissions: String, permissionGrantedBlock: (Boolean) -> Unit) {
    val permissionAllGranted = !permissions.any {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED
    }

    if (permissionAllGranted) {
        permissionGrantedBlock(true)
    } else {
        val requestCode = Math.abs(permissions.hashCode()) shr 16
        requestedPermissions.put(requestCode, permissionGrantedBlock)
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }
}

/**
 *  onRequestPermissionsResult 에서 호출 할것
 */
fun Activity.requestedPermissionResult(requestCode: Int, grantResults: IntArray) {
    requestedPermissions[requestCode]?.let {
        it(!grantResults.any { it == PackageManager.PERMISSION_DENIED })
        requestedPermissions.remove(requestCode)
    }
}