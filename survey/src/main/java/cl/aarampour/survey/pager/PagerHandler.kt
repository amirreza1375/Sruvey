package cl.aarampour.survey.pager

import cl.aarampour.survey.SurveyKey
import cl.aarampour.survey.livedata.ILiveDataDataChangeListener
import cl.aarampour.survey.livedata.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import org.json.JSONArray
import org.json.JSONObject

class PagerHandler(val pagesJson: JSONArray, val callBack: IPagerListener) :
    ILiveDataDataChangeListener {

    val liveData = LiveData.getNewInstance()

    /**
     * Get page data by condition
     */

    fun getPageDataByCondition(position: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            var foundPosition = -1
            for (i in (position + 1)..pagesJson.length() - 1) {

                val pageJson = pagesJson.getJSONObject(i)

                if (hasPageConditions(pageJson)) {
                    foundPosition = i
                    break
                }

            }
            GlobalScope.launch(Dispatchers.Main) {
                callBack.onPageRecieved(foundPosition)
            }
        }

    }

    fun popPagePisition(){
        GlobalScope.launch(Dispatchers.IO) {

            val postion = liveData.popPagePisition()

            GlobalScope.launch(Dispatchers.Main) {
                callBack.onPageRecieved(postion)
            }

        }
    }

    private fun hasPageConditions(pageJson: JSONObject): Boolean {

        if (!pageJson.has(SurveyKey.Page.VISIBLE_IF))//This means page has no condition to be shown
            return true

        //Get VISIBLE_IF value from json and clean it up , Sometimes comes with "'" or space
        val visibleIf = pageJson.getString(SurveyKey.Page.VISIBLE_IF).replace("'", "").trim()

        if (visibleIf.equals(""))//Again no condition
            return true

        //Get array of VISIBLE_IF
        val vb_greater_arr = visibleIf.split(">=")
        val vb_equal_arr = visibleIf.split("=")

        //Now we need to check which is filled with data to check it that way

        if (vb_equal_arr.size >= 2) {
            //This means we get equal condition
            //get VISIBLE_IF equal condition name and clean it up from { or } and get value as well and clean up spaces
            val vb_equal_name = vb_equal_arr.get(0).replace("}", "").replace("{", "").trim()
            val vb_equal_value = vb_equal_arr.get(1).trim()
            return liveData.isExistInConditions(vb_equal_name, vb_equal_value)

        } else if (vb_greater_arr.size >= 2) {
            val vb_greater_name_arr = vb_greater_arr.get(0).trim().split(".")
            val vb_greater_name = vb_greater_name_arr.get(0).replace("{", "").trim()
            val vb_greater_value: Int = vb_greater_name_arr.get(1).trim().toInt()
            return liveData.isExistInConditions(vb_greater_name, vb_greater_value)
        }

        return false
    }

    override fun onConditionChanged(condition: JSONObject) {

    }

}