package com.commonmsm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.commonmsm.data.ModelStorageManager
import com.commonmsm.engine.InferenceController
import com.commonmsm.ui.screens.ChatScreen
import com.commonmsm.ui.screens.ModelManagerScreen
import com.commonmsm.ui.theme.CommonMsmTheme
import com.commonmsm.ui.theme.BrutalBlack

class MainActivity : ComponentActivity() {

    private lateinit var inferenceController: InferenceController
    private lateinit var storageManager: ModelStorageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Protect screen contents from Android Recents switcher snapshot leakage and memory scraping
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )

        inferenceController = InferenceController(this)
        storageManager = ModelStorageManager(this)

        setContent {
            CommonMsmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BrutalBlack
                ) {
                    var currentScreen by remember { mutableStateOf("chat") }
                    var isMoEActive by remember { mutableStateOf(false) }

                    val storageReport = remember { storageManager.getStorageReport() }
                    val discoveredModels = remember { storageManager.scanAvailableModels() }

                    if (currentScreen == "chat") {
                        ChatScreen(
                            inferenceController = inferenceController,
                            onOpenSettings = { currentScreen = "settings" }
                        )
                    } else {
                        ModelManagerScreen(
                            storageReport = storageReport,
                            discoveredModels = discoveredModels,
                            isMoEActive = isMoEActive,
                            onToggleMoE = { active ->
                                isMoEActive = active
                                if (discoveredModels.isNotEmpty()) {
                                    val target = discoveredModels.find { it.isMoE == active } ?: discoveredModels.first()
                                    inferenceController.configureModel(target.file.absolutePath, active)
                                }
                            },
                            onSelectModel = { model ->
                                isMoEActive = model.isMoE
                                inferenceController.configureModel(model.file.absolutePath, model.isMoE)
                                currentScreen = "chat"
                            },
                            onBack = { currentScreen = "chat" }
                        )
                    }
                }
            }
        }
    }
}
