package com.nilsson.tipspromenad

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.nilsson.tipspromenad.databinding.FragmentFirstBinding

class FirstFragment : Fragment(R.layout.fragment_first) {
    private var binding: FragmentFirstBinding? = null
    private var quizWebView: WebView? = null
    private var browserState: Bundle? = null
    private var launchedLanguages: String? = null

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
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(): WebView = WebView(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return request.url.scheme != "https" || request.url.host != ALLOWED_HOST
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
        .appendQueryParameter("ui", LanguageSettings.uiLanguage(requireContext()))
        .appendQueryParameter("quizLang", LanguageSettings(requireContext()).quizLanguage)
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
        quizWebView?.onPause()
        super.onPause()
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
        private const val ALLOWED_HOST = "nilsson82.github.io"
        private const val START_URL = "https://nilsson82.github.io/TipspromenadQuizWebPage/"
        private const val STATE_WEBVIEW = "quiz_browser_history"
        private const val STATE_LANGUAGES = "quiz_launch_url"
    }
}
