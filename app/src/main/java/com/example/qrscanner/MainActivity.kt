package com.example.qrscanner

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.qrscanner.ui.theme.QRScannerTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnsafeOptInUsageError")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QRScannerTheme {

            }
            val context = LocalContext.current
            val cameraProviderFuture = remember {
                ProcessCameraProvider.getInstance(context)
            }
            var hasCameraPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                )

            }
            var code by remember {
                mutableStateOf("")
            }
            val lifeCycleOwner = LocalLifecycleOwner.current
            val launcher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission())
                { granted ->
                    hasCameraPermission = granted

                }
            LaunchedEffect(key1 = true) {
                launcher.launch(android.Manifest.permission.CAMERA)

            }

            Column(
                modifier = Modifier.fillMaxSize(),
            ) {

                if (hasCameraPermission) {
                    AndroidView(factory = { context ->

                        val previewView = PreviewView(context)
                        val preview = androidx.camera.core.Preview.Builder().build()
                        val selector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setTargetResolution(Size(previewView.width, previewView.height))
                            .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        imageAnalyzer.setAnalyzer(ContextCompat.getMainExecutor(context), ImageAnalyzer { result ->
                            code = result

                        })

                        try {
                            cameraProviderFuture.get()
                                .bindToLifecycle(lifeCycleOwner, selector, preview, imageAnalyzer)
                        } catch (e: Exception) {
                            e.printStackTrace()

                        }
                        previewView
                    },Modifier.weight(1f))
                    Text(text = code, fontSize = 20.sp,
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    fontWeight = FontWeight.Bold)
                }
            }


        }
    }
}


