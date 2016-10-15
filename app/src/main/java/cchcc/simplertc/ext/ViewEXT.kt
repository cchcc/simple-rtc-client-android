package cchcc.simplertc.ext

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View

fun View.startAnimationNope(movingDelta: Float = 20f) {

    val pvhTranslateX = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X,
            Keyframe.ofFloat(0f, 0f),
            Keyframe.ofFloat(.10f, -movingDelta),
            Keyframe.ofFloat(.26f, movingDelta),
            Keyframe.ofFloat(.42f, -movingDelta),
            Keyframe.ofFloat(.58f, movingDelta),
            Keyframe.ofFloat(.74f, -movingDelta),
            Keyframe.ofFloat(.90f, movingDelta),
            Keyframe.ofFloat(1f, 0f)
    )

    ObjectAnimator.ofPropertyValuesHolder(this, pvhTranslateX).
            setDuration(500).start()
}

fun View.startAnimationTada(shakeFactor: Float = 2f) {

    val pvhScaleX = PropertyValuesHolder.ofKeyframe(View.SCALE_X,
            Keyframe.ofFloat(0f, 1f),
            Keyframe.ofFloat(.1f, .9f),
            Keyframe.ofFloat(.2f, .9f),
            Keyframe.ofFloat(.3f, 1.1f),
            Keyframe.ofFloat(.4f, 1.1f),
            Keyframe.ofFloat(.5f, 1.1f),
            Keyframe.ofFloat(.6f, 1.1f),
            Keyframe.ofFloat(.7f, 1.1f),
            Keyframe.ofFloat(.8f, 1.1f),
            Keyframe.ofFloat(.9f, 1.1f),
            Keyframe.ofFloat(1f, 1f))

    val pvhScaleY = PropertyValuesHolder.ofKeyframe(View.SCALE_Y,
            Keyframe.ofFloat(0f, 1f),
            Keyframe.ofFloat(.1f, .9f),
            Keyframe.ofFloat(.2f, .9f),
            Keyframe.ofFloat(.3f, 1.1f),
            Keyframe.ofFloat(.4f, 1.1f),
            Keyframe.ofFloat(.5f, 1.1f),
            Keyframe.ofFloat(.6f, 1.1f),
            Keyframe.ofFloat(.7f, 1.1f),
            Keyframe.ofFloat(.8f, 1.1f),
            Keyframe.ofFloat(.9f, 1.1f),
            Keyframe.ofFloat(1f, 1f))

    val pvhRotate = PropertyValuesHolder.ofKeyframe(View.ROTATION,
            Keyframe.ofFloat(0f, 0f),
            Keyframe.ofFloat(.1f, -3f * shakeFactor),
            Keyframe.ofFloat(.2f, -3f * shakeFactor),
            Keyframe.ofFloat(.3f, 3f * shakeFactor),
            Keyframe.ofFloat(.4f, -3f * shakeFactor),
            Keyframe.ofFloat(.5f, 3f * shakeFactor),
            Keyframe.ofFloat(.6f, -3f * shakeFactor),
            Keyframe.ofFloat(.7f, 3f * shakeFactor),
            Keyframe.ofFloat(.8f, -3f * shakeFactor),
            Keyframe.ofFloat(.9f, 3f * shakeFactor),
            Keyframe.ofFloat(1f, 0f))

    ObjectAnimator.ofPropertyValuesHolder(this, pvhScaleX, pvhScaleY, pvhRotate).
            setDuration(1000).start()
}