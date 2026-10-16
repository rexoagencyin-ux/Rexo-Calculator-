package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalculatorTab
import com.example.ui.components.AdBanner
import com.example.ui.dialogs.CurrencyPickerDialog
import com.example.ui.dialogs.HistorySheet
import com.example.ui.dialogs.PrivacyPolicySheet
import com.example.ui.screens.DiscountCalculatorScreen
import com.example.ui.screens.GstCalculatorScreen
import com.example.ui.screens.PercentageCalculatorScreen
import com.example.ui.screens.VolumeCalculatorScreen
import com.example.ui.screens.WeightCalculatorScreen
import com.example.ui.theme.RexoCalculatorTheme
import com.example.viewmodel.CalculatorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RexoCalculatorTheme {
                RexoApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RexoApp(viewModel: CalculatorViewModel) {
    val activeTab by viewModel.activeTab.collectAsState()
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val historyList by viewModel.history.collectAsState()
    val showHistorySheet by viewModel.showHistorySheet.collectAsState()
    val showPrivacySheet by viewModel.showPrivacySheet.collectAsState()
    val showCurrencyDialog by viewModel.showCurrencyDialog.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "R",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Currency Picker Chip Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { viewModel.setCurrencyDialogVisible(true) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("action_currency_picker"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeCurrency.symbol,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = activeCurrency.code,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // History Action with badge
                    IconButton(
                        onClick = { viewModel.setHistorySheetVisible(true) },
                        modifier = Modifier.testTag("action_history_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (historyList.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ) {
                                        Text(text = if (historyList.size > 99) "99+" else historyList.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_iconsax_history),
                                contentDescription = stringResource(R.string.action_history),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Privacy & Policy Button
                    IconButton(
                        onClick = { viewModel.setPrivacySheetVisible(true) },
                        modifier = Modifier.testTag("action_privacy_button")
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_iconsax_shield),
                            contentDescription = stringResource(R.string.action_privacy),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Non-intrusive AdMob banner anchored above Navigation Bar
                AdBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp)
                )

                // Bottom Navigation Bar with 5 tabs
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    val tabs = listOf(
                        NavTabItem(CalculatorTab.PERCENTAGE, stringResource(R.string.tab_percentage), R.drawable.ic_iconsax_percentage),
                        NavTabItem(CalculatorTab.GST_TAX, stringResource(R.string.tab_gst), R.drawable.ic_iconsax_receipt),
                        NavTabItem(CalculatorTab.WEIGHT_PRICE, stringResource(R.string.tab_weight), R.drawable.ic_iconsax_weight),
                        NavTabItem(CalculatorTab.VOLUME_PRICE, stringResource(R.string.tab_volume), R.drawable.ic_iconsax_volume),
                        NavTabItem(CalculatorTab.DISCOUNT, stringResource(R.string.tab_discount), R.drawable.ic_iconsax_discount)
                    )

                    tabs.forEach { item ->
                        val isSelected = activeTab == item.tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(item.tab) },
                            icon = {
                                Icon(
                                    painter = painterResource(item.iconRes),
                                    contentDescription = item.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_${item.tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "CalculatorTabAnimation"
            ) { targetTab ->
                when (targetTab) {
                    CalculatorTab.PERCENTAGE -> PercentageCalculatorScreen(viewModel = viewModel)
                    CalculatorTab.GST_TAX -> GstCalculatorScreen(viewModel = viewModel)
                    CalculatorTab.WEIGHT_PRICE -> WeightCalculatorScreen(viewModel = viewModel)
                    CalculatorTab.VOLUME_PRICE -> VolumeCalculatorScreen(viewModel = viewModel)
                    CalculatorTab.DISCOUNT -> DiscountCalculatorScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Modal Sheets
    if (showHistorySheet) {
        HistorySheet(
            historyList = historyList,
            onClearHistory = { viewModel.clearHistory() },
            onDismiss = { viewModel.setHistorySheetVisible(false) }
        )
    }

    if (showPrivacySheet) {
        PrivacyPolicySheet(
            onDismiss = { viewModel.setPrivacySheetVisible(false) }
        )
    }

    if (showCurrencyDialog) {
        CurrencyPickerDialog(
            selectedCurrency = activeCurrency,
            onSelectCurrency = { viewModel.setCurrency(it) },
            onDismiss = { viewModel.setCurrencyDialogVisible(false) }
        )
    }
}

private data class NavTabItem(
    val tab: CalculatorTab,
    val title: String,
    val iconRes: Int
)
