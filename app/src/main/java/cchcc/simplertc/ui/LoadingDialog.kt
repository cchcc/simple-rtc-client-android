package cchcc.simplertc.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import cchcc.simplertc.R

class LoadingDialog(context: Context) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val loadingView = ImageView(context).apply {
            setBackgroundResource(R.drawable.loading_circle)
            val rotationAnim = AnimationUtils.loadAnimation(context, R.anim.clockwise_rotation).apply {
                repeatMode = Animation.RESTART
                repeatCount = Animation.INFINITE
            }
            startAnimation(rotationAnim)
        }

        val view = LinearLayout(context).apply {
            gravity = Gravity.CENTER
            addView(loadingView)
        }

        setContentView(view)
        setCancelable(false)
    }
}