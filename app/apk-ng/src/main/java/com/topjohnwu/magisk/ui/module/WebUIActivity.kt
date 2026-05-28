package com.topjohnwu.magisk.ui.module

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.topjohnwu.superuser.Shell
import java.io.File

class WebUIActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_MODULE_ID = "module_id"
        private const val EXTRA_MODULE_NAME = "module_name"

        fun intent(context: Context, moduleId: String, moduleName: String): Intent {
            return Intent(context, WebUIActivity::class.java).apply {
                putExtra(EXTRA_MODULE_ID, moduleId)
                putExtra(EXTRA_MODULE_NAME, moduleName)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val moduleId = intent.getStringExtra(EXTRA_MODULE_ID) ?: return finish()
        val moduleName = intent.getStringExtra(EXTRA_MODULE_NAME) ?: moduleId

        // Webroot extraction logic using root
        val cacheDir = File(cacheDir, "webroot_$moduleId")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        
        if (com.topjohnwu.magisk.core.Config.fakeRoot && moduleId == "fake_module") {
            val fakeWebroot = File(com.topjohnwu.magisk.core.AppContext.cacheDir, "fake_module/webroot")
            Shell.cmd("cp -rf ${fakeWebroot.absolutePath}/* ${cacheDir.absolutePath}").exec()
        } else {
            // Copy webroot from /data/adb/modules/<id>/webroot to cache
            Shell.cmd("cp -rf /data/adb/modules/$moduleId/webroot/* ${cacheDir.absolutePath}").exec()
        }
        Shell.cmd("chmod -R 777 ${cacheDir.absolutePath}").exec()

        val indexHtml = File(cacheDir, "index.html")
        if (!indexHtml.exists()) {
            // Write a basic error if index.html is missing
            indexHtml.writeText("<html><body><h1>Error: index.html not found in webroot</h1></body></html>")
        }

        setContent {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("$moduleName Dashboard") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) { padding ->
                AndroidView(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.allowFileAccess = true
                            settings.domStorageEnabled = true
                            webViewClient = WebViewClient()
                            webChromeClient = WebChromeClient()
                            addJavascriptInterface(MagiskJS(), "MagiskJS")
                            loadUrl("file://${indexHtml.absolutePath}")
                        }
                    }
                )
            }
        }
    }

    class MagiskJS {
        @JavascriptInterface
        fun exec(cmd: String): String {
            val result = Shell.cmd(cmd).exec()
            return result.out.joinToString("\n")
        }

        @JavascriptInterface
        fun execRoot(cmd: String): String {
            // In Magisk, Shell.cmd() is already root if SU is acquired, but we keep this for KSU compat
            val result = Shell.cmd(cmd).exec()
            return result.out.joinToString("\n")
        }
        
        @JavascriptInterface
        fun toast(msg: String) {
            // Minimal toast, typically we would use Context but keeping interface simple
        }
    }
}
