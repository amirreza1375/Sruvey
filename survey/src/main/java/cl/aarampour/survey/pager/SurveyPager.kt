package cl.aarampour.survey.pager

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import cl.aarampour.survey.R
import cl.aarampour.survey.livedata.ILiveDataDataChangeListener
import cl.aarampour.survey.livedata.LiveData
import cl.aarampour.survey.page.IPageViewListener
import cl.aarampour.survey.page.PageView
import cl.aarampour.survey.views.sliderview.ImageModel

import org.json.JSONArray
import org.json.JSONObject

class SurveyPager(context: Context) : LinearLayout(context), IPageViewListener,
    ILiveDataDataChangeListener, IPagerListener {

    private  val TAG = "SurveyPager"

    /**
     * Parameters from app
     */
    lateinit var pagesJson: JSONArray

    /**
     * Views
     */
    lateinit var pageLoading: AlertDialog
    lateinit var container: LinearLayout

    /**
     * CallBack
     */
    lateinit var callBack: ISurveyPagerListener

    /**
     * Handlers
     */
    lateinit var pagerHandler: PagerHandler

    /**
     * Runtime parameters
     */
    private var position = -1

    private lateinit var liveData: LiveData

    /**
     * Survey page will be holding only one page at a time
     * We pass fake pageview to make it non-null
     */
    private var pageView: PageView = PageView(context)

    /**
     * Output callback
     */

    private lateinit var outPutCallBack: ISurveyOutputListener

    /**
     * Finished page flag
     */

    private var isOnFinishedPage = false

    private var isPreview = false

    constructor(
        context: Context,
        pagesJson: JSONArray,
        folderPath: String,
        sliderImages: ArrayList<ImageModel>,
        answers: JSONObject,isPreview : Boolean,
        callBack: ISurveyPagerListener,
        outPutCallBack: ISurveyOutputListener
    ) : this(context) {
        this.callBack = callBack
        this.pagesJson = pagesJson
        this.outPutCallBack = outPutCallBack
        this.isPreview = isPreview


        liveData = LiveData.getNewInstance()
        liveData.isPreview = isPreview
        liveData.attachSurveyUsedCallBack(callBack)
        liveData.setFolderPath(folderPath)
        liveData.addSliderImages(sliderImages)
        liveData.injectAnswers(answers)



        initView()


    }

    /**
     * Live data
     */

    fun evacuate() {
        liveData.evacuate()
    }

    /**
     * This function added in order to handle finished page
     * When user finished and shows finished fragment , There is button to get back tu survey
     * So when hits back in onResume of ResultFragment we add it again and call this function to recreate it
     * And show last page that user was answering
     */

    fun showPage() {

        Log.i(TAG, "showPage: pos - $position")

        showLoading()

        isOnFinishedPage = false
        if (liveData.answeredPagePositions.size == 0) {
            position = -1
            //Here means is first time opening survey and not reached end yet

            pagerHandler = PagerHandler(pagesJson, this)
            pagerHandler.getPageDataByCondition(position)

        } else {//When user get back from finished page , SurveyPager won't be created again
            //Since constructor not created to add page , We need to get last page in queue to add to page
            //Last page exists in liveData.answeredPagePositions we pop it and if user press next again we add it
            //Then again goes to finish page as there is no page again , Note that position in this class will be -1 if finished page launches
            pagerHandler = PagerHandler(pagesJson, this)
            pagerHandler.popPagePisition()

        }


    }

    override fun onPageRecieved(newPosition: Int) {
        Log.i(TAG, "onPageRecieved: pos - $newPosition")
        if (newPosition == -1) {//If page position is -1 means user reached the end
                isOnFinishedPage = true
                callBack.onSurveyFinished()

            dismissLoading()
        } else {

            /**
             * Remove container view to add new page
             */
            container.removeAllViews()

            this.position = newPosition
            pageView = PageView(
                context,
                pagesJson.getJSONObject(position),
                position,isPreview,
                this@SurveyPager
            )
            container.addView(pageView)


        }

    }

    /**
     * Initiate parent UI
     */

    fun initView() {

        //This view as LinearLayout
        val params = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams = params
        orientation = VERTICAL

        //RelativeLayout to keep loading and container of survey
        val relativeLayout = RelativeLayout(context)

        //loading
        pageLoading = initializeLoadingAlert()

        val pageLoadingParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        pageLoadingParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)

        //Container of survey
        container = LinearLayout(context)
        container.layoutParams = params

        //Add views
        addView(relativeLayout)
        relativeLayout.addView(container)



    }

    private fun initializeLoadingAlert(): AlertDialog {
        val builder = AlertDialog.Builder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.alert_loading_layout,this,false)

        builder.setView(view)

        builder.setCancelable(false)

        return builder.create()
    }

    private fun showLoading(){
        pageLoading.show()
        pageLoading.getWindow()?.getAttributes()?.windowAnimations = R.style.DialogAnimation
        pageLoading.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun dismissLoading(){
        pageLoading.dismiss()
    }


    /**
     * Paginating functions
     */

    fun onBackPressed() {

        if (position > 0) {
            /**
             * We also can save current page answers if user changed it
             */
            liveData.savePageByPosition(position,pageView.getAnswers())
            /**
             * Remove container view to add new page
             */

            showLoading()

            pagerHandler.popPagePisition()

        }


    }

    fun onNextPressed() {

        if(isPreview && liveData.lastPagePosition != position || !isPreview) {

            if (pageView.isMandatoryAnswered()) {
                /**
                 * Show loading
                 */
                showLoading()


                /**
                 * Ask pager handler to give us new page position
                 */
                liveData.addPagePisition(position)//We pass this page position before adding new one / Cause we need answered ones
                liveData.savePageByPosition(position, pageView.getAnswers())
                pagerHandler.getPageDataByCondition(position)//Ask to give new position after adding current position data

            }
        }

    }


    /**
     * Save options
     */

    fun saveAsDraft(closeSurvey: Boolean) {
        if (!isOnFinishedPage) {//Check finished page flag
            //If user is not on finished page , Means user saved in the middle of survey or auto save triggered
            //For that we need to get current page position as well to save it and not loose data
            liveData.addPagePisition(position)//Put page position inside array

            liveData.savePageByPosition(
                position,
                pageView.getAnswers()
            )//Save answers of page in map
            //Here we add current position to save data and close , But if it's not closed we need to save this page as well but we don't want to have it in
            //Our queue to confuse , If we have current position in queue back it won't add it again but back button will pop current position and show it twice
            //If it will be closed it doesn't matter to remove current position from queue or not
            //But if it's not going to be closed it matters , So we will remove from queue after getting data
        }
        val answers = liveData.getAnswers()
        val pictures = liveData.getPictures()
        val answersCount = liveData.getAnswersCount()
        //Remove current position from queue in case survey not closed
        if (!isOnFinishedPage) //We pop added positions in case it was in the middle of survey
            liveData.popPagePisition()

        outPutCallBack.onSurveyDraftPressed(answers, pictures, answersCount, closeSurvey)
    }

    fun saveAsFinished() {
        val answers = liveData.getAnswers()
        val pictures = liveData.getPictures()
        val answersCount = liveData.getAnswersCount()
        outPutCallBack.onSurveyFinishPressed(answers, pictures, answersCount)
    }

    override fun onPageLoadStarted() {
//            pageLoading.visibility = VISIBLE

    }

    override fun onPageLoadFinished() {
//        pageLoading.visibility = GONE
        /**
         * Remove loading
         */
        Handler().postDelayed(Runnable {
            dismissLoading()
        },200)

    }

    override fun onConditionChanged(condition: JSONObject) {

    }

    /**
     * Picture handle
     */

    fun addImage(position: Int, picture: JSONObject) {
        liveData.addPicture(position, picture)
    }


    /**
     * Page creation
     */


}