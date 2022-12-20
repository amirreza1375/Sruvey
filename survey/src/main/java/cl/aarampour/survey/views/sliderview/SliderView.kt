package cl.aarampour.survey.views.sliderview

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import cl.aarampour.survey.R
import cl.aarampour.survey.SurveyKey
import cl.aarampour.survey.slider.ImageSliderView
import cl.aarampour.survey.slider.SliderItemModel
import cl.aarampour.survey.slider.SwiperRecyclerAdapter
import cl.aarampour.survey.views.base.BaseView
import cl.aarampour.survey.views.base.IConditionalValueListener
import cl.aarampour.survey.views.sliderview.imageview.GestureImageView
import org.json.JSONException
import org.json.JSONObject

class SliderView(
    context: Context,
    elementJSONObject: JSONObject,
    elementEnabled: Boolean,
    position: Int,
    conditionalCallback: IConditionalValueListener
) : BaseView(context, elementJSONObject, elementEnabled, position, conditionalCallback),SwiperRecyclerAdapter.SwiperActionListener {


    override fun getView(): Int {
        return R.layout.slider_view_layout
    }

    override fun getAnswer(answer: JSONObject) {
        //No answer
    }

    override fun initView(context: Context): LinearLayout {
        baseView?.let { safeBaseView ->

            val sliderContainer = safeBaseView.findViewById<LinearLayout>(R.id.sliderContainer)

            val sliderImageArray = ArrayList<SliderItemModel>()

            for (imageModel in liveData.sliderImages){

                if (isConditionsAreOk(imageModel,elementJSONObject)){
                    if (isSurveyOk(imageModel)){
                        val sliderItemModel = SliderItemModel()
                        sliderItemModel.name = imageModel.name
                        sliderItemModel.path = imageModel.imageFile
                        sliderItemModel.priority = imageModel.prioritie

                        sliderImageArray.add(sliderItemModel)
                    }
                }

            }

            if (sliderImageArray.size > 0) {

                val imageSliderView = ImageSliderView(context, sliderImageArray,this)

                sliderContainer.addView(imageSliderView)
            }else{
                this.visibility = GONE
            }
        }

        return this
    }

    private fun show_image(image_path: String, name: String, priority: String) {
        val activity = context as Activity
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        val inflater = activity.layoutInflater
        val view: View = inflater.inflate(R.layout.image_zoom_view_alert, this, false)
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.show()
        val nameTxt = view.findViewById<TextView>(R.id.name)
        val priorityTxt = view.findViewById<TextView>(R.id.priority)
        if (priority != "") {
            priorityTxt.text = "Prioridad : $priority"
            // priorityTxt.setVisibility(GONE);
        } else {
            priorityTxt.visibility = GONE
        }
        nameTxt.text = name
        val close = view.findViewById<ImageView>(R.id.close)
        val content: GestureImageView = view.findViewById(R.id.gesture_image)
        val bitmap = BitmapFactory.decodeFile(image_path)
        content.setImageBitmap(bitmap)
        close.setOnClickListener { alertDialog.dismiss() }
    }


    private fun isConditionsAreOk(model: ImageModel, element: JSONObject): Boolean {
        val resultIds: ArrayList<ResultId> = model.resultIDS

        var isOk = true
        var isAnyResultOk = false
        for (j in resultIds.indices) {
            val resultId = resultIds[j]
            try {
                val Posicion =
                    if (element.has(SurveyKey.Page.View.Slider.POSICTION)) element.getInt(SurveyKey.Page.View.Slider.POSICTION) else -1
                val Elemento =
                    if (element.has(SurveyKey.Page.View.Slider.ELEMENTO)) element.getInt(SurveyKey.Page.View.Slider.ELEMENTO) else -1
                val subCanal =
                    if (element.has(SurveyKey.Page.View.Slider.SUBCANAL)) element.getInt(SurveyKey.Page.View.Slider.SUBCANAL) else -1

                if (Posicion > -1) {
                    if (Posicion != resultId.posicion) {
                        isOk = false
                        continue
                    }
                }
                if (Elemento > -1) {
                    if (Elemento != resultId.elemento) {
                        isOk = false
                        continue
                    }
                }
                if (subCanal > -1) {
                    if (subCanal != resultId.subCanal) {
                        isOk = false
                        continue
                    }
                }
                var FLAG_EXIST = false
                if (model.shops.size == 0) {
                    FLAG_EXIST = true
                } else {
                    //check shops
                    for (k in 0 until model.shops.size) {
                        if (model.shops.get(k) === liveData.shopId) {

                            FLAG_EXIST = true
                            break
                        }
                    }
                }
                if (FLAG_EXIST) {
                    isAnyResultOk = true
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } //end of result for
        return isAnyResultOk

    }

    private fun isSurveyOk(model: ImageModel): Boolean {
        if (model.surveyIdes == null) return true
        if (model.surveyIdes.equals("")) return true
        val surveys: List<String> = model.surveyIdes!!.split(",")
        for (survey in surveys) {
            if (survey.equals(liveData.checklistId.toString())) {
                return true
            }
        }
        return false
    }

    override fun getViewDataFromJson() {

    }

    override fun clearData() {
    }

    override fun getValue(): JSONObject? {
        return null
    }

    override fun disableView() {
        blurViewAsDisables()
    }

    override fun enableView() {
        unBlurViewAsEnables()
    }

    override fun isViewMandatoryAnswered(): Boolean {
        return true
    }

    override fun updateView() {

    }

    override fun onPressedItem(sliderItemModel: SliderItemModel) {
        show_image(sliderItemModel.path!!,sliderItemModel.name!!,sliderItemModel.priority!!)
    }
}