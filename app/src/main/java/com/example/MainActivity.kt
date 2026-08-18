package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReceiptDetailScreen
import com.example.ui.screens.ReviewScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ReceiptViewModel
import com.example.ui.viewmodel.ScanViewModel
import com.example.ui.viewmodel.SettingsViewModel

sealed interface Screen {
    object Home : Screen
    object Scan : Screen
    data class Review(val existingReceiptId: Long? = null) : Screen
    data class Detail(val receiptId: Long) : Screen
    object Analytics : Screen
    object Settings : Screen
}

class MainActivity : ComponentActivity() {

    private val receiptViewModel: ReceiptViewModel by viewModels()
    private val scanViewModel: ScanViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialReceiptId = intent?.getLongExtra("receipt_id", -1L)?.takeIf { it > 0 }

        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    ReceiptVaultApp(
                        receiptViewModel = receiptViewModel,
                        scanViewModel = scanViewModel,
                        settingsViewModel = settingsViewModel,
                        initialReceiptId = initialReceiptId
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiptVaultApp(
    receiptViewModel: ReceiptViewModel,
    scanViewModel: ScanViewModel,
    settingsViewModel: SettingsViewModel,
    initialReceiptId: Long?
) {
    val backstack = remember {
        mutableStateListOf<Screen>().apply {
            add(Screen.Home)
            if (initialReceiptId != null) {
                add(Screen.Detail(initialReceiptId))
            }
        }
    }

    val currentScreen = backstack.lastOrNull() ?: Screen.Home

    fun navigateTo(screen: Screen) {
        backstack.add(screen)
    }

    fun navigateBack() {
        if (backstack.size > 1) {
            backstack.removeAt(backstack.size - 1)
        }
    }

    BackHandler(enabled = backstack.size > 1) {
        navigateBack()
    }

    when (val screen = currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                viewModel = receiptViewModel,
                onNavigateToScan = { navigateTo(Screen.Scan) },
                onNavigateToDetail = { id -> navigateTo(Screen.Detail(id)) },
                onNavigateToAnalytics = { navigateTo(Screen.Analytics) },
                onNavigateToSettings = { navigateTo(Screen.Settings) }
            )
        }

        is Screen.Scan -> {
            ScanScreen(
                viewModel = scanViewModel,
                onNavigateBack = { navigateBack() },
                onNavigateToReview = {
                    navigateTo(Screen.Review())
                }
            )
        }

        is Screen.Review -> {
            ReviewScreen(
                scanViewModel = scanViewModel,
                receiptViewModel = receiptViewModel,
                existingReceiptId = screen.existingReceiptId,
                onNavigateBack = { navigateBack() },
                onSaved = { receiptId ->
                    // Clear review/scan from backstack and navigate to detail or home
                    while (backstack.size > 1 && (backstack.last() is Screen.Review || backstack.last() is Screen.Scan)) {
                        backstack.removeAt(backstack.size - 1)
                    }
                    navigateTo(Screen.Detail(receiptId))
                }
            )
        }

        is Screen.Detail -> {
            ReceiptDetailScreen(
                receiptId = screen.receiptId,
                receiptViewModel = receiptViewModel,
                scanViewModel = scanViewModel,
                onNavigateBack = { navigateBack() },
                onNavigateToEdit = { editId ->
                    navigateTo(Screen.Review(existingReceiptId = editId))
                }
            )
        }

        is Screen.Analytics -> {
            AnalyticsScreen(
                receiptViewModel = receiptViewModel,
                onNavigateBack = { navigateBack() }
            )
        }

        is Screen.Settings -> {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navigateBack() }
            )
        }
    }
}
