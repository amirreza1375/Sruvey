package cl.aarampour.survey.views.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.widget.*
import cl.aarampour.survey.R
import cl.aarampour.survey.SurveyKey
import cl.aarampour.survey.camera.ActivityCamera
import cl.aarampour.survey.camera.KotlinCameraActivity

class ImageTakerItemView(context: Context) : LinearLayout(context) {

    lateinit var takeImage: RelativeLayout

    private lateinit var viewId: String
    private lateinit var viewName: String
    private lateinit var imageTypeName: String

    private var pagePosition: Int = 0
    private var imageTypeId: Int = 0
    private var index: Int = 0

    public var path = ""


    constructor(
        context: Context,
        viewId: String,
        viewName: String,
        pagePosition: Int,
        imageTypeId: Int,
        imageTypeName: String,
        index: Int,
        folderPath: String,
        path: String
    ) : this(context) {
        this.viewId = viewId
        this.viewName = viewName
        this.imageTypeName = imageTypeName
        this.imageTypeId = imageTypeId
        this.pagePosition = pagePosition
        this.index = index
        this.path = path

        val view =
            LayoutInflater.from(context).inflate(R.layout.image_taker_layout_item, this, false)

        val catNameTxt: TextView = view.findViewById(R.id.catNameTxt)
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val emptyImageView: ImageView = view.findViewById(R.id.emptyImageView)
        val pdfNameTxt: TextView = view.findViewById(R.id.pdfNameTxt)
        takeImage = view.findViewById(R.id.takeImage)

        if (path.equals("")) {
            emptyImageView.visibility = VISIBLE
            imageView.visibility = GONE
        } else {
            if(!getFileExtention(path).contains("pdf")) {
                pdfNameTxt.visibility = GONE
                emptyImageView.visibility = GONE
                imageView.visibility = VISIBLE
                try {
                    val bitmap = BitmapFactory.decodeFile(path)
                    imageView.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
            }else{
                imageView.visibility = GONE
                emptyImageView.visibility = GONE
                pdfNameTxt.visibility = VISIBLE
                pdfNameTxt.setText(getFileName(path))
            }
        }

        takeImage.setOnClickListener {
            val cameraIntent = Intent(context, ActivityCamera::class.java)
            cameraIntent.putExtra(SurveyKey.IMAGE_PROPS.FOLDER_PATH, folderPath)
            cameraIntent.putExtra(SurveyKey.IMAGE_PROPS.VIEW_NAME, viewName)
            cameraIntent.putExtra(SurveyKey.IMAGE_PROPS.VIEW_ID, viewId)
            cameraIntent.putExtra(SurveyKey.IMAGE_PROPS.IMAGE_TYPE_NAME, imageTypeName)
            cameraIntent.putExtra(SurveyKey.IMAGE_PROPS.IMAGE_TYPE_ID, imageTypeId)
            cameraIntent.putExtra(SurveyKey.IMAGE_PROPS.PAGE_POSITION, pagePosition)
            cameraIntent.putExtra(SurveyKey.IMAGE_PROPS.INDEX, index)
            val activity = context as Activity
            activity.startActivityForResult(cameraIntent, 1000)
        }

        catNameTxt.setText(imageTypeName + "-" + index)

        this.addView(view)

    }

    fun disable() {
        takeImage.isEnabled = false
    }

    private fun getFileExtention(filePath: String): String {
        val picturePathArr = filePath.split(".")
        val extention = picturePathArr[picturePathArr.size - 1]
        return extention
    }

    private fun getFileName(filePath : String) : String{
        val picturePathArr = filePath.split("/")
        val name = picturePathArr[picturePathArr.size - 1]
        return name
    }


}