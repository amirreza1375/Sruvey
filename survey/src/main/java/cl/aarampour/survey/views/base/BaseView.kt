package cl.aarampour.survey.views.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import cl.aarampour.survey.Functions.Companion.dpToPx
import cl.aarampour.survey.R
import cl.aarampour.survey.SurveyKey
import cl.aarampour.survey.livedata.ILiveDataDataChangeListener
import cl.aarampour.survey.livedata.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

open abstract class BaseView(context: Context) : LinearLayout(context) {

    lateinit var liveData: LiveData

    /**
     * General parameters
     */
    protected lateinit var elementJSONObject: JSONObject
    public var viewEnabled: Boolean = false
    protected var viewAnswer = JSONObject()
    protected var viewTitle: String = ""
    public var isMandatory = false
    protected var systemId = -1
    protected lateinit var viewType: String
    protected lateinit var viewId: String
    protected lateinit var viewName: String

    //    protected lateinit var viewTipo: String
    protected var pagePosition: Int = 0
    protected var viewPosition: Int = 0
    protected var isJustView = false

    //    protected var isConditionary = false
    private var isVisible = false

    /**
     * Conditional parameters
     * There parameters can disable , make mandatory or hide on some conditions
     * They have very bad logics to get conditions and that's why i separated them from other parameters
     */
    protected var isViewEnabledIf = false
    protected var isViewEnabledIfName: String = ""
    protected var isViewEnabledIfValue: Array<String>? = null
    protected var viewEnabledIf: String = ""

    protected var isViewVisibleIf = false
    protected var viewVisibleIf: String = ""
    protected var viewVisibleIfName: String = ""
    protected var viewVisibleIfValue: Array<String>? = null

    protected var viewRequiredIf: String = ""
    protected var viewRequiredIfName: String = ""
    protected var viewRequiredIfValue: String = ""
    protected var isViewRequiredIf = false

    //Important
    public var isViewAnswered = false // We must change this as answer changes
    public var isViewShown = true // We must change this as view hidden or shown
    protected var isRequiredIf =
        false // We must change this as view gets condition to become mandatory

    /**
     * RadioGroup
     */
    protected var choosenIndex = -1

    /**
     * Multitext
     */
    protected var isValid = false
    protected var hasValidation = false

    /**
     * Image files
     */

    /**
     * General views
     */
    protected var baseView: View? = null
    protected lateinit var parentView: LinearLayout

    /**
     * Callbacks
     */

    //Conditional RadioGroup , CheckBox , MultiText
    protected lateinit var conditionalCallback: IConditionalValueListener

    var titleTxt: TextView? = null
    var questionIdTxt: TextView? = null


    constructor(
        context: Context,
        elementJSONObject: JSONObject,
        elementEnabled: Boolean,
        position: Int,
        conditionalCallback: IConditionalValueListener
    ) : this(
        context
    ) {

        this.elementJSONObject = elementJSONObject
        this.viewEnabled = elementEnabled
        this.viewAnswer = viewAnswer
        this.conditionalCallback = conditionalCallback
        this.pagePosition = position

        liveData = LiveData.getNewInstance()

        baseView = getViewFromRes(getView())

        getDataFromJSON()

        getViewAnswer()

        getAnswer(viewAnswer)

        parentView = initView(context)

        initBaseView()

        if (!isVisible) {
            isViewShown = false
            visibility = GONE
        }

        setViewStatus()

        if (!viewEnabled){
            disableView()
        }

    }

    private fun getViewAnswer() {

        viewAnswer = liveData.getViewAnswer(pagePosition, viewId)

    }


    /**
     * Functions - General
     */

    //This function sets view status such as visible , required and enabled depend on data
    fun setViewStatus() {

        if (isViewVisibleIf) {
            hideView()
        }

        if (isViewEnabledIf) {
            disableView()
            viewEnabled = false
        }

    }

    //This function will return VIEW from the id integer given from custom views
    private fun getViewFromRes(viewRes: Int): View {
        val view = LayoutInflater.from(context).inflate(viewRes, this, false)
        return view
    }

    private fun getDataFromJSON() {
        viewType = if (elementJSONObject.has(SurveyKey.Page.View.TYPE)) elementJSONObject.getString(
            SurveyKey.Page.View.TYPE
        ) else ""

        viewTitle =
            if (elementJSONObject.has(SurveyKey.Page.View.TITLE)) elementJSONObject.getString(
                SurveyKey.Page.View.TITLE
            ) else ""
        if (viewTitle.equals("")) {//If there is not title means it's simple text view
            viewTitle =
                if (elementJSONObject.has(SurveyKey.Page.View.SimpleText.HTML)) elementJSONObject.getString(
                    SurveyKey.Page.View.SimpleText.HTML
                ) else ""
        }
        if (viewTitle.equals("")) {//No title assigned so get name instead
            viewTitle =
                if (elementJSONObject.has(SurveyKey.Page.View.NAME)) elementJSONObject.getString(
                    SurveyKey.Page.View.NAME
                ) else ""
        }

        viewId =
            if (elementJSONObject.has(SurveyKey.Page.View.ID)) elementJSONObject.getString(SurveyKey.Page.View.ID) else ""
        viewName = if (elementJSONObject.has(SurveyKey.Page.View.NAME)) elementJSONObject.getString(
            SurveyKey.Page.View.NAME
        ) else ""
        isMandatory =
            if (elementJSONObject.has(SurveyKey.Page.View.IS_MANDATORY)) elementJSONObject.getBoolean(
                SurveyKey.Page.View.IS_MANDATORY
            ) else false
        systemId =
            if (elementJSONObject.has(SurveyKey.Page.View.SYSTEM_ID)) elementJSONObject.getInt(
                SurveyKey.Page.View.SYSTEM_ID
            ) else -1
        isVisible =
            if (elementJSONObject.has(SurveyKey.Page.View.IS_VISIBLE)) elementJSONObject.getBoolean(
                SurveyKey.Page.View.IS_VISIBLE
            ) else true

        getEnableIf()
        getVisibleIf()
        getRequiredIf()

        getViewDataFromJson()
    }

    private fun initBaseView() {

        baseView?.let { safeBaseView ->

            titleTxt = safeBaseView.findViewById(R.id.titleText)
            questionIdTxt = safeBaseView.findViewById(R.id.questionIdTxt)

            titleTxt?.let { safeTitleTxt ->
                safeTitleTxt.setText(viewTitle)

                if (isMandatory) {
                    safeTitleTxt.setText(safeTitleTxt.text.toString() + " * ")
                }
                if (systemId > 0) {
                    safeTitleTxt.setText(systemId.toString() + " - " + safeTitleTxt.text.toString())
                }
            }


            val params = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(
                dpToPx(0, context),
                dpToPx(4, context),
                dpToPx(0, context),
                dpToPx(0, context)
            )
            this.layoutParams = params

            val cardView = createCardView()

            cardView.addView(baseView)

            parentView.addView(cardView)

        }

    }

    fun createCardView(): CardView {
        val cardView = CardView(context)
        cardView.cardElevation = dpToPx(3, context).toFloat()
        cardView.radius = dpToPx(8, context).toFloat()
        val cardParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        cardParams.setMargins(
            dpToPx(5, context),
            dpToPx(5, context),
            dpToPx(5, context),
            dpToPx(5, context)
        )
        cardView.layoutParams = cardParams
        return cardView
    }

    protected fun getViewGeneralParameters(): JSONObject {

        val params = JSONObject()

        params.put(SurveyKey.Page.View.TYPE, viewType)
        params.put(SurveyKey.Page.View.ID, viewId)
        params.put(SurveyKey.Page.View.NAME, viewName)
        params.put(SurveyKey.Page.PAGE_POSITION, pagePosition)

        return params

    }

    protected fun removeMandatoryError() {
            baseView?.background = null


    }

    protected fun setMandatoryError() {
            baseView?.background = resources.getDrawable(R.drawable.mandatory_error_background)

    }

    public fun isMandatoryAnswered(): Boolean {

        if (!isViewShown)
            return true

        var isParentDone = true

        if (isMandatory) {

            if (viewEnabled) {
                if (!isViewAnswered)
                    setMandatoryError()

                isParentDone = isViewAnswered
            } else {
                isParentDone = true
            }

        } else {
            var errors = 0

            if (hasValidation) {
                if (!isValid) {
                    errors++
                }
            }

            if (isRequiredIf) {
                if (isViewAnswered) {
                    errors++
                }
            }

            if (errors > 0) {
                setMandatoryError()
                isParentDone = false
            }

        }

        if (isParentDone) {//This flag will give us general rules about all views
            if (isViewMandatoryAnswered()) {//This one will get child rules from each view
                return true
            }
        }

        return false
    }

    protected fun blurViewAsDisables() {
        alpha = 0.5f
    }

    protected fun unBlurViewAsEnables() {
        alpha = 1f
    }

    fun hideView() {
        isViewShown = false
        this.visibility = GONE
    }

    fun showView() {
        isViewShown = true
        this.visibility = VISIBLE
    }


    /**
     * Conditional parameters for view
     */

    //region Conditionals

    private fun getEnableIf() {
        try {
            isViewEnabledIf = elementHasEnableSi(elementJSONObject)
            if (isViewEnabledIf) {
                viewEnabledIf = elementJSONObject.getString(SurveyKey.Page.View.ENABLE_IF)
                var elementVisibleSiArray: Array<String> = viewEnabledIf.split("=").toTypedArray()
                if (elementVisibleSiArray.size < 2) {
                    elementVisibleSiArray = viewEnabledIf.split("contains").toTypedArray()
                }
                if (elementVisibleSiArray.size == 2) {
                    isViewEnabledIfName =
                        elementVisibleSiArray[0].substring(1, elementVisibleSiArray[0].length - 2)
                    isViewEnabledIfValue = getVisibleSiValue(elementVisibleSiArray[1])
//                    callBack.isHiddenView()
                } else {
                    isViewEnabledIfName = ""
                    isViewEnabledIfValue = arrayOf<String>()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getVisibleSiValue(s: String): Array<String> {
        var s = s
        try {
            s = s.trim { it <= ' ' }
            if (s.length > 0) {
                val startChar = s[0]
                return if (startChar != '[') {
                    //RadioGroup
                    arrayOf(s)
                } else {
                    //CheckBox
                    val withOutBracets = s.substring(1, s.length - 1)
                    withOutBracets.split(",").toTypedArray()
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return arrayOf()
    }

    private fun elementHasEnableSi(element: JSONObject): Boolean {
        // Shitty logic
        if (!element.has(SurveyKey.Page.View.ENABLE_IF)) return false
        val tempVisibleSi = element.getString(SurveyKey.Page.View.ENABLE_IF)
        var elementVisibleSiArray = tempVisibleSi.split("=").toTypedArray()
        if (elementVisibleSiArray.size < 2) {
            elementVisibleSiArray = tempVisibleSi.split("contains").toTypedArray()
        }
        if (elementVisibleSiArray.size == 2) {
            val tempName =
                elementVisibleSiArray[0].substring(1, elementVisibleSiArray[0].length - 2)
            return tempName != "Correcto"
        }
        return false
    }

    private fun getVisibleIf() {
        try {
            isViewVisibleIf = elementHasVisibleSi(elementJSONObject)
            if (isViewVisibleIf) {
                viewVisibleIf = elementJSONObject.getString(SurveyKey.Page.View.VISIBLE_IF)
                var elementVisibleSiArray: Array<String> =
                    viewVisibleIf.split("=").toTypedArray()
                if (elementVisibleSiArray.size < 2) {
                    elementVisibleSiArray = viewVisibleIf.split("contains").toTypedArray()
                }
                if (elementVisibleSiArray.size == 2) {
                    viewVisibleIfName =
                        elementVisibleSiArray[0].substring(1, elementVisibleSiArray[0].length - 2)
                    viewVisibleIfValue = getVisibleSiValue(elementVisibleSiArray[1])
//                    callBack.isHiddenView()
                } else {
                    viewVisibleIfName = ""
                    viewVisibleIfValue = arrayOf<String>()
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun elementHasVisibleSi(element: JSONObject): Boolean {
        if (!element.has(SurveyKey.Page.View.VISIBLE_IF)) return false
        val tempVisibleSi = element.getString(SurveyKey.Page.View.VISIBLE_IF)
        var elementVisibleSiArray = tempVisibleSi.split("=").toTypedArray()
        if (elementVisibleSiArray.size < 2) {
            elementVisibleSiArray = tempVisibleSi.split("contains").toTypedArray()
        }
        if (elementVisibleSiArray.size == 2) {
            val tempName =
                elementVisibleSiArray[0].substring(1, elementVisibleSiArray[0].length - 2)
            return tempName != "Correcto"
        }
        return false
    }

    private fun getRequiredIf() {
        if (elementJSONObject.has(SurveyKey.Page.View.REQUIRED_IF)) {
            isViewRequiredIf = true
            try {
                viewRequiredIf = elementJSONObject.getString(SurveyKey.Page.View.REQUIRED_IF)
                var elementRequiredIfArray: Array<String> =
                    viewRequiredIf.split("=").toTypedArray()
                if (elementRequiredIfArray.size < 2) {
                    elementRequiredIfArray = viewRequiredIf.split("contains").toTypedArray()
                }
                if (elementRequiredIfArray.size == 2) {
                    viewRequiredIfName =
                        elementRequiredIfArray[0].substring(1, elementRequiredIfArray[0].length - 2)
                    viewRequiredIfValue =
                        getVisibleSiValue(elementRequiredIfArray[1])[0].replace("'", "")
//                    callBack.isHiddenView()
                } else {
                    viewRequiredIfName = ""
                    viewRequiredIfValue = ""
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    //endregion

    /**
     * Abstract Functions - General
     */

    public abstract fun getView(): Int  // Child view will return view res id
    public abstract fun getAnswer(answer: JSONObject) // Child view will return answers from answer json given
    public abstract fun initView(context: Context): LinearLayout// This will prepare depend on view needs and return LinearLayout to add to car view
    public abstract fun getViewDataFromJson()//To get additional data for each view
    public abstract fun clearData() // Clear data and answers
    public abstract fun getValue(): JSONObject?// Get answer of view
    public abstract fun disableView() // Works with enable if
    public abstract fun enableView() // Works with enable if
    public abstract fun isViewMandatoryAnswered(): Boolean //This will be additional to check other mandatory flags in each view depend on own conditions
    public abstract fun updateView() // Update view by the live data changes

    /**
     *
     */

    fun triggerAnswerCondition() {
        if (viewType.equals(SurveyKey.TYPE.RADIO_GROUP)) {
            if (viewAnswer.has(SurveyKey.Page.View.NAME))
                liveData.addCondition(pagePosition,viewAnswer)
        }
    }

    fun onConditionChanged(condition: JSONObject) {
        val conditionName = condition.getString(SurveyKey.Page.View.NAME)
        val conditionType = condition.getString(SurveyKey.Page.View.TYPE)
        if (isViewVisibleIf) {

            if (conditionName.equals(viewVisibleIfName)) {

                if (conditionType
                        .equals(SurveyKey.TYPE.RADIO_GROUP)
                ) {

                    val conditionValue = condition.getString(SurveyKey.Page.View.RadioGroup.VALUE)

                    if (viewVisibleIfValue!!.contains(conditionValue)) {
                        showView()
                    } else {
                        hideView()
                        clearData()
                    }

                }else if (conditionType.equals(SurveyKey.TYPE.CHECK_BOX)){
                    val conditionValue = condition.getString(SurveyKey.Page.View.CheckBox.VALUE)
                    if (viewVisibleIfValue!!.contains(conditionValue)){
                        if (condition.getBoolean(SurveyKey.Page.View.CheckBox.STATUS)){
                            showView()
                        }else{
                            hideView()
                            clearData()
                        }
                    }
                }

            }

        }

        if (isViewEnabledIf) {
            if (conditionName.equals(isViewEnabledIfName)) {

                if (conditionType
                        .equals(SurveyKey.TYPE.RADIO_GROUP)
                ) {

                    val conditionValue = condition.getString(SurveyKey.Page.View.RadioGroup.VALUE)

                    if (isViewEnabledIfValue!!.contains(conditionValue)) {
                        viewEnabled = true
                        enableView()
                    } else {
                        viewEnabled = false
                        disableView()
                        clearData()
                    }

                }else if (conditionType.equals(SurveyKey.TYPE.CHECK_BOX)){
                    val conditionValue = condition.getString(SurveyKey.Page.View.CheckBox.VALUE)
                    if (viewVisibleIfValue!!.contains(conditionValue)){
                        if (condition.getBoolean(SurveyKey.Page.View.CheckBox.STATUS)){
                            viewEnabled = false
                            enableView()
                        }else{
                            viewEnabled = false
                            disableView()
                            clearData()
                        }
                    }
                }

            }
        }

        if (isViewRequiredIf) {
            if (conditionName.equals(viewRequiredIfName)) {

                if (conditionType
                        .equals(SurveyKey.TYPE.RADIO_GROUP)
                ) {

                    val conditionValue = condition.getString(SurveyKey.Page.View.RadioGroup.VALUE)

                    if (viewRequiredIfValue.equals(conditionValue)) {
                        isMandatory = true
                        titleTxt?.setText(titleTxt?.text.toString() + "*")
                    } else {
                        isMandatory = false
                        titleTxt?.setText(titleTxt?.text.toString().replace("*", ""))
                    }


                }else if (conditionType.equals(SurveyKey.TYPE.CHECK_BOX)){
                    val conditionValue = condition.getString(SurveyKey.Page.View.CheckBox.VALUE)
                    if (viewRequiredIfValue.equals(conditionValue)){
                        if (condition.getBoolean(SurveyKey.Page.View.CheckBox.STATUS)){
                            isMandatory = true
                            titleTxt?.setText(titleTxt?.text.toString() + "*")
                        }else{
                            isMandatory = false
                            titleTxt?.setText(titleTxt?.text.toString().replace("*", ""))
                        }
                    }
                }

            }
        }

    }

}