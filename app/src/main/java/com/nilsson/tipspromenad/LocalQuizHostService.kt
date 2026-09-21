package com.nilsson.tipspromenad

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.NotificationCompat
import androidx.webkit.WebViewAssetLoader
import org.json.JSONObject

/** Keeps the local transport and its isolated scoring engine alive during hosting. */
class LocalQuizHostService : Service() {
    private var engineView: WebView? = null
    override fun onBind(intent: Intent?): IBinder? = null
    @android.annotation.SuppressLint("SetJavaScriptEnabled")
    override fun onCreate() {
        super.onCreate()
        val manager=getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("quiz-host","Local quiz hosting",NotificationManager.IMPORTANCE_LOW))
        val open=PendingIntent.getActivity(this,0,Intent(this,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE)
        val stop=PendingIntent.getService(this,1,Intent(this,LocalQuizHostService::class.java).setAction("STOP"),PendingIntent.FLAG_IMMUTABLE)
        startForeground(8086,NotificationCompat.Builder(this,"quiz-host").setSmallIcon(android.R.drawable.stat_notify_sync).setContentTitle("Tipspromenad · Wi-Fi host").setContentText("Hosting on this phone. Tap Stop to end sharing.").setContentIntent(open).addAction(0,"Stop",stop).setOngoing(true).build())
        val loader=WebViewAssetLoader.Builder().addPathHandler("/assets/",WebViewAssetLoader.AssetsPathHandler(this)).build()
        engineView=WebView(this).apply {
            settings.javaScriptEnabled=true;settings.domStorageEnabled=true;settings.allowFileAccess=false;settings.allowContentAccess=false
            addJavascriptInterface(object {
                @JavascriptInterface fun reply(id:String,body:String){LocalQuizServer.reply(id,body)}
                @JavascriptInterface fun ready(){
                    android.os.Handler(mainLooper).post {
                        val view=engineView?:return@post
                        LocalQuizServer.engine={id,request->view.evaluateJavascript("hostRequest(${JSONObject.quote(id)},$request)",null)}
                        try { LocalQuizServer.start(applicationContext) } catch (_: Exception) { stopSelf() }
                    }
                }
            },"TipspromenadServer")
            webViewClient=object:WebViewClient(){
                override fun shouldInterceptRequest(view:WebView,request:WebResourceRequest):WebResourceResponse? = loader.shouldInterceptRequest(request.url)
                override fun shouldOverrideUrlLoading(view:WebView,request:WebResourceRequest)=true
            }
            loadUrl("https://appassets.androidplatform.net/assets/quiz/host-engine.html")
        }
    }
    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {if(intent?.action=="STOP")stopSelf();return START_NOT_STICKY}
    override fun onDestroy(){LocalQuizServer.stop();engineView?.destroy();engineView=null;super.onDestroy()}
}
