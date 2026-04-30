package com.nilsson.tipspromenad

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.nilsson.tipspromenad.databinding.FragmentFirstBinding

class FirstFragment : Fragment(R.layout.fragment_first) {

    private var _binding: FragmentFirstBinding? = null

    private val binding get() = _binding!!

    private val allowedHost = "nilsson82.github.io"
    private val startUrl = "https://nilsson82.github.io/TipspromenadQuizWebPage/"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentFirstBinding.bind(view)

        setupMenu()
        setupWebView()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(
            object : MenuProvider {

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menu.clear()
                    menuInflater.inflate(R.menu.menu_main, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_info -> {
                            findNavController().navigate(
                                R.id.action_FirstFragment_to_SecondFragment
                            )
                            true
                        }

                        else -> false
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.apply {
            webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    val host = request.url.host
                    return host != allowedHost
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

            loadUrl(startUrl)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}