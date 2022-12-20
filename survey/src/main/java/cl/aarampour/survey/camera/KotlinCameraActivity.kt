package cl.aarampour.survey.camera

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import cl.aarampour.survey.SurveyKey
import cl.aarampour.survey.SurveyKey.IMAGE_PROPS.Companion.IMAGE_TYPE_ID
import cl.aarampour.survey.SurveyKey.IMAGE_PROPS.Companion.IMAGE_TYPE_NAME
import cl.aarampour.survey.SurveyKey.IMAGE_PROPS.Companion.INDEX
import cl.aarampour.survey.SurveyKey.IMAGE_PROPS.Companion.PAGE_POSITION
import cl.aarampour.survey.SurveyKey.IMAGE_PROPS.Companion.PATH
import cl.aarampour.survey.SurveyKey.IMAGE_PROPS.Companion.VIEW_ID
import cl.aarampour.survey.SurveyKey.IMAGE_PROPS.Companion.VIEW_NAME
import cl.aarampour.survey.databinding.ActivityKotlinCameraBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class KotlinCameraActivity : AppCompatActivity() , View.OnClickListener{

    lateinit var binding: ActivityKotlinCameraBinding

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var preview : Preview
    private lateinit var cameraProvider : ProcessCameraProvider

    lateinit var photoFile : File
    private val folderPath = ""
    private var viewId = ""
    private var viewName = ""
    private var typeId = 0
    private var typeName = ""
    private var pagePosition = 0
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityKotlinCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.closeBtn.setOnClickListener(this)
        binding.acceptBtn.setOnClickListener(this)

        startCamera()

        binding.takePhotoBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                takePhoto()
            }

        })

        val bundle = intent.extras
        bundle?.let { safeBundle ->
            val folderPath = safeBundle.getString(SurveyKey.IMAGE_PROPS.FOLDER_PATH)
            folderPath?.let {
                outputDirectory = File(it)
                viewId = bundle.getString(VIEW_ID)!!
                viewName = bundle.getString(VIEW_NAME)!!
                typeId = bundle.getInt(VIEW_ID)
                typeName = bundle.getString(IMAGE_TYPE_NAME)!!
                pagePosition = bundle.getInt(PAGE_POSITION)
                index = bundle.getInt(INDEX)
            }

        }

        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    private fun takePhoto() {



        rotateView(binding.takePhotoBtn)

        val imageCapture = imageCapture ?: return

        imageCapture.flashMode = ImageCapture.FLASH_MODE_AUTO

        photoFile = File(outputDirectory, "${System.currentTimeMillis()}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)

                    binding.previewImageView.setImageURI(savedUri)
                    binding.previewImageView.visibility = View.VISIBLE
                    binding.cameraPreview.visibility = View.GONE

                    Toast.makeText(
                        this@KotlinCameraActivity,
                        "Photo captured $savedUri",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.takePhotoBtn.clearAnimation()

                    binding.cameraOptionsContainer.visibility = VISIBLE
                    binding.pdfImageView.visibility = GONE
                    binding.galleryImageView.visibility = GONE
                    binding.takePhotoBtn.visibility = GONE
                }

                override fun onError(exception: ImageCaptureException) {
                    binding.takePhotoBtn.clearAnimation()
                    Toast.makeText(
                        this@KotlinCameraActivity,
                        "${exception.cause}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })

    }

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
             cameraProvider  = cameraProviderFuture.get()

             preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

               cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onClick(view: View?) {
        if (view == binding.closeBtn){
            binding.cameraOptionsContainer.visibility = GONE
            binding.previewImageView.visibility = GONE
            binding.cameraPreview.visibility = VISIBLE
            binding.pdfImageView.visibility = VISIBLE
            binding.galleryImageView.visibility = VISIBLE
            binding.takePhotoBtn.visibility = VISIBLE
        }else if (view == binding.acceptBtn){
            val newData = Intent()
            newData.putExtra(VIEW_NAME, viewName)
            newData.putExtra(VIEW_ID, viewId)
            newData.putExtra(IMAGE_TYPE_NAME, typeName)
            newData.putExtra(IMAGE_TYPE_ID, typeId)
            newData.putExtra(INDEX, index)
            newData.putExtra(PAGE_POSITION, pagePosition)
            newData.putExtra(PATH, photoFile.absoluteFile.toString())
            setResult(Activity.RESULT_OK,newData)
            finish()
        }
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