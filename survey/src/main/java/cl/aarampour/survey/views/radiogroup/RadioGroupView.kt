package cl.aarampour.survey.views.radiogroup

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import cl.aarampour.survey.R
import cl.aarampour.survey.SurveyKey
import cl.aarampour.survey.views.base.BaseView
import cl.aarampour.survey.views.base.IConditionalValueListener
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("ViewConstructor")
class RadioGroupView(
    context: Context,
    element: JSONObject,
    isViewEnabled: Boolean,
    position: Int,
    conditionalCallback: IConditionalValueListener
) : BaseView(context, element, isViewEnabled, position, conditionalCallback) {

    lateinit var radioButtonsById: HashMap<Int, RadioButton>
    lateinit var valueByIndex: HashMap<Int, String>
    lateinit var isVisibleById: HashMap<Int, Boolean>

    lateinit var ides: ArrayList<Int>
    lateinit var visibilityConditions: ArrayList<JSONObject>
    lateinit var visibilityButtons: ArrayList<RadioButton>
    lateinit var visibleConditionNames: ArrayList<String>

    lateinit var radioGroup: RadioGroup
    var choices: JSONArray? = null

    override fun getView(): Int {
        return R.layout.radio_group_layout
    }

    override fun getAnswer(answer: JSONObject) {
        if (answer.has(SurveyKey.Page.View.RadioGroup.INDEX)) {
            choosenIndex = answer.getInt(SurveyKey.Page.View.RadioGroup.INDEX)
            removeMandatoryError()
            isViewAnswered = true
        }
    }

    override fun initView(context: Context): LinearLayout {

        baseView?.let { safeBaseView ->
            radioGroup = safeBaseView.findViewById(R.id.group)

            addButtons()
        }

        return this

    }

    override fun getViewDataFromJson() {
        choices = elementJSONObject.getJSONArray(SurveyKey.Page.View.RadioGroup.CHOICES)
    }

    override fun clearData() {
        radioGroup.removeAllViews()
        radioButtonsById.clear()
        valueByIndex.clear()
        choosenIndex = -1
        isViewAnswered = false
        addButtons()
    }

    /**
     * How radiobutton acts :
     * Radio button's answer won't be removed unless it gets hidden from UI
     * If answer comes for an id we can overwrite it cause there is only one answer for each group
     */
    override fun getValue(): JSONObject? {
        if (isViewShown && viewEnabled) {
            if (choosenIndex >= 0) {
                val answer = getViewGeneralParameters()

                answer.put(SurveyKey.Page.View.RadioGroup.INDEX, choosenIndex)
                answer.put(SurveyKey.Page.View.RadioGroup.VALUE, valueByIndex.get(choosenIndex))

                return answer

            }

        }
        return null
    }

    override fun disableView() {
        viewEnabled = false
        blurViewAsDisables()
        for (id in ides) {
            val radioButton = radioButtonsById.get(id)
            radioButton?.let { safeRadioButton ->
                safeRadioButton.isEnabled = false
            }
        }

    }

    override fun enableView() {
        viewEnabled = true
        unBlurViewAsEnables()
        for (id in ides) {
            val radioButton = radioButtonsById.get(id)
            radioButton?.let { safeRadioButton ->
                safeRadioButton.isEnabled = true
            }
        }
    }

    override fun isViewMandatoryAnswered(): Boolean {
        return true
    }

    override fun updateView() {

    }

    private fun addButtons() {

        /**
         * init all arrays and maps
         */
        radioButtonsById = HashMap()
        valueByIndex = HashMap()
        isVisibleById = HashMap()

        ides = ArrayList()
        visibilityConditions = ArrayList()
        visibilityButtons = ArrayList()
        visibleConditionNames = ArrayList()

        choices?.let { safeChoices ->
            for (i in 0 until safeChoices.length()) {

                val choiceJson = safeChoices.getJSONObject(i)


                val radioButton = RadioButton(context)
                radioButton.setText(choiceJson.getString(SurveyKey.Page.View.RadioGroup.TEXT))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    radioButton.setTypeface(resources.getFont(R.font.q))
                }
                radioButton.isEnabled = viewEnabled
                radioButton.id = i
                if (i == choosenIndex) {
                    radioButton.isChecked = true
                }
                ides.add(i)
                radioGroup.addView(radioButton)
                //TODO visibility
                radioButtonsById.put(i, radioButton)
                valueByIndex.put(i, choiceJson.getString(SurveyKey.Page.View.RadioGroup.VALUE))

                radioGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
                    override fun onCheckedChanged(p0: RadioGroup?, checkedId: Int) {
                        choosenIndex = checkedId
                        removeMandatoryError()
                        getValue()?.let { safeValue ->
                            liveData.addCondition(pagePosition, safeValue)
                        }
                        isViewAnswered = true
                    }

                })
            }
        }
    }


}