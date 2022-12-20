package cl.aarampour.survey.views.checkbox

import android.content.Context
import android.os.Build
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import cl.aarampour.survey.R
import cl.aarampour.survey.SurveyKey
import cl.aarampour.survey.views.base.BaseView
import cl.aarampour.survey.views.base.IConditionalValueListener
import org.json.JSONArray
import org.json.JSONObject

class CheckBoxView(
    context: Context, element: JSONObject,
    isViewEnabled: Boolean,
    position: Int,
    conditionalCallback: IConditionalValueListener
) : BaseView(context, element, isViewEnabled, position, conditionalCallback) {

    private var min = -1
    private var max: Int = -1
    private var disableOthers = -1

    private lateinit var answers: ArrayList<String>
    private lateinit var checkboxes: ArrayList<CheckBox>
    private lateinit var statusByCheckBoxId: HashMap<Int, Boolean>
    private lateinit var valueByIndex: HashMap<Int, String>

    private lateinit var footer: LinearLayout
    private lateinit var holder: LinearLayout
    private lateinit var minTxt: TextView
    private lateinit var maxTxt: TextView

    var choices: JSONArray = JSONArray()

    override fun getView(): Int {
        return R.layout.checkbox_group_layout
    }

    override fun getAnswer(answer: JSONObject) {
        answers = ArrayList()
        try {
            val answersArr = answer.getJSONArray(SurveyKey.Page.View.CheckBox.VALUE)
            for (i in 0 until answersArr.length()) {

                val answerObj = answersArr.getJSONObject(i)

                if (answerObj.getBoolean(SurveyKey.Page.View.CheckBox.STATUS)) {
                    answers.add(answerObj.getString(SurveyKey.Page.View.CheckBox.VALUE))
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun initView(context: Context): LinearLayout {


        baseView?.let { safeBaseView ->
            footer = safeBaseView.findViewById(R.id.footer)
            holder = safeBaseView.findViewById(R.id.checkboxGroupContainer)
            minTxt = safeBaseView.findViewById(R.id.minTxt)
            maxTxt = safeBaseView.findViewById(R.id.maxTxt)

            addButtons()
        }
        return this
    }

    override fun getViewDataFromJson() {
        disableOthers =
            if (elementJSONObject.has(SurveyKey.Page.View.CheckBox.DISABLE_OTHERS)) elementJSONObject.getInt(
                SurveyKey.Page.View.CheckBox.DISABLE_OTHERS
            ) else -1
        choices =
            if (elementJSONObject.has(SurveyKey.Page.View.CheckBox.CHOICES)) elementJSONObject.getJSONArray(
                SurveyKey.Page.View.CheckBox.CHOICES
            ) else JSONArray()

    }

    override fun clearData() {
        for (checkBox in checkboxes) {
            if (checkBox.isChecked) {
                checkBox.isChecked = false
            }
        }
    }

    override fun getValue(): JSONObject? {
        if (isViewShown && viewEnabled) {
            val answer = getViewGeneralParameters()

            try {

                val answersArr = JSONArray()

                for ((key, value) in valueByIndex) {
                    statusByCheckBoxId.get(key)?.let { safeStatusById ->
                        if (safeStatusById) {
                            val answerObj = JSONObject()

                            answerObj.put(SurveyKey.Page.View.CheckBox.INDEX, key)
                            answerObj.put(SurveyKey.Page.View.CheckBox.VALUE, value)
                            answerObj.put(
                                SurveyKey.Page.View.CheckBox.STATUS,
                                statusByCheckBoxId.get(key)
                            )

                            answersArr.put(answerObj)
                        }
                    }

                }

                answer.put(SurveyKey.Page.View.CheckBox.VALUE, answersArr)

            } catch (e: Exception) {
                e.printStackTrace()
            }
            return answer
        }
        return null
    }


    override fun disableView() {
        blurViewAsDisables()
        for (checkBox in checkboxes) {
            checkBox.isEnabled = false
        }
    }

    override fun enableView() {
        unBlurViewAsEnables()
        for (checkBox in checkboxes) {
            checkBox.isEnabled = true
        }
    }

    override fun isViewMandatoryAnswered(): Boolean {
        return true
    }

    override fun updateView() {

    }

    private fun addButtons() {

        valueByIndex = HashMap()
        checkboxes = ArrayList()
        statusByCheckBoxId = HashMap()

        for (i in 0 until choices.length()) {

            val choiceObj = choices.getJSONObject(i)

            val checkBox = CheckBox(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                checkBox.setTypeface(resources.getFont(R.font.q))
            }

            checkBox.isEnabled = viewEnabled

            checkBox.setText(choiceObj.getString(SurveyKey.Page.View.CheckBox.TEXT))

            checkBox.id = i
            val value = choiceObj.getString(SurveyKey.Page.View.CheckBox.VALUE)
            valueByIndex.put(i, value)

            statusByCheckBoxId.put(i, false)

            if (isAnswerExist(value)) {
                checkBox.isChecked = true
                isViewAnswered = true
                statusByCheckBoxId.put(i, true)

                if (disableOthers >= 0) {
                    if (value.equals(disableOthers.toString())) {
                        disableOthersById(value, true)
                    }
                }

            }

            checkboxes.add(checkBox)
            setMinMaxId(i)

            checkBox.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(checkbox: CompoundButton?, status: Boolean) {
                    checkbox?.let { safeCheckBox ->
                        removeMandatoryError()
                        statusByCheckBoxId.put(safeCheckBox.id, status)
                        val tempVal = valueByIndex.get(safeCheckBox.id)
                        if (tempVal.equals(disableOthers.toString())) {
                            disableOthersById(tempVal!!, status)
                        }
                        setAnsweredStatus()
                        liveData.addCondition(pagePosition, getCondition(checkbox.id, status))
                    }

                }

            })

            holder.addView(checkBox)

        }

    }

    private fun setAnsweredStatus() {
        var isAnswered = false
        for ((key,value) in statusByCheckBoxId){
            if (value){
                isAnswered = true
                removeMandatoryError()
                break
            }
        }
        isViewAnswered = isAnswered
    }

    private fun getCondition(id: Int, status: Boolean): JSONObject {
        val condition = getViewGeneralParameters()

        condition.put(SurveyKey.Page.View.CheckBox.VALUE, valueByIndex.get(id))
        condition.put(SurveyKey.Page.View.CheckBox.STATUS, status)

        return condition
    }

    private fun isAnswerExist(value: String): Boolean {

        for (answerValue in answers) {
            if (answerValue.equals(value)) {
                return true
            }
        }

        return false
    }

    private fun setMinMaxId(id: Int) {
        //first check if assigned before
        if (min == -1) { //not assigned
            min = id
        }
        if (max == -1) { //not assigned
            max = id
        }
        //if assigned and lower that min assign to min
        if (min > id) {
            min = id
        }
        //if assigned and higher than max assign to max
        if (max < id) {
            max = id
        }
    }

    private fun disableOthersById(id: String, isChecked: Boolean) {
        for (i in checkboxes.indices) {
            val checkBoxId = valueByIndex[checkboxes.get(i).getId()]
            if (checkBoxId != id) {
                checkboxes.get(i).setEnabled(!isChecked)
                checkboxes.get(i).setChecked(false)
                statusByCheckBoxId.put(checkboxes.get(i).getId(), false)
            }
        }
    }
}