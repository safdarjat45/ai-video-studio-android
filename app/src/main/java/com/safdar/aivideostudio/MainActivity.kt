package com.safdar.aivideostudio

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var setupPanel: LinearLayout
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var etServerUrl: EditText

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var currentStudioUrl: String = ""

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        filePathCallback?.onReceiveValue(uris)
        filePathCallback = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        setupPanel = findViewById(R.id.setupPanel)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)
        etServerUrl = findViewById(R.id.etServerUrl)

        val btnConnect: Button = findViewById(R.id.btnConnect)
        val btnChangeServer: Button = findViewById(R.id.btnChangeServer)
        val btnGoogleFlow: Button = findViewById(R.id.btnGoogleFlow)

        val prefs = getSharedPreferences("StudioPrefs", Context.MODE_PRIVATE)
        currentStudioUrl = prefs.getString("studio_url", "") ?: ""

        setupWebView()

        if (currentStudioUrl.isEmpty()) {
            showSetupScreen()
        } else {
            etServerUrl.setText(currentStudioUrl)
            loadStudio(currentStudioUrl)
        }

        btnConnect.setOnClickListener {
            val entered = etServerUrl.text.toString().trim()
            if (!entered.startsWith("http")) {
                Toast.makeText(this, "Valid https:// URL darj karein", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            currentStudioUrl = entered
            prefs.edit().putString("studio_url", currentStudioUrl).apply()
            loadStudio(currentStudioUrl)
        }

        btnChangeServer.setOnClickListener {
            showSetupScreen()
        }

        btnGoogleFlow.setOnClickListener {
            // Switch directly to Google Flow inside app (using logged-in Gemini Pro session) or back to Studio
            if (webView.url?.contains("labs.google") == true && currentStudioUrl.isNotEmpty()) {
                webView.loadUrl(currentStudioUrl)
                btnGoogleFlow.text = "Google Flow"
            } else {
                webView.loadUrl("https://labs.google/fx/tools/flow")
                btnGoogleFlow.text = "Back to Studio"
            }
        }

        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }

    private fun showSetupScreen() {
        setupPanel.visibility = View.VISIBLE
        swipeRefresh.visibility = View.GONE
    }

    private fun loadStudio(url: String) {
        setupPanel.visibility = View.GONE
        swipeRefresh.visibility = View.VISIBLE
        webView.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Standard Chrome Mobile User-Agent so Google Login & Flow work smoothly
        settings.userAgentString = settings.userAgentString.replace("; wv", "")

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                swipeRefresh.isRefreshing = false
                progressBar.visibility = View.GONE
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                } else {
                    progressBar.visibility = View.GONE
                }
            }

            // Enable picking reference photos & MP4 clips from Android gallery/storage
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = filePathCallback
                val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                }
                fileChooserLauncher.launch(intent)
                return true
            }
        }

        // Handle direct MP4 / PNG downloads into Android Downloads folder
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            try {
                val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setMimeType(mimeType)
                    addRequestHeader("User-Agent", userAgent)
                    setTitle(fileName)
                    setDescription("Downloading from AI Video Studio...")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                }
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(this, "Downloading $fileName to Downloads folder...", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Download error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
