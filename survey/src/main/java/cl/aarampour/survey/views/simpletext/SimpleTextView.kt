package cl.aarampour.survey.views.simpletext

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import cl.aarampour.survey.R
import cl.aarampour.survey.SurveyKey
import cl.aarampour.survey.views.base.BaseView
import cl.aarampour.survey.views.base.IConditionalValueListener
import org.json.JSONObject

class SimpleTextView(
    context: Context,
    elementJSONObject: JSONObject,
    elementEnabled: Boolean,
    position: Int,
    conditionalCallback: IConditionalValueListener
) : BaseView(context, elementJSONObject, elementEnabled, position, conditionalCallback) {

    private lateinit var textView: TextView
    private var text = ""

    override fun getView(): Int {
        return R.layout.simple_text_layout
    }

    override fun getAnswer(answer: JSONObject) {

    }

    override fun initView(context: Context): LinearLayout {
        baseView?.let { safeBaseView ->
            textView = safeBaseView.findViewById(R.id.textView)

            textView.setText(text)

            isViewAnswered = true
        }
        return this
    }

    override fun getViewDataFromJson() {
        text =
            if (elementJSONObject.has(SurveyKey.Page.View.SimpleText.HTML)) elementJSONObject.getString(
                SurveyKey.Page.View.SimpleText.HTML
            ) else ""
    }

    override fun clearData() {
        //No need
    }

    override fun getValue(): JSONObject? {
        //No need
        return null
    }

    override fun disableView() {
    }

    override fun enableView() {
    }

    override fun isViewMandatoryAnswered(): Boolean {
        return true
    }

    override fun updateView() {
    }
}