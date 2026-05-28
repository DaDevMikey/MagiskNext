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
            val fakeHtml = File(cacheDir, "index.html")
            fakeHtml.writeText(
                """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Module Dashboard</title>
                    <style>
                        :root { --md-primary: #D0BCFF; --md-surface: #1C1B1F; --md-surface-container: #2B2930; --md-on-surface: #E6E1E5; --md-on-surface-variant: #CAC4D0; --md-outline: #938F99; }
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body { font-family: 'Google Sans', 'Roboto', system-ui, sans-serif; background: var(--md-surface); color: var(--md-on-surface); padding: 24px; min-height: 100vh; }
                        h1 { font-weight: 400; font-size: 28px; margin-bottom: 24px; letter-spacing: -0.5px; }
                        .card { background: var(--md-surface-container); border-radius: 16px; padding: 20px; margin-bottom: 16px; transition: transform 0.2s ease, box-shadow 0.2s ease; }
                        .card:active { transform: scale(0.98); }
                        .card h2 { font-size: 16px; font-weight: 500; margin-bottom: 8px; color: var(--md-primary); }
                        .card p { font-size: 14px; color: var(--md-on-surface-variant); line-height: 1.5; }
                        .btn { display: inline-flex; align-items: center; justify-content: center; background: var(--md-primary); color: #1C1B1F; border: none; padding: 12px 24px; border-radius: 100px; font-size: 14px; font-weight: 500; letter-spacing: 0.1px; cursor: pointer; transition: all 0.2s ease; margin-top: 12px; }
                        .btn:active { transform: scale(0.95); opacity: 0.8; }
                        .snackbar { position: fixed; bottom: -60px; left: 50%; transform: translateX(-50%); background: #E8DEF8; color: #1C1B1F; padding: 14px 24px; border-radius: 12px; font-size: 14px; font-weight: 500; transition: bottom 0.3s cubic-bezier(0.2, 0, 0, 1); z-index: 100; box-shadow: 0 6px 20px rgba(0,0,0,0.4); }
                        .snackbar.show { bottom: 24px; }
                        .chip { display: inline-flex; padding: 6px 16px; border-radius: 8px; background: rgba(208,188,255,0.12); color: var(--md-primary); font-size: 12px; font-weight: 500; margin-right: 8px; margin-top: 8px; }
                    </style>
                </head>
                <body>
                    <h1>Mock Dashboard</h1>
                    <div class="card">
                        <h2>System Info</h2>
                        <p>Status: Active (Mock)</p>
                        <div><span class="chip">Zygisk</span><span class="chip">MagiskHide</span></div>
                    </div>
                    <div class="card">
                        <h2>Configuration</h2>
                        <p>Adjust module parameters and preferences.</p>
                        <button class="btn" onclick="showSnackbar('Configuration saved successfully')">Save Configuration</button>
                    </div>
                    <div class="card">
                        <h2>Diagnostics</h2>
                        <p>Run a self-test to check module health.</p>
                        <button class="btn" onclick="showSnackbar('All systems nominal ✓')">Run Diagnostics</button>
                    </div>
                    <div id="snackbar" class="snackbar"></div>
                    <script>
                        function showSnackbar(msg) {
                            const sb = document.getElementById('snackbar');
                            sb.textContent = msg;
                            sb.classList.add('show');
                            setTimeout(() => sb.classList.remove('show'), 2500);
                        }
                    </script>
                </body>
                </html>
                """.trimIndent()
            )
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
