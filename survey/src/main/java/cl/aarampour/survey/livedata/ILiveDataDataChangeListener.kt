package cl.aarampour.survey.livedata

import org.json.JSONObject

interface ILiveDataDataChangeListener {
    fun onConditionChanged(condition : JSONObject)
    fun onImageChanged(){

    }
}