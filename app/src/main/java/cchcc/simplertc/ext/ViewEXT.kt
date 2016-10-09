package cchcc.simplertc.ext

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View

fun View.nope() {
    val delta = 20f

    val pvhTranslateX = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X,
            Keyframe.ofFloat(0f, 0f),
            Keyframe.ofFloat(.10f, -delta),
            Keyframe.ofFloat(.26f, delta),
            Keyframe.ofFloat(.42f, -delta),
            Keyframe.ofFloat(.58f, delta),
            Keyframe.ofFloat(.74f, -delta),
            Keyframe.ofFloat(.90f, delta),
            Keyframe.ofFloat(1f, 0f)
    )

    ObjectAnimator.ofPropertyValuesHolder(this, pvhTranslateX).
            setDuration(500).start()
}