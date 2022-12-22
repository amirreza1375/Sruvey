package com.amirrezaarampour.sruvey

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class Functions {
    companion object {

        /**
         * Get current milisec from date
         */

        //endregion
        fun get_current_long_time(date: String): Long {
            val parts = date.split("-").toTypedArray()
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()
            val calendar = Calendar.getInstance()
            calendar[Calendar.YEAR] = year
            calendar[Calendar.MONTH] = month - 1
            calendar[Calendar.DAY_OF_MONTH] = day
            return calendar.timeInMillis
        }

        /**
         * Handle prefix zero in dates
         */

        fun handle_day_with_zero(dayOrMonth: Int): String? {

            var day = dayOrMonth.toString()
            if (day.length < 2) {
                day = "0$day"
            }
            return day
        }

        /**
         * Convert DP to PX for adding sizes programmatically
         */
        fun dpToPx(dp: Float, context: Context): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.resources.displayMetrics
            ).toInt()
        }

        /**
         * App current version
         */
        fun getCurrentVersion(context: Context): String {

            return context.packageManager.getPackageInfo(context.packageName, 0).versionName

        }

        /**
         * Check to see if user installed current app directly or installed from Play Store
         */

        fun isInstalledFromPlayStore(context: Context): Boolean {


            //  list of valid installers package name
            val validInstallers: List<String> = ArrayList(
                Arrays.asList(
                    "com.android.vending",
                    "com.google.android.feedback"
                )
            )

            val installer = context.packageManager.getInstallerPackageName(context.packageName)

            return installer != null && validInstallers.contains(installer)

        }

        /**
         * Check network connectivity status
         */

        fun isNetworkConnected(context: Context): Boolean {
            val manager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val nw = manager.activeNetwork ?: return false
                val actNw = manager.getNetworkCapabilities(nw) ?: return false
                return when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> return true
                    else -> return false
                }
            } else {
                val nwInfo = manager.activeNetworkInfo ?: return false
                return nwInfo.isConnected
            }
        }

        /**
         * Blink animation
         */

        fun blink(view: View) {
            val alphaAnim = AlphaAnimation(0.0f, 1.0f)
            alphaAnim.duration = 700
            alphaAnim.repeatCount = AlphaAnimation.INFINITE
            alphaAnim.repeatMode = AlphaAnimation.REVERSE
            view.startAnimation(alphaAnim)
        }


        fun recycleBitmap(bitmap: Bitmap?) {
            if (bitmap != null) {
                if (bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
        }

        /**
         * Check android version is above Q Api 29
         */
        fun isAboveQ(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        }

        @SuppressLint("SimpleDateFormat")


        fun showToast(context: Context, msg: String?) {
            val mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)

            // cancel previous toast and display correct answer toast
            try {
                if (mToast.view!!.isShown) {
                    mToast.cancel()
                }
                // cancel same toast only on Android P and above, to avoid IllegalStateException on addView
                if (Build.VERSION.SDK_INT >= 28 && mToast.view!!.isShown) {
                    mToast.cancel()
                }
                val activity = context as Activity
                activity.runOnUiThread { mToast.show() }
            } catch (e: Exception) {
                e.printStackTrace()
                mToast.show()
            }
        }




        fun playSound(context: Context,res : Int){
            val mediaPlayer = MediaPlayer.create(context,res)
            mediaPlayer.setVolume(0.08f,0.08f)
            mediaPlayer.start()
        }
    }


}