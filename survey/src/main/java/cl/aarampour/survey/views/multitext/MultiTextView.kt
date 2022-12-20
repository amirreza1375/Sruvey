package cl.aarampour.survey.views.multitext

import android.content.Context
import android.widget.LinearLayout
import androidx.core.view.get
import cl.aarampour.survey.R
import cl.aarampour.survey.SurveyKey
import cl.aarampour.survey.views.base.BaseView
import cl.aarampour.survey.views.base.IConditionalValueListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MultiTextView(
    context: Context,
    elementJSONObject: JSONObject,
    elementEnabled: Boolean,
    position: Int,
    conditionalCallback: IConditionalValueListener
) : BaseView(context, elementJSONObject, elementEnabled, position, conditionalCallback),
    IMultiTextItemCallBack {

    private lateinit var itemsContainer: LinearLayout
    private var isVertical = false
    private lateinit var items: JSONArray
    private lateinit var multiTextItems: ArrayList<MultiTextItemView>
    private lateinit var answers: JSONArray
    private lateinit var acceptableValues: ArrayList<Int>

    override fun getView(): Int {
        return R.layout.multitext_layout
    }

    override fun getAnswer(answer: JSONObject) {

        if (answer.has(SurveyKey.Page.View.MultiText.VALUE)) {

            answers = answer.getJSONArray(SurveyKey.Page.View.MultiText.VALUE)

        } else {
            answers = JSONArray()
        }

    }

    override fun initView(context: Context): LinearLayout {
        baseView?.let { safeBaseView ->
            itemsContainer = safeBaseView.findViewById(R.id.itemsContainer)

            if (isVertical)
                itemsContainer.orientation = VERTICAL

            addItems()

        }

        return this
    }

    private fun addItems() {
        multiTextItems = ArrayList()
        for (i in 0 until items.length()) {

            val item = items.getJSONObject(i)

            val multiTextItem =
                MultiTextItemView(context, isMandatory, item, acceptableValues, this)

            itemsContainer.addView(multiTextItem)

            multiTextItems.add(multiTextItem)

            for (j in 0 until answers.length()) {

                val value = answers.getJSONObject(j)

                val name = value.getString(SurveyKey.Page.View.MultiText.Item.NAME)
                val itemValue = value.getString(SurveyKey.Page.View.MultiText.Item.VALUE)


                if (multiTextItem.name.equals(name)) {

                    multiTextItem.setAnswer(itemValue)

                    break
                }

            }
        }

    }

    override fun getViewDataFromJson() {
        isVertical =
            elementJSONObject.has(SurveyKey.Page.View.MultiText.PREVIEW) && elementJSONObject.getString(
                SurveyKey.Page.View.MultiText.PREVIEW
            ).equals(SurveyKey.Page.View.MultiText.VERTICAL)

        items =
            if (elementJSONObject.has(SurveyKey.Page.View.MultiText.ITEMS)) elementJSONObject.getJSONArray(
                SurveyKey.Page.View.MultiText.ITEMS
            ) else JSONArray()

        getAcceptableValues()
    }

    private fun getAcceptableValues() {
        acceptableValues = ArrayList()
        //Check type to accept only multiple text
        try {
            if (elementJSONObject.has(SurveyKey.Page.View.MultiText.ACCEPTABLE_VALUES)) { //Check to have acceptable values key
                try {
                    //get acceptable keys as string from element
                    val acceptableValuesStr =
                        elementJSONObject.getString(SurveyKey.Page.View.MultiText.ACCEPTABLE_VALUES)
                    //Then convert it to array
                    val acceptableValuesArr: Array<String> =
                        acceptableValuesStr.split(",").toTypedArray()
                    //Parse stirng array
                    for (i in acceptableValuesArr.indices) {
                        val acceptableValueStr = acceptableValuesArr[i]
                        //Convert value to integer
                        val acceptableValue = acceptableValueStr.toInt()
                        //Add it to array
                        acceptableValues.add(acceptableValue)
                    }

                    //DONE
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

        } catch (e: JSONException) {
            e.printStackTrace() //Means there is not type for element from server and this is very bad
        }
    }

    override fun clearData() {
    }

    override fun getValue(): JSONObject? {
        if (isViewShown && viewEnabled) {
            val answer = getViewGeneralParameters()

            val values = JSONArray()

            for (i in 0 until multiTextItems.size) {
                val mt = multiTextItems.get(i)
                values.put(mt.getValue())

                if (mt.getIntValue() > 0) {
                    liveData.addCondition(pagePosition, getCondition(mt.getIntValue()))
                }

            }

            answer.put(SurveyKey.Page.View.MultiText.VALUE, values)

            return answer
        }
        return null
    }

    private fun getCondition(value: Int): JSONObject {
        val condition = getViewGeneralParameters()

        condition.put(SurveyKey.Page.View.MultiText.VALUE, value)

        return condition
    }

    override fun disableView() {
        blurViewAsDisables()
        for (mt in multiTextItems){
            mt.disable()
        }
    }

    override fun enableView() {
        unBlurViewAsEnables()
        for (mt in multiTextItems){
            mt.enable()
        }
    }

    override fun isViewMandatoryAnswered(): Boolean {
        for (mt in multiTextItems) {

            if (!mt.isValid())
                return false
        }
        return true
    }

    override fun updateView() {
    }

    override fun onValueChanged() {
        var isOk = true
        for (mt in multiTextItems) {

            if (!mt.isValid()) {
                isOk = false
                break
            }
        }

        if (isOk) {
            isViewAnswered = true
            removeMandatoryError()
        } else {
            isViewAnswered = false
        }
    }
}