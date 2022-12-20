package cl.aarampour.survey.livedata

import cl.aarampour.survey.SurveyKey
import cl.aarampour.survey.pager.ISurveyPagerListener
import cl.aarampour.survey.views.sliderview.ImageModel
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ArrayList

/**
 * This class handles all data
 * Such as conditions , page data , pictures and slider images
 */

class LiveData {

    /**
     * Folder address that image wil be saved
     */

    private var folderPath = ""

    /**
     * This will be triggered to change views availability on conditions
     */

    lateinit var callBack: ILiveDataDataChangeListener

    /**
     * Call back to parent activity to not let user to close survey without saving it
     * Will be triggered if condition added , position added and answer added
     */
    var surveyUsedCallBack: ISurveyPagerListener? = null

    /**
     * Shop id and checklist id of server
     */

    var shopId = -1
    var checklistId = -1

    /**
     * Will be used in preview mode to prevent next button action on last page
     */
    var lastPagePosition = -1

    /**
     *
     */
    var isPreview = false

    private constructor() {

    }

    fun attachSurveyUsedCallBack(callBack: ISurveyPagerListener) {
        this.surveyUsedCallBack = callBack
    }

    fun attachCallBack(callBack: ILiveDataDataChangeListener) {
        this.callBack = callBack
    }

    /**
     * Singlton class which will be used in whole survey
     */
    companion object {

        var liveData: LiveData? = null

        fun getNewInstance(): LiveData {

            liveData?.let { safeLiveData ->

                return safeLiveData

            } ?: kotlin.run {
                liveData = LiveData()
                return liveData!!
            }

        }

    }

    /**
     *  ---- Keep conditional answers in this array to compare for conditional functions ----
     */

    //region conditions
    val conditionsByPagePosition = HashMap<Int, ArrayList<JSONObject>>()

    fun addCondition(position: Int, condition: JSONObject) {

            surveyUsedCallBack?.onSurveyUsed()

            var conditions: ArrayList<JSONObject>? = conditionsByPagePosition.get(position)

            conditions?.let { safeCondiotions ->

                if (condition.getString(SurveyKey.Page.View.TYPE)
                        .equals(SurveyKey.TYPE.RADIO_GROUP)
                ) {
                    //RadioGroup must have only one answer
                    for (i in 0 until safeCondiotions.size) {

                        val exisitingCondition = safeCondiotions.get(i)

                        if (exisitingCondition.getString(SurveyKey.Page.View.ID)
                                .equals(condition.getString(SurveyKey.Page.View.ID))
                        ) {
                            safeCondiotions.removeAt(i)//Find with equal id and remove it
                            break
                        }

                    }

                    safeCondiotions.add(condition)//Then add new value
                    callBack.onConditionChanged(condition)

                } else if (condition.getString(SurveyKey.Page.View.TYPE)
                        .equals(SurveyKey.TYPE.CHECK_BOX)
                ) {
                    //Checkbox can have multiple answers
                    if (condition.getBoolean(SurveyKey.Page.View.CheckBox.STATUS)) {//If is true we need to add it
                        safeCondiotions.add(condition)
                    } else {//If is false then we remove it from list
                        for (i in 0 until safeCondiotions.size) {
                            val existingCondition = safeCondiotions.get(i)
                            if (existingCondition.getString(SurveyKey.Page.View.ID)
                                    .equals(condition.getString(SurveyKey.Page.View.ID))
                            ) {
                                if (existingCondition.getString(SurveyKey.Page.View.CheckBox.VALUE)
                                        .equals(condition.getString(SurveyKey.Page.View.CheckBox.VALUE))
                                ) {
                                    safeCondiotions.removeAt(i)
                                    break
                                }
                            }
                        }
                    }
                    callBack.onConditionChanged(condition)
                } else if (condition.getString(SurveyKey.Page.View.TYPE)
                        .equals(SurveyKey.TYPE.MULTI_TEXT)
                ) {
                    //Multitext can have only one answer for each value
                    for (i in 0 until safeCondiotions.size) {

                        val exisitingCondition = safeCondiotions.get(i)

                        if (exisitingCondition.getString(SurveyKey.Page.View.ID)
                                .equals(condition.getString(SurveyKey.Page.View.ID))
                        ) {
                            if (exisitingCondition.getString(SurveyKey.Page.View.MultiText.Item.NAME)
                                    .equals(condition.getString(SurveyKey.Page.View.MultiText.Item.NAME))
                            ) {
                                safeCondiotions.removeAt(i)//Find with equal id and remove it
                                break
                            }
                        }

                    }

                    safeCondiotions.add(condition)//Then add new value
                }

                conditionsByPagePosition.put(
                    position,
                    safeCondiotions
                )//Replace changed array in map

            } ?: kotlin.run {

                conditions = ArrayList()
                conditions!!.add(condition)

                callBack.onConditionChanged(condition)

                conditionsByPagePosition.put(position, conditions!!)//Replace changed array in map

            }

    }


    //endregion

    //region page positions

    /**
     * Answered pages positions will be kept in this array to not send other pages answers
     * Only answer of the pages that user passed to get end of the survey
     */
    val answeredPagePositions = ArrayList<Int>()

    fun addPagePisition(position: Int) {

        if (position >= 0) {

            surveyUsedCallBack?.onSurveyUsed()

            var exist = false

            for (pos in answeredPagePositions) {

                if (pos == position) {
                    exist = true
                    break
                }

            }

            if (!exist) {
                answeredPagePositions.add(position)
            }
        }

    }

    fun popPagePisition(): Int {

        val position = answeredPagePositions.get(answeredPagePositions.size - 1)
        answeredPagePositions.removeAt(answeredPagePositions.size - 1)//Remove page position after saving it in position val

        return position

    }

    //endregion

    //region answers

    /**
     * All pages will be saved with position and answers here , Even ones that won't be send
     * In case to have it as draft data
     */
    val answers = HashMap<Int, JSONArray>()


    fun savePageByPosition(position: Int, answer: JSONArray) {

        surveyUsedCallBack?.onSurveyUsed()

        if (!isPreview)
            answers.put(position, answer)

    }

    //endregion

    //region pictures

    /**
     * Pictures of survey to save or overwrite to get them at the end
     */

    val picturesByPagePosition = HashMap<Int, ArrayList<JSONObject>>()

    fun addPicture(position: Int, picture: JSONObject) {

        if (!isPreview) {

            surveyUsedCallBack?.onSurveyUsed()

            var picturesOfPage = getImagesOfPage(position)

            for (i in 0 until picturesOfPage.size) {

                val pictureOfPage = picturesOfPage.get(i)

                if (pictureOfPage.getString(SurveyKey.Page.View.ImagesTaker.ID)
                        .equals(picture.getString(SurveyKey.Page.View.ImagesTaker.ID))
                ) {

                    if (pictureOfPage.getString(SurveyKey.Page.View.ImagesTaker.ImageTakerItem.INDEX)
                            .equals(picture.getString(SurveyKey.Page.View.ImagesTaker.ImageTakerItem.INDEX))
                    ) {

                        picturesOfPage.removeAt(i)
                        break

                    }

                }

            }

            picturesOfPage.add(picture)

            picturesByPagePosition.put(position, picturesOfPage)

            callBack.onImageChanged()
        }
    }

    fun getImagesOfPage(position: Int): ArrayList<JSONObject> {
        val picturesOfPage = picturesByPagePosition.get(position)
        picturesOfPage?.let {
            return it
        } ?: kotlin.run {
            return ArrayList()
        }
    }

    fun getImagesOfView(position: Int, id: String): ArrayList<JSONObject> {
        val pictures = getImagesOfPage(position)

        val picturesArr = ArrayList<JSONObject>()

        for (picture in pictures) {
            if (picture.getString(SurveyKey.Page.View.ImagesTaker.ID)
                    .equals(id)
            ) {
                picturesArr.add(picture)
            }
        }

        return picturesArr

    }

    fun getImageOfViewIndex(position: Int, id: String, index: Int): JSONObject? {
        val pictures = getImagesOfView(position, id)


        for (picture in pictures) {
            if (picture.getString(SurveyKey.Page.View.ImagesTaker.ImageTakerItem.INDEX).toInt()
                == index
            ) {
                return picture
            }
        }

        return null
    }

    //endregion

    //region slider images

    var sliderImages = ArrayList<ImageModel>()

    fun addSliderImages(sliderImage: ArrayList<ImageModel>) {
        if (this.sliderImages.size == 0)//One time will be added
            this.sliderImages.addAll(sliderImage)
    }


    //endregion

    /**
     * Functions of Conditions
     */
    //For page with equal condition -> RadioGroup and CheckBox
    fun isExistInConditions(visibleEqualName: String, visibleEqualValue: String): Boolean {

        for (i in 0 until answeredPagePositions.size) {

            val position = answeredPagePositions.get(i)

            val pageConditions = conditionsByPagePosition.get(position)

            pageConditions?.let { safePageConditions ->

                for (j in 0 until safePageConditions.size) {

                    val pageCondition = safePageConditions.get(j)

                    val pageConditionName = pageCondition.getString(SurveyKey.Page.View.NAME)

                    if (pageConditionName.equals(visibleEqualName)) {

                        val pageConditionValue = getValueDependOnType(pageCondition)

                        if (pageConditionValue.equals(visibleEqualValue)) {

                            return true

                        }

                    }

                }

            }

        }
        return false
    }

    //For page with greater condition - > MultiText
    fun isExistInConditions(visibleEqualName: String, visibleGreaterValue: Int): Boolean {

        for (i in 0 until answeredPagePositions.size) {

            val position = answeredPagePositions.get(i)

            val pageConditions = conditionsByPagePosition.get(position)

            pageConditions?.let { safePageConditions ->

                for (j in 0 until safePageConditions.size) {

                    val pageCondition = safePageConditions.get(j)

                    val pageConditionName = pageCondition.getString(SurveyKey.Page.View.NAME)

                    if (pageConditionName.equals(visibleEqualName)) {

                        val pageConditionValue: Int =
                            getValueDependOnType(pageCondition).toString().toInt()

                        if (pageConditionValue >= visibleGreaterValue) {

                            return true

                        }

                    }

                }

            }

        }
        return false
    }

    /**
     * Get value of answer depend of each view
     * Radio group has it directly in json object cause includes only one answer
     * Check box has it inside json array cause could have multiple answers
     */
    private fun getValueDependOnType(pageCondition: JSONObject): String {

        if (pageCondition.getString(SurveyKey.Page.View.TYPE).equals(SurveyKey.TYPE.RADIO_GROUP)) {
            return pageCondition.getString(SurveyKey.Page.View.RadioGroup.VALUE)
        } else if (pageCondition.getString(SurveyKey.Page.View.TYPE)
                .equals(SurveyKey.TYPE.CHECK_BOX)
        ) {
            return pageCondition.getString(SurveyKey.Page.View.CheckBox.VALUE)
        } else if (pageCondition.getString(SurveyKey.Page.View.TYPE)
                .equals(SurveyKey.TYPE.MULTI_TEXT)
        ) {
            return pageCondition.getString(SurveyKey.Page.View.MultiText.VALUE)
        }

        return ""

    }

    /**
     *Get each view answer by it's page position and id of view
     */

    fun getViewAnswer(pagePosition: Int, viewId: String): JSONObject {

        val answersOfPage = answers.get(pagePosition)

        answersOfPage?.let { safeAnswersOfPage ->

            for (i in 0 until safeAnswersOfPage.length()) {

                val answer = safeAnswersOfPage.getJSONObject(i)

                if (answer.has(SurveyKey.Page.View.ID)) {

                    if (answer.getString(SurveyKey.Page.View.ID).equals(viewId)) {
                        return answer
                    }

                }

            }

        }

        return JSONObject()
    }

    fun getFolderPath(): String {
        return folderPath
    }

    fun setFolderPath(path: String) {//Will be only one time and won't be changed during survey in use
        if (!path.equals(""))
            this.folderPath = path
    }

    //region         OUT-PUT

    /**
     * This part is reserved to create output for app
     */

    fun getAnswers(): JSONObject {
        val answeredPagesByAnswersObj = JSONObject()
        for (answeredPagePosition in answeredPagePositions) {

            answers.get(answeredPagePosition)?.let {

                answeredPagesByAnswersObj.put(answeredPagePosition.toString(), it)

            }

            picturesByPagePosition.get(answeredPagePosition)?.let {

                if (it.size > 0)
                    answeredPagesByAnswersObj.put("p$answeredPagePosition", JSONArray(it))

            }

        }

        val pagePositions = JSONArray(answeredPagePositions)
        answeredPagesByAnswersObj.put(SurveyKey.PAGE_POSITION, pagePositions)
        return answeredPagesByAnswersObj
    }

    fun getPictures(): ArrayList<JSONObject> {

        val pictures = ArrayList<JSONObject>()

        for (pagePotition in answeredPagePositions) {

            picturesByPagePosition.get(pagePotition)?.let {
                pictures.addAll(it)
            }

        }

        return pictures
    }

    fun getAnswersCount(): Int {

        var count = 0

        for (pagePosition in answeredPagePositions) {

            val answersOfPage = answers.get(pagePosition)

            answersOfPage?.let { safeAnswers ->

                for (i in 0 until safeAnswers.length()) {

                    val answer = safeAnswers.getJSONObject(i)

                    if (answer.getString(SurveyKey.Page.View.TYPE)
                            .equals(SurveyKey.TYPE.RADIO_GROUP)
                        || answer.getString(SurveyKey.Page.View.TYPE).equals(SurveyKey.TYPE.COMMENT)
                    ) {

                        count++

                    } else if (answer.getString(SurveyKey.Page.View.TYPE)
                            .equals(SurveyKey.TYPE.CHECK_BOX)
                    ) {

                        val answersOfView = answer.getJSONArray(SurveyKey.Page.View.CheckBox.VALUE)

                        count += answersOfView.length()

                    } else if (answer.getString(SurveyKey.Page.View.TYPE)
                            .equals(SurveyKey.TYPE.MULTI_TEXT)
                    ) {

                        val answersOfView = answer.getJSONArray(SurveyKey.Page.View.MultiText.VALUE)

                        count += answersOfView.length()

                    }

                }

            }

        }

        return count

    }

    //endregion

    //region           IN_PUT

    /**
     * This part is reserved to get input from app , For example draft or preview
     */

    fun injectAnswers(answersObj: JSONObject) {
        //First get page positions of the injected answers
        if (answersObj.has(SurveyKey.PAGE_POSITION)) {
            val pagePositions = answersObj.getJSONArray(SurveyKey.PAGE_POSITION)

            addPagePisitionsFromJsonArray(pagePositions)

            //Start a loop on page positions to get each position data and put it in map

            for (answeredPagePisition in answeredPagePositions) {

                if (answersObj.has(answeredPagePisition.toString()))
                    answers.put(
                        answeredPagePisition,
                        answersObj.getJSONArray(answeredPagePisition.toString())
                    )
                //Also need to add conditional items
                injectConditions(
                    answeredPagePisition,
                    answersObj.getJSONArray(answeredPagePisition.toString())
                )

                if (answersObj.has("p$answeredPagePisition"))
                    picturesByPagePosition.put(
                        answeredPagePisition,
                        getInjectedImagesAsArray(answersObj.getJSONArray("p$answeredPagePisition"))
                    )


            }
        }

    }

    private fun injectConditions(position: Int, jsonArray: JSONArray) {
        for (i in 0 until jsonArray.length()) {

            val answer = jsonArray.getJSONObject(i)

            val type = answer.getString(SurveyKey.Page.View.TYPE)

            if (type.equals(SurveyKey.TYPE.RADIO_GROUP)) {
                injectCondition(position, answer)
            }

        }
    }

    private fun injectCondition(position: Int, condition: JSONObject) {
        var conditions: ArrayList<JSONObject>? = conditionsByPagePosition.get(position)

        conditions?.let { safeCondiotions ->

            if (condition.getString(SurveyKey.Page.View.TYPE).equals(SurveyKey.TYPE.RADIO_GROUP)) {
                //RadioGroup must have only one answer
                for (i in 0 until safeCondiotions.size) {

                    val exisitingCondition = safeCondiotions.get(i)

                    if (exisitingCondition.getString(SurveyKey.Page.View.ID)
                            .equals(condition.getString(SurveyKey.Page.View.ID))
                    ) {
                        safeCondiotions.removeAt(i)//Find with equal id and remove it
                        break
                    }

                }

                safeCondiotions.add(condition)//Then add new value

            }

            conditionsByPagePosition.put(position, safeCondiotions)//Replace changed array in map

        } ?: kotlin.run {

            conditions = ArrayList()
            conditions!!.add(condition)

            conditionsByPagePosition.put(position, conditions!!)//Replace changed array in map

        }
    }

    private fun getInjectedImagesAsArray(jsonArray: JSONArray): ArrayList<JSONObject> {

        val pictures = ArrayList<JSONObject>()

        for (i in 0 until jsonArray.length()) {

            pictures.add(jsonArray.getJSONObject(i))

        }
        return pictures
    }

    private fun addPagePisitionsFromJsonArray(pagePisitionsArr: JSONArray) {
        if (pagePisitionsArr.length() > 0 && isPreview)
            this.lastPagePosition = pagePisitionsArr.getInt(pagePisitionsArr.length() - 1)

        for (i in 0 until pagePisitionsArr.length()) {

            answeredPagePositions.add(pagePisitionsArr.getInt(i))

        }

    }

    //endregion

    fun evacuate() {

        conditionsByPagePosition.clear()
        answeredPagePositions.clear()
        answers.clear()
        picturesByPagePosition.clear()
        sliderImages.clear()



        folderPath = ""
        shopId = -1
        checklistId = -1

        surveyUsedCallBack = null

        liveData = null

    }


}