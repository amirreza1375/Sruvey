package cl.aarampour.survey.views.multitext

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import cl.aarampour.survey.R
import cl.aarampour.survey.SurveyKey
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject

class MultiTextItemView(context: Context?) : LinearLayout(context), TextWatcher {

    private lateinit var itemJSON: JSONObject

    public lateinit var name: String
    private lateinit var title: String
    private var hasValidation = false
    private var validatorType: Int = InputType.TYPE_CLASS_TEXT
    private var validatorText = context?.resources?.getString(R.string.commmentHint)
    private var validatorMin = -1
    private var validatorMax = -1
    private lateinit var acceptableValues: ArrayList<Int>

    private lateinit var commentField: TextInputEditText

    private lateinit var callBack: IMultiTextItemCallBack

    private var isMandatory = false

    constructor(
        context: Context,
        isMandatory: Boolean,
        itemJson: JSONObject,
        acceptableValues: ArrayList<Int>,
        callBack: IMultiTextItemCallBack
    ) : this(context) {
        this.itemJSON = itemJson
        this.callBack = callBack
        this.isMandatory = isMandatory
        this.acceptableValues = acceptableValues

        getParamsFromJSON()

        initView()


    }

    fun setAnswer(value: String) {
        commentField.setText(value)
    }

    fun getValue(): JSONObject {

        val answer = JSONObject()

        answer.put(SurveyKey.Page.View.MultiText.Item.NAME, name)
        answer.put(SurveyKey.Page.View.MultiText.Item.VALUE, commentField.text.toString())

        return answer
    }

    fun getIntValue() : Int{
        try{
            val intValue = commentField.text.toString().toInt()
            return intValue
        }catch (e : Exception){
            e.printStackTrace()
        }
        return -1
    }

    /**
     * Here we use acceptable values from json of server to check it user
     * used right numbers
     *
     * @return
     */
    private fun isValueAcceptable(): Boolean {
        if (acceptableValues.size <= 0) return true
        try {
            val edtValue: Int = commentField.getText().toString().toInt()
            var isInList = false
            for (i in acceptableValues.indices) {
                val acceptableValue = acceptableValues[i]
                if (acceptableValue == edtValue) {
                    isInList = true
                    break //Here means that value is acceptable and we ignore rest of array to check
                }
            } //Checking value with array FINISHED HERE
            if (!isInList) //If second loop can't match value with array will have false isInList
                return false //So we need to warn user with wrong input
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true //If we got here means all are acceptable
    }

    fun isValid(): Boolean {

        if (isMandatory) {
            if (commentField.text.toString().trim().equals("")) {
                return false
            }
        }

        if (hasValidation) {
            if (commentField.getText().toString().trim { it <= ' ' } == "") {
                return true
            }
            if (validatorType == InputType.TYPE_CLASS_NUMBER) {
                var valueNumber = -1
                valueNumber = try {
                    commentField.getText().toString().trim { it <= ' ' }.toInt()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    setInputError(context.getString(R.string.rangeError))
                    return false
                }
                var isMinOk = false
                var isMaxOk = false
                if (validatorMin != -1) {
                    if (valueNumber >= validatorMin) {
                        isMinOk = true
                    }
                } else {
                    isMinOk = true
                }
                if (validatorMax != -1) {
                    if (valueNumber <= validatorMax) {
                        isMaxOk = true
                    }
                } else {
                    isMaxOk = true
                }
                if (!isMaxOk || !isMinOk) {
                    setInputError(context.getString(R.string.rangeError))
                    return false
                }
            }
        }

        if (!isValueAcceptable()) {
            setInputError(context.getString(R.string.valueIsNotAcceptable))
            return false
        }

        return true
    }

    private fun initView() {

        val view = LayoutInflater.from(context).inflate(R.layout.multitext_layout_item, this, false)

        val commentFieldContainer = view.findViewById<TextInputLayout>(R.id.commentFieldContainer)
        commentFieldContainer.setHint(validatorText)
        val rangeTxt = view.findViewById<TextView>(R.id.rangeTxt)

        if (hasValidation)
            rangeTxt.visibility = VISIBLE

        if (validatorMin != -1 || validatorMax != -1) {
            var range = ""
            if (validatorMin >= 0) {
                range = "MIN = $validatorMin"
            }
            if (validatorMax >= 0) {
                range = "$range  MAX = $validatorMax"
            }
            rangeTxt.setText(range)
        }

        commentField = view.findViewById(R.id.commentField)

        commentField.inputType = validatorType

        commentField.addTextChangedListener(this)

        this.addView(view)

    }

    private fun getParamsFromJSON() {

        try {

            name = if (itemJSON.has(SurveyKey.Page.View.MultiText.Item.NAME)) itemJSON.getString(
                SurveyKey.Page.View.MultiText.Item.NAME
            ) else ""
            title = if (itemJSON.has(SurveyKey.Page.View.MultiText.Item.TITLE)) itemJSON.getString(
                SurveyKey.Page.View.MultiText.Item.TITLE
            ) else ""

            if (itemJSON.has(SurveyKey.Page.View.MultiText.Item.VALIDATORS)) {
                hasValidation = true
                val validatorJson =
                    itemJSON.getJSONArray(SurveyKey.Page.View.MultiText.Item.VALIDATORS)
                        .getJSONObject(0)

                validatorText =
                    if (validatorJson.has(SurveyKey.Page.View.MultiText.Item.Validators.TEXT)) validatorJson.getString(
                        SurveyKey.Page.View.MultiText.Item.Validators.TEXT
                    ) else context.resources.getString(R.string.commmentHint)
                validatorMin =
                    if (validatorJson.has(SurveyKey.Page.View.MultiText.Item.Validators.MIN)) validatorJson.getInt(
                        SurveyKey.Page.View.MultiText.Item.Validators.MIN
                    ) else -1
                validatorMax =
                    if (validatorJson.has(SurveyKey.Page.View.MultiText.Item.Validators.MAX)) validatorJson.getInt(
                        SurveyKey.Page.View.MultiText.Item.Validators.MAX
                    ) else -1

                if (validatorJson.has(SurveyKey.Page.View.MultiText.Item.Validators.INPUT_TYPE)) {

                    if (validatorJson.getString(SurveyKey.Page.View.MultiText.Item.Validators.INPUT_TYPE)
                            .equals(SurveyKey.Page.View.MultiText.Item.Validators.NUMBER)
                    ) {
                        validatorType = InputType.TYPE_CLASS_NUMBER
                    }
                }

            }

            if (acceptableValues.size > 0) {//Of we have acceptable values means input must be number
                validatorType = InputType.TYPE_CLASS_NUMBER
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun setInputError(msg: String) {
        commentField.error = msg
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        callBack.onValueChanged()
    }

    override fun afterTextChanged(p0: Editable?) {

    }

    fun disable() {
        commentField.isEnabled = false
    }
    fun enable() {
        commentField.isEnabled = true
    }
}