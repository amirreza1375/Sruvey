package cl.aarampour.survey.page

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import cl.aarampour.survey.R
import cl.aarampour.survey.SurveyKey
import cl.aarampour.survey.livedata.ILiveDataDataChangeListener
import cl.aarampour.survey.livedata.LiveData
import cl.aarampour.survey.views.base.BaseView
import cl.aarampour.survey.views.base.IConditionalValueListener
import cl.aarampour.survey.views.checkbox.CheckBoxView
import cl.aarampour.survey.views.comment.CommentView
import cl.aarampour.survey.views.image.ImagesTakerView
import cl.aarampour.survey.views.multitext.MultiTextView
import cl.aarampour.survey.views.radiogroup.RadioGroupView
import cl.aarampour.survey.views.simpletext.SimpleTextView
import cl.aarampour.survey.views.sliderview.ImageModel
import cl.aarampour.survey.views.sliderview.SliderView

import org.json.JSONArray
import org.json.JSONObject

class PageView(context: Context) : LinearLayout(context), IConditionalValueListener,
    ILiveDataDataChangeListener {

    /**
     * Important parameters
     */
    lateinit var pageJson: JSONObject
    var views = ArrayList<BaseView>()
    lateinit var liveData: LiveData
    var position = 0

    /**
     * CallBacks
     */
    lateinit var callBack: IPageViewListener

    /**
     * Views
     */
    lateinit var pageViewContainer: LinearLayout
    lateinit var scrollView: ScrollView

    private var isPreview = false

    constructor(
        context: Context,
        pageJson: JSONObject,
        position: Int, isPreview: Boolean,
        callBack: IPageViewListener
    ) : this(context) {
        this.callBack = callBack
        this.pageJson = pageJson
        this.position = position
        this.isPreview = isPreview

        liveData = LiveData.getNewInstance()
        liveData.attachCallBack(this)

        initView()

        addQuestions()

    }

    private fun addQuestions() {


        val questions = getQuestions()

        for (baseView in questions) {

            pageViewContainer.addView(baseView)

        }

        triggerConditionalAnswers()


        callBack.onPageLoadFinished()


    }

    private fun getQuestions(): ArrayList<BaseView> {

        val questions = ArrayList<BaseView>()

        for (i in 0 until pageJson.getJSONArray(SurveyKey.Page.ELEMENTS).length()) {
            val elementJson = pageJson.getJSONArray(SurveyKey.Page.ELEMENTS).getJSONObject(i)

            val currentType = elementJson.getString(SurveyKey.Page.View.TYPE)
            var baseView: BaseView? = null

            if (currentType.equals(SurveyKey.TYPE.RADIO_GROUP)) {
                baseView = RadioGroupView(context, elementJson, !isPreview, position, this)
            } else if (currentType.equals(SurveyKey.TYPE.CHECK_BOX)) {
                baseView = CheckBoxView(context, elementJson, !isPreview, position, this)
            } else if (currentType.equals(SurveyKey.TYPE.IMAGE_TAKER)) {
                baseView = ImagesTakerView(context, elementJson, !isPreview, position, this)
            } else if (currentType.equals(SurveyKey.TYPE.COMMENT)) {
                baseView = CommentView(context, elementJson, !isPreview, position, this)
            } else if (currentType.equals(SurveyKey.TYPE.SIMPLE_TEXT) || currentType.equals(
                    SurveyKey.TYPE.HTML
                )
            ) {
                baseView = SimpleTextView(context, elementJson, !isPreview, position, this)
            } else if (currentType.equals(SurveyKey.TYPE.MULTI_TEXT)) {
                baseView = MultiTextView(context, elementJson, !isPreview, position, this)
            } else if (currentType.equals(SurveyKey.TYPE.IMAGE_SLIDER)) {
                if (elementJson.getString(SurveyKey.TYPE.IMAGE_SLIDER_TYPE)
                        .equals(SurveyKey.TYPE.IMAGE_SLIDER_TYPE_OPTICO)
                ) {
                    baseView = SliderView(context, elementJson, !isPreview, position, this)
                }
            }

            baseView?.let { safeBaseView ->
                views.add(safeBaseView)
                questions.add(safeBaseView)
            }

        }

        return questions
    }

    private fun triggerConditionalAnswers() {
        for (view in views) {
            view.triggerAnswerCondition()
        }
    }

    /**
     * Just add view props and prepare
     */

    private fun initView() {
        callBack.onPageLoadStarted()

        //This view as LinearLayout
        val params =
            LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams = params
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL

        val view =
            LayoutInflater.from(context).inflate(R.layout.page_view_layout, this, false)

        addView(view)
        view.findViewById<TextView>(R.id.pageTitleTxt)
            .setText(if (pageJson.has(SurveyKey.Page.TITLE)) pageJson.getString(SurveyKey.Page.TITLE) else "")

        this.pageViewContainer = view.findViewById(R.id.pageViewContainer)
        this.scrollView = view.findViewById(R.id.scrollView)

    }

    /**
     * Conditional call backs
     */

    //Radiogroup

    override fun onRadioGroupConditionalValueChanged(data: JSONObject) {


    }

    override fun onConditionChanged(condition: JSONObject) {

        for (view in views) {

            view.onConditionChanged(condition)

        }

    }

    override fun onImageChanged() {
        super.onImageChanged()

        for (view in views) {

            view.updateView()

        }

    }

    fun isMandatoryAnswered(): Boolean {

        for (view in views) {

            if (!view.isMandatoryAnswered()) {
                scrollView.requestChildFocus(
                    view,
                    view
                )//Bring up not answered question to user in long surveys

                return false

            }

        }
        return true
    }

    fun getAnswers(): JSONArray {

        val answers = JSONArray()

        for (view in views) {
            view.getValue()?.let { safeValue ->
                answers.put(safeValue)
            }
        }

        return answers
    }

}