package cl.aarampour.survey

import android.content.Context
import android.media.MediaPlayer
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation

class Functions {
    companion object{

        fun playSound(context: Context,res : Int){
            val mediaPlayer = MediaPlayer.create(context,res)
            mediaPlayer.setVolume(0.08f,0.08f)
            mediaPlayer.start()
        }

        fun dpToPx(dp: Int, context: Context): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                context.resources.displayMetrics
            ).toInt()
        }

        fun rotateView(view : View){

            val ra = RotateAnimation(
                0F, 360F,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )
            ra.duration = 500
            ra.repeatMode = RotateAnimation.REVERSE
            ra.repeatCount = RotateAnimation.INFINITE
            ra.fillAfter = true
            ra.isFillEnabled = true
            view.startAnimation(ra)

        }


    }
}