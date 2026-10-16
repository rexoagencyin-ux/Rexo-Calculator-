package com.example.ui.components

import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

private const val TAG = "AdBanner"
const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

fun isRunningOnEmulator(): Boolean {
    return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
        || Build.FINGERPRINT.startsWith("generic")
        || Build.FINGERPRINT.startsWith("unknown")
        || Build.FINGERPRINT.contains("robolectric")
        || Build.HARDWARE.contains("goldfish")
        || Build.HARDWARE.contains("ranchu")
        || Build.MODEL.contains("google_sdk")
        || Build.MODEL.contains("Emulator")
        || Build.MODEL.contains("Android SDK built for x86")
        || Build.MANUFACTURER.contains("Genymotion")
        || Build.PRODUCT.contains("sdk_google")
        || Build.PRODUCT.contains("google_sdk")
        || Build.PRODUCT.contains("sdk")
        || Build.PRODUCT.contains("sdk_x86")
        || Build.PRODUCT.contains("vbox86p")
        || Build.PRODUCT.contains("emulator")
        || Build.PRODUCT.contains("simulator")
}

@Composable
fun AdBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEmulator = remember { isRunningOnEmulator() }

    // Initialize MobileAds on real hardware devices
    if (!isEmulator) {
        remember {
            try {
                val reqConfig = RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                    .build()
                MobileAds.setRequestConfiguration(reqConfig)
                MobileAds.initialize(context) {}
            } catch (e: Exception) {
                Log.e(TAG, "AdMob initialization error", e)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .testTag("admob_banner_container"),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = TEST_BANNER_AD_UNIT_ID

                    if (!isEmulator) {
                        try {
                            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                        } catch (e: Exception) {
                            Log.w(TAG, "Could not set software layer type", e)
                        }
                        adListener = object : AdListener() {
                            override fun onAdFailedToLoad(error: LoadAdError) {
                                super.onAdFailedToLoad(error)
                                Log.d(TAG, "Ad failed to load: ${error.message}")
                            }
                        }
                        try {
                            loadAd(AdRequest.Builder().build())
                        } catch (e: Exception) {
                            Log.e(TAG, "Error loading ad", e)
                        }
                    } else {
                        // In virtual/emulator environments without adservices daemon,
                        // render test banner badge inside AdView to avoid ServiceUnavailableException & MESA logs
                        val testBanner = TextView(ctx).apply {
                            text = "Ad • Google AdMob Test Banner (320x50)"
                            textSize = 12f
                            setTextColor(android.graphics.Color.DKGRAY)
                            gravity = Gravity.CENTER
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                        addView(testBanner)
                    }
                }
            }
        )
    }
}
