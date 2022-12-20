package cl.aarampour.survey.views.image

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import cl.aarampour.survey.R
import cl.aarampour.survey.SurveyKey
import cl.aarampour.survey.views.base.BaseView
import cl.aarampour.survey.views.base.IConditionalValueListener
import org.json.JSONArray
import org.json.JSONObject

class ImagesTakerView(
    context: Context, element: JSONObject,
    isViewEnabled: Boolean,
    position: Int,
    conditionalCallback: IConditionalValueListener
) : BaseView(context, element, isViewEnabled, position, conditionalCallback) {

    private lateinit var imageItemsHolder: LinearLayout
    private var imageTypeNames: List<String>? = null
    private lateinit var imageTypeIdes: List<String>
    private lateinit var imageModels: ArrayList<ImageTakerItemView>

    override fun getView(): Int {
        return R.layout.images_taker_layout
    }

    override fun getAnswer(answer: JSONObject) {


    }

    override fun initView(context: Context): LinearLayout {

        imageModels = ArrayList()

        baseView?.let { safeBaseView ->

            imageItemsHolder = safeBaseView.findViewById(R.id.imageItemsHolder)

            imageTypeNames?.let { safeImageTypeNames ->
                var countIndex = 0
                for (i in 0 until safeImageTypeNames.size) {

                    val imageTypeName = safeImageTypeNames[i]

                    val imageTypeCountStr =
                        if (elementJSONObject.has(SurveyKey.Page.View.ImagesTaker.COUNT + "-" + imageTypeName))
                            elementJSONObject.getString(SurveyKey.Page.View.ImagesTaker.COUNT + "-" + imageTypeName) else "1"
                    val imageTypeCount = imageTypeCountStr.toInt()


                    for (j in 0 until imageTypeCount) {

                        var path = ""
                        liveData.getImageOfViewIndex(pagePosition, viewId, countIndex + j)
                            ?.getString(SurveyKey.IMAGE_PROPS.PATH)?.let{safePath ->
                                path = safePath
                                isViewAnswered = true
                                removeMandatoryError()
                            }

                        val imageTakerItem = ImageTakerItemView(
                            context, viewId, viewName, pagePosition, imageTypeIdes[i].toInt(),
                            imageTypeName, countIndex + j, liveData.getFolderPath(), path
                        )


                        imageItemsHolder.addView(imageTakerItem)

                        imageModels.add(imageTakerItem)

                    }

                    countIndex += imageTypeCount

                }

            } ?: kotlin.run {
                val errorTxt = TextView(context)
                errorTxt.setText("There is no image configured here !")
                errorTxt.setTextColor(resources.getColor(R.color.generalErrorColor))
                imageItemsHolder.addView(errorTxt)
            }

        }

        return this
    }


    override fun getViewDataFromJson() {
        if (elementJSONObject.has(SurveyKey.Page.View.ImagesTaker.IMAGE_TYPE_NAME)) {
            imageTypeNames =
                elementJSONObject.getString(SurveyKey.Page.View.ImagesTaker.IMAGE_TYPE_NAME)
                    .split(",")
            imageTypeIdes =
                elementJSONObject.getString(SurveyKey.Page.View.ImagesTaker.IMAGE_TYPE).split(",")
        }
    }

    override fun clearData() {

    }

    override fun getValue(): JSONObject? {
        return null
    }

    override fun disableView() {
        blurViewAsDisables()

        for (imageModel in imageModels){
            imageModel.disable()
        }
    }

    override fun enableView() {
        unBlurViewAsEnables()

        for (imageModel in imageModels){
            imageModel.isEnabled = true
        }
    }

    override fun isViewMandatoryAnswered(): Boolean {
        return true
    }

    override fun updateView() {
        imageItemsHolder.removeAllViews()
        initView(context)
    }
}