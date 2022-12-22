package com.amirrezaarampour.sruvey

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.*
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.CompoundButton.OnCheckedChangeListener
import androidx.appcompat.app.AlertDialog
import cl.aarampour.survey.SurveyConfig
import cl.aarampour.survey.SurveyKey
import cl.aarampour.survey.pager.ISurveyOutputListener
import cl.aarampour.survey.pager.ISurveyPagerListener
import cl.aarampour.survey.pager.SurveyPager
import cl.aarampour.survey.views.sliderview.ImageModel
import cl.aarampour.survey.views.sliderview.ResultId
import com.amirrezaarampour.sruvey.databinding.ActivityMainBinding
import com.bequarks.operatortrackkotlin.permission.PermissionModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity(), OnItemSelectedListener, IServerParamsCallBack,
    ISurveyOutputListener, ISurveyPagerListener {

    companion object {
        public val SHARED_PREF_KEY = "sharedPrefKey"
        public val CHOSEN_SERVER_KEY = "chosenServerKey"
        public val TOKEN_KEY = "tokenKey"
        public val SHOPS_KEY = "shopsKey"
        public val CHECKLISTS_KEY = "checklistsKey"
        public val OPTICOS_KEY = "opticosKey"
    }

    private lateinit var binding: ActivityMainBinding

    private val servers = ArrayList<String>()

    private var userEmail = ""
    private var userPassword = ""

    private var choosenServer = ""
    private var token = ""

    private var chosenShop = JSONObject()
    private var chosenChecklist = JSONObject()

    private var shops = ArrayList<JSONObject>()
    private var checklists = ArrayList<JSONObject>()
    private var opticos = ArrayList<JSONObject>()

    private lateinit var checklistAdapter: ArrayAdapter<JSONObject>
    private lateinit var surveyPager: SurveyPager

    private var isPreview = false

    private fun removeAllParams() {
        val editor = getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE).edit()
        editor.putString(SHOPS_KEY, "[]")
        editor.putString(CHECKLISTS_KEY, "[]")
        editor.putString(OPTICOS_KEY, "[]")

        editor.apply()
        clear()
    }

    private val shopItemChangeListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            chosenShop = shops.get(position)
            clear()
//            val shops = chosenShop.getString("shops")
//            for (i in 0 until checklists.size){
//
//                val checklistObj = checklists.get(i)
//
//                if (!checklistObj.getString("id").equals(shops)){
//                    checklists.removeAt(i)
//                }
//
//            }


        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

    }

    private val checklistItemChangeListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            chosenChecklist = checklists.get(position)
            clear()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fab.setOnClickListener { view ->
            showAlert()
        }

        binding.isPreviewCheck.setOnCheckedChangeListener(object : OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                isPreview = isChecked
                clear()
            }

        })

        //region Add servers
        servers.add("https://redbull.humanresources.cl")
        servers.add("https://parquearauco-aeco.bequarks.cl")
        servers.add("https://promart-analytic.bequarks.pe")

        //endregion

        //region Get cached data
        choosenServer =
            getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE).getString(CHOSEN_SERVER_KEY, "")!!
        token = getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE).getString(TOKEN_KEY, "")!!

        //endregion

        updateSpinners()

        if (token.equals("")) {
            showAlert()
        }


    }

    private fun updateSpinners() {

        binding.loading.visibility = VISIBLE
        //region Get params cached data , And convert to array

        val shopsJsonArray =
            JSONArray(
                getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE).getString(
                    SHOPS_KEY,
                    "[]"
                )!!
            )
        val checklistsJsonArray = JSONArray(
            getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE).getString(
                CHECKLISTS_KEY, "[]"
            )!!
        )
        val opticosJsonArray = JSONArray(
            getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE).getString(
                OPTICOS_KEY, "[]"
            )!!
        )

        shops.clear()
        checklists.clear()
        opticos.clear()

        if (choosenServer.equals("https://redbull.humanresources.cl")) {
            for (i in 0 until shopsJsonArray.length()) {
                val shopArr = shopsJsonArray.getString(i).split(":")
                val shopObj = JSONObject()
                shopObj.put("id", shopArr[0])
                shopObj.put("title", shopArr[1])
                shopObj.put("shops", shopArr[2])
                shops.add(shopObj)
            }
        } else {
            for (i in 0 until shopsJsonArray.length()) {
                shops.add(shopsJsonArray.getJSONObject(i))
            }
        }

        for (i in 0 until checklistsJsonArray.length()) {
            checklists.add(checklistsJsonArray.getJSONObject(i))
        }

        for (i in 0 until opticosJsonArray.length()) {
            opticos.add(opticosJsonArray.getJSONObject(i))
        }

        //endregion

        //region setup shop and checklist spinners

        val shopAdapter = CustomArrayAdapter(this, android.R.layout.simple_list_item_1, shops)
        checklistAdapter = CustomArrayAdapter(this, android.R.layout.simple_list_item_1, checklists)
        shopAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        checklistAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)

        with(binding.shopSpinner) {
            this.adapter = shopAdapter
            onItemSelectedListener = this@MainActivity.shopItemChangeListener
            prompt = "Select shop"
            gravity = Gravity.CENTER
        }

        with(binding.checklistSpinner) {
            this.adapter = checklistAdapter
            onItemSelectedListener = this@MainActivity.checklistItemChangeListener
            prompt = "Select checklist"
            gravity = Gravity.CENTER
        }

        binding.loading.visibility = GONE

        //endregion

    }

    fun onBackClicked(view: View) {
        surveyPager.onBackPressed()
    }

    fun onNextClicked(view: View) {
        surveyPager.onNextPressed()
    }

    fun convert_String_to_ArrayListInteger(shops_str: String): ArrayList<Int> {
        val shops: ArrayList<Int> = ArrayList()
        try {
            val shopsArray = JSONArray(shops_str)
            for (i in 0 until shopsArray.length()) {
                shops.add(shopsArray.getInt(i))
            }
            return shops
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        try {
            if (shops_str == null) {
                return ArrayList()
            }
            if (shops_str.length == 0) {
                return ArrayList()
            }
            val shop_str_array = shops_str.split(",").toTypedArray()
            for (shop_id in shop_str_array) {
                shops.add(shop_id.toInt())
            }
            return shops
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return java.util.ArrayList()
    }


    fun onOpenChecklistClicked(view: View) {

        val imageSliderModels = ArrayList<ImageModel>()
        for (i in 0 until opticos.size) {
            try {
                val optico = opticos.get(i)
                val model = ImageModel()
//                model.imageFile = optico.imagePath
                model.name = optico.getString("optico_name")
                model.prioritie = optico.getString("priority")
                model.shops = convert_String_to_ArrayListInteger(optico.getString("shop_id"))
                val results = JSONArray(optico.getString("result_id"))
                val resultIds: ArrayList<ResultId> = ArrayList<ResultId>()
                for (j in 0 until results.length()) {
                    val result: JSONObject = results.getJSONObject(j)
                    val resultId = ResultId()
                    resultId.ID = result.getInt(SurveyKey.Page.View.Slider.ID)
                    resultId.elemento = result.getInt(SurveyKey.Page.View.Slider.ELEMENTO)
                    resultId.posicion = result.getInt(SurveyKey.Page.View.Slider.POSICTION)
                    resultId.subCanal = result.getInt(SurveyKey.Page.View.Slider.SUBCANAL)
                    resultIds.add(resultId)
                }
                model.resultIDS = resultIds
                imageSliderModels.add(model)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } //end of list for

        SurveyConfig.IS_PDF_ALLOWED = false
        SurveyConfig.IS_GALLERY_ALLOWED = true

        clear()
        surveyPager = SurveyPager(
            this,
            JSONObject(chosenChecklist.getString("json")).getJSONArray("pages"),
            getExternalFilesDir(null)?.absolutePath!!,
            imageSliderModels,
            JSONObject(),
            isPreview,
            this,
            this
        )
        binding.surveyContainer.addView(surveyPager)
        surveyPager?.showPage()
        binding.buttonsContainer.visibility = VISIBLE
    }

    private fun clear() {
        binding.surveyContainer.removeAllViews()
    }

    fun onUpdateClicked(view: View) {
        binding.loading.visibility = VISIBLE
        ApiService().getParamsData(
            this,
            token,
            Settings.Secure.getString(getContentResolver(), "android_id"),
            this
        )
    }

    fun showAlert() {

        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this)
            .inflate(R.layout.alert_requirement_layout, binding.root, false)

        builder.setView(view)
        builder.setCancelable(false)

        val spinnerView: Spinner = view.findViewById(R.id.serverSpinner)
        val emailEdt: EditText = view.findViewById(R.id.emailEdt)
        val passwordEdt: EditText = view.findViewById(R.id.passEdt)
        val loginBtn: Button = view.findViewById(R.id.login)

        val adapter = ArrayAdapter<String>(this, R.layout.spinner_item, servers)
        adapter.setDropDownViewResource(R.layout.spinner_item)

        with(spinnerView) {
            this.adapter = adapter
//            setSelection(0,false)
            onItemSelectedListener = this@MainActivity
            prompt = "Select server"
            gravity = Gravity.CENTER
        }

        val alert = builder.create()
        alert.show()

        loginBtn.setOnClickListener(object : OnClickListener {
            override fun onClick(v: View?) {
                userEmail = emailEdt.text.toString()
                userPassword = passwordEdt.text.toString()
                login(alert)
            }

        })

    }

    private fun login(alert: AlertDialog) {
        val exitingToken = getSharedPreferences(
            SHARED_PREF_KEY,
            MODE_PRIVATE
        ).getString("$TOKEN_KEY-$choosenServer", "")!!
        if (exitingToken.equals("")) {
            removeAllParams()
            ApiService().loginRequest(this,
                userEmail,
                userPassword,
                Settings.Secure.getString(getContentResolver(), "android_id"),
                object : IBaseApiServiceCallBack<String> {
                    override fun onResponse(response: String) {
                        this@MainActivity.token = response.trim()
                        val editor = getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE).edit()
                        editor.putString(TOKEN_KEY, this@MainActivity.token).apply()
                        editor.putString("$TOKEN_KEY-$choosenServer", this@MainActivity.token)
                            .apply()
                        alert.dismiss()
                    }

                    override fun onFailed(err: String) {
                        super.onFailed(err)
                        Toast.makeText(this@MainActivity, err, Toast.LENGTH_SHORT).show()

                    }

                })
        } else {
            this@MainActivity.token = exitingToken
            val editor = getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE).edit()
            editor.putString(TOKEN_KEY, this@MainActivity.token).apply()
            editor.putString("$TOKEN_KEY-$choosenServer", this@MainActivity.token).apply()
            alert.dismiss()
        }
    }

    override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ) {
        Toast.makeText(this@MainActivity, "Selected $position", Toast.LENGTH_SHORT).show()
        this@MainActivity.choosenServer = servers.get(position)
        val editor = getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE).edit()
        editor.putString(CHOSEN_SERVER_KEY, choosenServer).apply()
        clear()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this@MainActivity, "Nothing selected", Toast.LENGTH_SHORT).show()
    }

    override fun onParamsReceived(params: JSONObject) {
        val editor = getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE).edit()
        editor.putString(SHOPS_KEY, params.getJSONArray("shops").toString())
        editor.putString(CHECKLISTS_KEY, params.getJSONArray("checklists").toString())
        editor.putString(
            OPTICOS_KEY,
            params.getJSONObject("checklist_db").getJSONArray("opticodb").toString()
        )
        editor.apply()

        updateSpinners()

    }

    override fun onFailed(err: String) {
        Toast.makeText(this@MainActivity, "Params error : $err", Toast.LENGTH_SHORT).show()
        binding.loading.visibility = GONE
    }

    override fun onSurveyFinishPressed(
        answers: JSONObject,
        pictures: ArrayList<JSONObject>,
        answersCount: Int
    ) {

    }

    override fun onSurveyDraftPressed(
        answers: JSONObject,
        pictures: java.util.ArrayList<JSONObject>,
        answersCount: Int,
        closeSurvey: Boolean
    ) {
    }

    override fun onSurveyFinished() {
    }

    override fun onSurveyUsed() {
    }


    override fun onResume() {
        super.onResume()

        val permission = PermissionModel(this@MainActivity)
        permission.getCameraPermission()
        permission.getLocationPermission()
        permission.getStoragePermission()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                data?.let { safeData ->
                    safeData.extras?.let { safeExtras ->
                        val position = safeExtras.getInt(SurveyKey.IMAGE_PROPS.PAGE_POSITION)

                        val typeId = safeExtras.getInt(SurveyKey.IMAGE_PROPS.IMAGE_TYPE_ID)
                        val typeName = safeExtras.getString(SurveyKey.IMAGE_PROPS.IMAGE_TYPE_NAME)
                        val viewId = safeExtras.getString(SurveyKey.IMAGE_PROPS.VIEW_ID)
                        val viewName = safeExtras.getString(SurveyKey.IMAGE_PROPS.VIEW_NAME)
                        val index = safeExtras.getInt(SurveyKey.IMAGE_PROPS.INDEX)
                        val path = safeExtras.getString(SurveyKey.IMAGE_PROPS.PATH)
                        val pagePosition = safeExtras.getInt(SurveyKey.IMAGE_PROPS.PAGE_POSITION)

                        val picture = JSONObject()
                        picture.put(SurveyKey.IMAGE_PROPS.VIEW_NAME, viewName)
                        picture.put(SurveyKey.IMAGE_PROPS.VIEW_ID, viewId)
                        picture.put(SurveyKey.IMAGE_PROPS.IMAGE_TYPE_NAME, typeName)
                        picture.put(SurveyKey.IMAGE_PROPS.IMAGE_TYPE_ID, typeId)
                        picture.put(SurveyKey.IMAGE_PROPS.INDEX, index)
                        picture.put(SurveyKey.IMAGE_PROPS.PATH, path)
                        picture.put(SurveyKey.IMAGE_PROPS.PAGE_POSITION, pagePosition)



                        surveyPager.addImage(position, picture)
                    }
                }

            }
        }

    }
}