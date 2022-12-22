package com.amirrezaarampour.sruvey

import android.util.Log

interface IBaseApiServiceCallBack<T> {

    fun onResponse(response : T)
    fun onFailed(err : String){
        Log.i("ApiService", "onFailed: "+err)
    }
    fun onServerFailed(err : String){
        Log.i("ApiService", "onServerFailed: "+err)
    }

}