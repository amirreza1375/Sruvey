package com.amirrezaarampour.sruvey

import org.json.JSONObject

interface IServerParamsCallBack {
    fun onParamsReceived(params: JSONObject);
    fun onFailed(err : String)
}