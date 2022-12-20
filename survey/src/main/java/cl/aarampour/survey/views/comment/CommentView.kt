package cl.aarampour.survey.views.comment

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.widget.LinearLayout
import android.widget.TextView
import cl.aarampour.survey.R
import cl.aarampour.survey.SurveyKey
import cl.aarampour.survey.views.base.BaseView
import cl.aarampour.survey.views.base.IConditionalValueListener
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import kotlin.math.max

class CommentView(
    context: Context,
    element: JSONObject,
    isViewEnabled: Boolean,
    position: Int,
    conditionalCallback: IConditionalValueListener
) : BaseView(context, element, isViewEnabled, position, conditionalCallback),TextWatcher {

    private lateinit var commentField: TextInputEditText
    private lateinit var maxLengthTxt: TextView
    private  var comment : String =""
    private var maxLength = -1

    override fun getView(): Int {
        return R.layout.comment_layout
    }

    override fun getAnswer(answer: JSONObject) {
        if (answer.has(SurveyKey.Page.View.Comment.VALUE))
        comment = answer.getString(SurveyKey.Page.View.Comment.VALUE)
    }

    override fun initView(context: Context): LinearLayout {
        baseView?.let { safeBaseView ->
            commentField = safeBaseView.findViewById(R.id.commentField)
            maxLengthTxt = safeBaseView.findViewById(R.id.maxTxt)
            commentField.addTextChangedListener(this)
            commentField.setText(comment)


            if (maxLength > 0){
                maxLengthTxt.visibility = VISIBLE
                val fArray = arrayOfNulls<InputFilter>(1)
                fArray[0] = LengthFilter(maxLength)
                commentField.filters = fArray

                maxLengthTxt.setText("MAX : "+maxLength)
            }

        }
        return this
    }

    override fun getViewDataFromJson() {
        maxLength = if (elementJSONObject.has(SurveyKey.Page.View.Comment.MAX_LENGTH)) elementJSONObject.getInt(SurveyKey.Page.View.Comment.MAX_LENGTH) else -1
    }

    override fun clearData() {
        commentField.setText("")
    }

    override fun getValue(): JSONObject? {

        if (isViewShown && viewEnabled) {
            val answer = getViewGeneralParameters()

            answer.put(SurveyKey.Page.View.Comment.VALUE, commentField.text.toString())


            return answer

        }
        return null
    }

    override fun disableView() {
        blurViewAsDisables()

        commentField.isEnabled = false
    }

    override fun enableView() {
        unBlurViewAsEnables()

        commentField.isEnabled = true
    }

    override fun isViewMandatoryAnswered(): Boolean {
        return true // We don't have any additional mandatory parameters
    }

    override fun updateView() {
        //No need
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if (commentField.text.toString().trim().equals("")){
            isViewAnswered = false
        }else{
            isViewAnswered = true
            removeMandatoryError()
        }
    }

    override fun afterTextChanged(p0: Editable?) {

    }
}