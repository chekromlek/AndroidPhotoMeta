package com.example.photometa

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.example.photometa.extension.HumanReadableFileSzie
import com.example.photometa.model.PhotoData
import kotlinx.android.synthetic.main.full_photo_activity.*
import kotlinx.android.synthetic.main.full_photo_activity.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class FullPhotoActivity : AppCompatActivity(), CoroutineScope {

    private val moveDown = 1
    private val moveUp = 2
    private val maxSettleDuration = 320 // ms

    private val job = Job()

    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

    private lateinit var detector: GestureDetectorCompat
    private lateinit var exif: ExifInterface

    private var minY: Float = 0.0f
    private var maxY: Float = 0.0F
    private var direction: Int = 0
    private var velocityTracker: VelocityTracker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_photo_activity)
        detector = GestureDetectorCompat(this, GestureListener())

        val data = intent.getParcelableExtra<PhotoData>("sample")
        Glide.with(this)
            .load(data.imagePath)
            .into(fullPhotoLayout.fullSizeImageView)

        launch {
            setMetadata(ExifInterface(data.imagePath), data)
        }

        removeAllMeta.setOnClickListener {
            this.exif.setLatLong(0.0, 0.0)
            this.exif.setAttribute(ExifInterface.TAG_MAKE, null)
            this.exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, null)
            this.exif.saveAttributes()
        }
    }

    override fun onBackPressed() {
        if (fullSizeImageView.translationY != 0.0f) {
            direction = moveDown
            finishAnimation(0.0f)
            return
        }
        super.onBackPressed()
    }

    @SuppressLint("SetTextI18n")
    @UiThread
    private fun setMetadata(exif: ExifInterface, data: PhotoData) {
        this.exif = exif
        filename.text = data.name
        imageSpec.text =
            "${((data.imageHeight * data.imageWidth) / 1048576).toDouble().roundToLong()}MP ・ ${data.imageWidth}x${data.imageHeight}px ・ ${File(
                data.imagePath
            ).length().HumanReadableFileSzie()}"

        val date = exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED)
        captureAt.text = if (date == null) "Unknown" else {
            val dateFormatter = SimpleDateFormat("yyyy:MM:dd hh:mm:ss", Locale.getDefault())
            SimpleDateFormat("EEE, dd MMMM yyyy HH:mm aaa", Locale.getDefault()).format(dateFormatter.parse(date))
        }

        val latLong = exif.latLong
        if (latLong == null) {
            location.text = "Unknown"
            address.text = "Unknown"
        } else {
            location.text = "${latLong[0]},${latLong[1]}"
            val adds = Geocoder(this).getFromLocation(latLong[0], latLong[1], 5)
            address.text = try {
                adds[0].getAddressLine(0)
            } catch (e: Exception) {
                "Unknown"
            }
        }

        model.text = exif.getAttribute(ExifInterface.TAG_MAKE) ?: "Unknown"

        photoMeta.post {
            val detailContainerHeight = photoMeta.measuredHeight
            minY = fullSizeImageView.translationY - detailContainerHeight
            photoMeta.translationY += detailContainerHeight
        }
    }

    fun finishAnimation(velocityY: Float) {
        val deltaY: Float = when (direction) {
            moveDown -> {
                -fullSizeImageView.translationY
            }
            moveUp -> {
                minY - fullSizeImageView.translationY
            }
            else -> return
        }

        var duration: Int = if (velocityY > 0) {
            4 * (1000 * abs(10 / velocityY)).roundToInt()
        } else {
            (abs(deltaY + 1) * 100).toInt()
        }
        duration = min(duration, maxSettleDuration)

        val initialY = photoMeta.translationY
        val show = direction == moveUp

        fullSizeImageView.animate()
            .setDuration(duration.toLong())
            .translationYBy(deltaY)
            .setUpdateListener {
                photoMeta.translationY = initialY + (deltaY * (it.animatedValue as Float))
            }
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    if (show) {
                        photoMeta.translationY = 0.0f
                        fullSizeImageView.translationY = minY
                    } else {
                        photoMeta.translationY = photoMeta.measuredHeight.toFloat()
                        fullSizeImageView.translationY = 0.0f
                    }
                }
            })
            .start()

        direction = 0
        velocityTracker?.recycle()
        velocityTracker = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(event)

        if (detector.onTouchEvent(event)) {
            return true
        }

        if (event.action == MotionEvent.ACTION_UP) {
            velocityTracker!!.computeCurrentVelocity(
                1000,
                ViewConfiguration.get(this).scaledMaximumFlingVelocity.toFloat()
            )
            finishAnimation(velocityTracker!!.yVelocity)
        }
        return super.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {

            val newY = fullSizeImageView.translationY - distanceY
            direction = if (distanceY > 0) moveDown else moveUp
            when {
                minY > newY -> {
                    fullSizeImageView.translationY = minY
                    photoMeta.translationY = 0.0F
                }
                maxY < newY -> {
                    fullSizeImageView.translationY = maxY
                    photoMeta.translationY = maxY - minY
                }
                else -> {
                    fullSizeImageView.translationY = newY
                    photoMeta.translationY -= distanceY
                }
            }

            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {

            if (e2?.action == MotionEvent.ACTION_UP) {
                direction = if (velocityY > 0) moveDown else moveUp
                finishAnimation(velocityY)
            }

            return true
        }

    }

}