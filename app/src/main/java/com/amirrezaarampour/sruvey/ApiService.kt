package com.amirrezaarampour.sruvey

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amirrezaarampour.sruvey.MainActivity.Companion.CHOSEN_SERVER_KEY
import com.amirrezaarampour.sruvey.MainActivity.Companion.SHARED_PREF_KEY
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

/**
 * This class will hold and handle all API requests and only will get response to present to models
 */

class ApiService {

    private val TAG = "ApiService"

    private val AUTH_FAIL_CODE = 100

    /**
     * Secondary URLs
     */

    val login = "/api/authenticate/"
    val getServerParameters = "/api/getParams"
    val getParamsVersion = "/api/getChangedElements"

    /**
     * This URL connects to app manager console created by Be Quarks
     */


    fun loginRequest(
        context: Context, email: String, password: String, devId: String,
        callBack: IBaseApiServiceCallBack<String>
    ) {

        val baseURL = context.getSharedPreferences(SHARED_PREF_KEY, AppCompatActivity.MODE_PRIVATE)
            .getString(CHOSEN_SERVER_KEY, "")!!

        if (!baseURL.equals("")) {
            val response = object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    val res = JSONObject(response)//TODO

                    if (res.getBoolean("error")) {
                        callBack.onFailed(res.getJSONObject("result").getString("error_msg"))
                    } else {
                        val result = res.getJSONObject("result")
                        callBack.onResponse(result.getString("t"))
                    }

                }

            }
            val errorResponse = object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    callBack.onServerFailed(error?.message.toString())
                }

            }

            val request = object : StringRequest(
                Request.Method.POST,
                baseURL + login,
                response,
                errorResponse
            ) {
                override fun getParams(): Map<String, String>? {
                    val params = HashMap<String, String>()
                    params["email"] = email
                    params["password"] = password
                    params["devid"] = devId
                    params["devmodel"] = ""
                    params["verapp"] = ""
                    return params
                }
            }
            request.setRetryPolicy(
                DefaultRetryPolicy(
                    8000,
                    1,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            )
            Volley.newRequestQueue(context).add(request)
        } else {
            callBack.onFailed("Choose server")
        }

    }


    fun getParamsData(
        context: Context,
        token:String,
        devId : String,
        callBack: IServerParamsCallBack
    ) {

        val baseURL = context.getSharedPreferences(SHARED_PREF_KEY, AppCompatActivity.MODE_PRIVATE)
            .getString(CHOSEN_SERVER_KEY, "")!!

        if (!baseURL.equals("")) {
            Log.i(TAG, "getParamsData: ")
            val response = object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    response?.let { safeResponse ->
                        val params = JSONObject(safeResponse)
                        if (params.has("error")) {
                            if (params.getBoolean("error")) {

                                if (params.getJSONObject("result").getString("error_number")
                                        .equals("100")
                                ) {
//                                    callBack.onAuthFailed(context)
                                } else {
                                    callBack.onFailed(
                                        params.getJSONObject("result").getString("error_msg")
                                    )
                                }
                            }

                        } else {
                            callBack.onParamsReceived(params)
                        }
                    } ?: kotlin.run {
                        Log.i(TAG, "onResponse: Error parsing")
                        callBack.onFailed("Can not parse json")
                    }

                }

            }
            val errorResponse = object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
//                Log.i(TAG, "onErrorResponse: " + error?.message)
                    callBack.onFailed(error.toString())
                }

            }

            val request = object : StringRequest(
                Request.Method.POST,
                baseURL + getServerParameters,
                response,
                errorResponse
            ) {
                override fun getParams(): Map<String, String>? {
                    val params = HashMap<String, String>()
                    params["devid"] = devId
                    params["authkey"] = token.toString()
                    params["sections"] = "shops,checklists,opticodb"
                    params["version"] = ""
                    return params
                }
            }
            request.setRetryPolicy(
                DefaultRetryPolicy(
                    1000 * 60,
                    1,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            )
            Volley.newRequestQueue(context).add(request)

        }

    }


}