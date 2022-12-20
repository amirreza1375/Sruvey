package cl.aarampour.survey.views.base

import org.json.JSONObject

interface IConditionalValueListener {
    fun onRadioGroupConditionalValueChanged(data : JSONObject)
}