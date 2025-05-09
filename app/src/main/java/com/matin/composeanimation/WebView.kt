package com.matin.composeanimation

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * An enhanced WebView implementation for Jetpack Compose.
 *
 * @param initialUrl The starting URL to load
 * @param modifier Modifier for styling
 * @param enableJavaScript Whether to enable JavaScript execution
 * @param enableDomStorage Whether to enable DOM storage API
 * @param userAgent Custom user agent string (null for default)
 * @param showControls Whether to show navigation controls
 * @param showUrlBar Whether to show an editable URL bar
 * @param onScreenshot Callback for screenshot capture feature
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedWebView(
    initialUrl: String,
    modifier: Modifier = Modifier,
    enableJavaScript: Boolean = true,
    enableDomStorage: Boolean = true,
    userAgent: String? = null,
    showControls: Boolean = true,
    showUrlBar: Boolean = false,
    onScreenshot: ((Bitmap) -> Unit)? = null
) {

    val state = rememberSaveable(stateSaver = WebViewStateSaver) { mutableStateOf(WebViewState(url = initialUrl)) }
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    var showUrlInput by remember { mutableStateOf(showUrlBar) }
    var urlInputValue by remember { mutableStateOf(initialUrl) }

    // Track if the bundle exists for state restoration
    val webViewState = remember { mutableStateOf<Bundle?>(null) }

    BackHandler(enabled = state.value.canGoBack) {
        webViewRef.value?.goBack()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                if (showUrlInput) {
                    OutlinedTextField(
                        value = urlInputValue,
                        onValueChange = { urlInputValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter URL") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = {
                            if (urlInputValue.isNotEmpty()) {
                                // Add http:// if protocol is missing
                                val formattedUrl = if (!urlInputValue.startsWith("http://") &&
                                    !urlInputValue.startsWith("https://")
                                ) {
                                    "https://$urlInputValue"
                                } else {
                                    urlInputValue
                                }
                                webViewRef.value?.loadUrl(formattedUrl)
                                state.value = state.value.copy(url = formattedUrl)
                            }
                        }),
                        trailingIcon = {
                            IconButton(onClick = { showUrlInput = false }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Close URL input"
                                )
                            }
                        }
                    )
                } else {
                    Text(state.value.pageTitle ?: "Loading...")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            navigationIcon = {
                if (showControls) {
                    NavigationIcons(state, webViewRef)
                }
            },
            actions = {
                if (!showUrlInput && showControls) {
                    IconButton(onClick = {
                        urlInputValue = state.value.url
                        showUrlInput = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Edit URL"
                        )
                    }

                    IconButton(onClick = { webViewRef.value?.reload() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            }
        )

        if (state.value.isLoading && state.value.progress < 100) {
            LinearProgressIndicator(
                progress = state.value.progress / 100f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    createWebView(
                        context = context,
                        state = state,
                        enableJavaScript = enableJavaScript,
                        enableDomStorage = enableDomStorage,
                        userAgent = userAgent,
                        initialUrl = initialUrl
                    ).also {
                        webViewRef.value = it
                        // Restore state if available
                        webViewState.value?.let { savedState ->
                            it.restoreState(savedState)
                        } ?: it.loadUrl(initialUrl)
                    }
                },
                update = { webView ->
                    if (state.value.url != webView.url) {
                        webView.loadUrl(state.value.url)
                    }
                }
            )

            if (state.value.hasError) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        androidx.compose.material3.TextButton(
                            onClick = { webViewRef.value?.reload() }
                        ) {
                            Text("Retry")
                        }
                    }
                ) {
                    Text(state.value.errorMessage ?: "An error occurred")
                }
            }

            if (state.value.isLoading && state.value.progress < 30) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            webViewRef.value?.apply {
                // Save state before destroying
                val bundle = Bundle()
                saveState(bundle)
                webViewState.value = bundle

                // Clean up resources
                stopLoading()
                clearHistory()
                clearCache(true)
                destroy()
            }
        }
    }
}

@Composable
private fun NavigationIcons(
    state: MutableState<WebViewState>,
    webViewRef: MutableState<WebView?>
) {
    Row {
        IconButton(
            onClick = {
                if (state.value.canGoBack) {
                    webViewRef.value?.goBack()
                }
            },
            enabled = state.value.canGoBack
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Spacer(Modifier.width(6.dp))

        IconButton(
            onClick = {
                if (state.value.canGoForward) {
                    webViewRef.value?.goForward()
                }
            },
            enabled = state.value.canGoForward
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Forward"
            )
        }
    }
}

/**
 * Helper function to create and configure a WebView
 */
private fun createWebView(
    context: Context,
    state: MutableState<WebViewState>,
    enableJavaScript: Boolean,
    enableDomStorage: Boolean,
    userAgent: String?,
    initialUrl: String
): WebView {
    return WebView(context).apply {
        settings.apply {
            javaScriptEnabled = enableJavaScript
            domStorageEnabled = enableDomStorage
            userAgent?.let { userAgentString = it }
            // Additional settings for better browsing experience
            loadsImagesAutomatically = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }

        webViewClient = object : WebViewClient() {
            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: Bitmap?
            ) {
                url?.let {
                    state.value = state.value.copy(
                        url = it,
                        isLoading = true,
                        hasError = false,
                        errorMessage = null
                    )
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                view?.let {
                    state.value = state.value.copy(
                        canGoBack = it.canGoBack(),
                        canGoForward = it.canGoForward(),
                        pageTitle = it.title,
                        isLoading = false
                    )
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                state.value = state.value.copy(
                    hasError = true,
                    errorMessage = error?.description?.toString() ?: "Failed to load page",
                    isLoading = false
                )
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                state.value = state.value.copy(progress = newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                state.value = state.value.copy(pageTitle = title)
            }

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
            }
        }
    }
}

data class WebViewState(
    var url: String = "",
    var canGoBack: Boolean = false,
    var canGoForward: Boolean = false,
    var progress: Int = 0,
    var pageTitle: String? = null,
    var isLoading: Boolean = false,
    var hasError: Boolean = false,
    var errorMessage: String? = null,
)

val WebViewStateSaver: Saver<WebViewState, *> = Saver(
    save = {
        listOf(
            it.url,
            it.canGoBack,
            it.canGoForward,
            it.progress,
            it.pageTitle,
            it.isLoading,
            it.hasError,
            it.errorMessage
        )
    },
    restore = {
        WebViewState(
            url = it[0] as String,
            canGoBack = it[1] as Boolean,
            canGoForward = it[2] as Boolean,
            progress = it[3] as Int,
            pageTitle = it[4] as String?,
            isLoading = it[5] as Boolean,
            hasError = it[6] as Boolean,
            errorMessage = it[7] as String?
        )
    }
)