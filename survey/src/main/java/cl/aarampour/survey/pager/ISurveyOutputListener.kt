package cl.aarampour.survey.pager

import org.json.JSONObject

interface ISurveyOutputListener {

    fun onSurveyFinishPressed(answers : JSONObject,pictures : ArrayList<JSONObject>,answersCount : Int)
    fun onSurveyDraftPressed(answers: JSONObject, pictures: java.util.ArrayList<JSONObject>,answersCount : Int,closeSurvey : Boolean)

}