package com.nilsson.tipspromenad

import android.annotation.SuppressLint
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.PermissionRequest
import android.webkit.GeolocationPermissions
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import androidx.activity.OnBackPressedCallback
import android.webkit.JavascriptInterface
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.fragment.app.Fragment
import com.nilsson.tipspromenad.databinding.FragmentFirstBinding

class FirstFragment : Fragment(R.layout.fragment_first) {
    private var binding: FragmentFirstBinding? = null
    private var quizWebView: WebView? = null
    private var browserState: Bundle? = null
    private var launchedLanguages: String? = null
    private var pendingCameraRequest: PermissionRequest? = null
    private var organizerPending = false
    private var locationCallback: Pair<String, GeolocationPermissions.Callback>? = null
    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        locationCallback?.let { (origin, callback) ->
            callback.invoke(origin, grants[Manifest.permission.ACCESS_FINE_LOCATION] == true && isResumed, false)
        }
        locationCallback = null
    }

    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        ContextCompat.startForegroundService(requireContext(), android.content.Intent(requireContext(), LocalQuizHostService::class.java))
    }
    fun showWalkSettings(): Boolean {
        if (binding == null || !isResumed) return false
        val webView = quizWebView ?: return false
        if (webView.url?.contains("mode=classic") == true) return false
        webView.evaluateJavascript("window.dispatchEvent(new Event('tipspromenad:settings'));", null)
        return true
    }
    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pendingCameraRequest?.let { request ->
            if (granted && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) request.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
            else request.deny()
        }
        pendingCameraRequest = null
    }

    fun showOrganizer() {
        organizerPending = true
        if (binding != null) dispatchOrganizer()
    }

    private fun dispatchOrganizer() {
        val url = quizWebView?.url?.let(Uri::parse) ?: return
        if (url.scheme != "https" || url.host != ALLOWED_HOST || !url.path.orEmpty().startsWith("/assets/quiz/")) return
        quizWebView?.evaluateJavascript("window.tipspromenadOrganizerRequested=true;window.dispatchEvent(new Event('tipspromenad:organizer'));", null)
        organizerPending = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        browserState = savedInstanceState?.getBundle(STATE_WEBVIEW)
        launchedLanguages = savedInstanceState?.getString(STATE_LANGUAGES)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFirstBinding.bind(view)
        val webView = quizWebView ?: createWebView().also { quizWebView = it }
        binding?.root?.addView(webView)

        // A live WebView survives an ordinary Info/Back visit within this fragment.
        // Across recreation, restoreState restores history, not form/JavaScript state.
        if (webView.url == null) {
            val startUrl = startUrl()
            val restored = launchedLanguages == startUrl &&
                browserState?.let { webView.restoreState(it) } != null
            if (!restored) {
                launchedLanguages = startUrl
                webView.loadUrl(startUrl)
            }
        }
        browserState = null
        refreshLanguageIfNeeded()
        if (organizerPending) dispatchOrganizer()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                webView.evaluateJavascript("typeof window.tipspromenadBack==='function' && window.tipspromenadBack()") { handled ->
                    if (handled != "true") {
                        if (webView.canGoBack()) webView.goBack()
                        else { isEnabled = false; activity?.onBackPressedDispatcher?.onBackPressed() }
                    }
                }
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(): WebView = WebView(requireContext()).apply {
        // Only the bundled, navigation-restricted origin can access this bridge.
        addJavascriptInterface(object {
            @JavascriptInterface fun startLocalHost() {
                activity?.runOnUiThread {
                    if (android.os.Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else ContextCompat.startForegroundService(context, android.content.Intent(context, LocalQuizHostService::class.java))
                }
            }
            @JavascriptInterface fun stopLocalHost() { context.stopService(android.content.Intent(context, LocalQuizHostService::class.java)) }
            @JavascriptInterface fun localHostStatus(): String = LocalQuizServer.status()
            @JavascriptInterface fun requestLocal(id: String, request: String) {
                if (request.length > 14000) return
                LocalQuizServer.dispatch(request) { response ->
                    post { if (url?.startsWith("https://$ALLOWED_HOST/assets/quiz/") == true) evaluateJavascript("WalkLAN.reply(" + org.json.JSONObject.quote(id) + "," + response + ")", null) }
                }
            }
            @JavascriptInterface fun setLanguages(ui: String, questions: String) {
                val supported = AppLanguages.supported
                if (ui !in supported || questions !in supported) return
                activity?.runOnUiThread {
                    val current = quizWebView?.url?.let(Uri::parse)
                    if (current?.host != ALLOWED_HOST) return@runOnUiThread
                    LanguageSettings(requireContext()).quizLanguage = questions
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(ui))
                }
            }
        }, "TipspromenadHost")
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context)).build()
        webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                loader.shouldInterceptRequest(request.url)
            override fun onPageFinished(view: WebView, url: String) {
                if (organizerPending) dispatchOrganizer()
            }
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return request.url.scheme != "https" || request.url.host != ALLOWED_HOST
            }
        }
        webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                if (!isResumed || origin != "https://$ALLOWED_HOST" && origin != "https://$ALLOWED_HOST/" || locationCallback != null) {
                    callback.invoke(origin, false, false); return
                }
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    callback.invoke(origin, true, false)
                } else {
                    locationCallback = origin to callback
                    locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }
            }
            override fun onGeolocationPermissionsHidePrompt() {
                locationCallback?.let { (origin, callback) -> callback.invoke(origin, false, false) }
                locationCallback = null
            }
            override fun onPermissionRequest(request: PermissionRequest) {
                val page = url?.let(Uri::parse)
                if (!isResumed || request.origin.scheme != "https" || request.origin.host != ALLOWED_HOST ||
                    page?.host != ALLOWED_HOST || !page.path.orEmpty().startsWith("/assets/quiz/") ||
                    request.resources.toList() != listOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                    request.deny(); return
                }
                if (pendingCameraRequest != null) { request.deny(); return }
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    request.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                } else {
                    pendingCameraRequest = request
                    cameraPermission.launch(Manifest.permission.CAMERA)
                }
            }
            override fun onPermissionRequestCanceled(request: PermissionRequest) {
                if (pendingCameraRequest === request) pendingCameraRequest = null
            }
        }
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            safeBrowsingEnabled = true
        }
    }

    private fun startUrl(): String = Uri.parse(START_URL).buildUpon()
        .appendQueryParameter("host", "android")
        .appendQueryParameter("ui", LanguageSettings.uiLanguage(requireContext()))
        .appendQueryParameter("quizLang", LanguageSettings(requireContext()).quizLanguage)
        .appendQueryParameter("correction", LanguageSettings(requireContext()).correctionMode)
        .build().toString()

    fun refreshLanguageIfNeeded() {
        if (binding == null) return
        val startUrl = startUrl()
        if (launchedLanguages != startUrl) {
            launchedLanguages = startUrl
            browserState = null
            quizWebView?.loadUrl(startUrl)
        }
    }

    override fun onResume() {
        super.onResume()
        quizWebView?.onResume()
        refreshLanguageIfNeeded()
    }

    override fun onPause() {
        // The permission dialog can pause the activity while the scanner awaits it.
        if (pendingCameraRequest == null) quizWebView?.evaluateJavascript("window.dispatchEvent(new Event('tipspromenad:stop-camera'));", null)
        quizWebView?.onPause()
        super.onPause()
    }

    override fun onStop() {
        locationCallback?.let { (origin, callback) -> callback.invoke(origin, false, false) }
        locationCallback = null
        pendingCameraRequest?.deny()
        pendingCameraRequest = null
        quizWebView?.evaluateJavascript("window.dispatchEvent(new Event('tipspromenad:stop-camera'));", null)
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val state = Bundle()
        if (quizWebView?.saveState(state) != null) outState.putBundle(STATE_WEBVIEW, state)
        outState.putString(STATE_LANGUAGES, launchedLanguages)
    }

    override fun onDestroyView() {
        // Navigation keeps this fragment on the back stack. Detach the view while
        // retaining its current page; onDestroy releases it when the fragment ends.
        (quizWebView?.parent as? ViewGroup)?.removeView(quizWebView)
        binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        quizWebView?.apply {
            stopLoading()
            webViewClient = WebViewClient()
            destroy()
        }
        quizWebView = null
        super.onDestroy()
    }

    companion object {
        private const val ALLOWED_HOST = "appassets.androidplatform.net"
        private const val START_URL = "https://appassets.androidplatform.net/assets/quiz/index.html"
        private const val STATE_WEBVIEW = "quiz_browser_history"
        private const val STATE_LANGUAGES = "quiz_launch_url"
    }
}
