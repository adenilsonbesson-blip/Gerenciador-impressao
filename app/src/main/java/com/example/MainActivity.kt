package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.EpsonDiagnosticsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.PrintStudioScreen
import com.example.ui.screens.PwaHubScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PrintViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PrintViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                // Suporte a botão Voltar para retornar à tela inicial se em sub-tela
                BackHandler(enabled = state.currentTab != 0) {
                    viewModel.setTab(0)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "EcoPrint Hub",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFFE8F5E9))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Wifi,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "L3250 Wi-Fi",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1B5E20)
                                            )
                                        }
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("main_bottom_nav"),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            NavigationBarItem(
                                selected = state.currentTab == 0,
                                onClick = { viewModel.setTab(0) },
                                icon = {
                                    Icon(
                                        imageVector = if (state.currentTab == 0) Icons.Filled.Print else Icons.Outlined.Print,
                                        contentDescription = "Imprimir"
                                    )
                                },
                                label = { Text("Imprimir", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("nav_tab_print")
                            )

                            NavigationBarItem(
                                selected = state.currentTab == 1,
                                onClick = { viewModel.setTab(1) },
                                icon = {
                                    Icon(
                                        imageVector = if (state.currentTab == 1) Icons.Filled.Language else Icons.Outlined.Language,
                                        contentDescription = "PWA Hub"
                                    )
                                },
                                label = { Text("PWA Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("nav_tab_pwa")
                            )

                            NavigationBarItem(
                                selected = state.currentTab == 2,
                                onClick = { viewModel.setTab(2) },
                                icon = {
                                    Icon(
                                        imageVector = if (state.currentTab == 2) Icons.Filled.Build else Icons.Outlined.Build,
                                        contentDescription = "Diagnóstico"
                                    )
                                },
                                label = { Text("Diagnóstico", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("nav_tab_diag")
                            )

                            NavigationBarItem(
                                selected = state.currentTab == 3,
                                onClick = { viewModel.setTab(3) },
                                icon = {
                                    Icon(
                                        imageVector = if (state.currentTab == 3) Icons.Filled.History else Icons.Outlined.History,
                                        contentDescription = "Histórico"
                                    )
                                },
                                label = { Text("Histórico", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("nav_tab_history")
                            )
                        }
                    }
                ) { innerPadding ->
                    Crossfade(
                        targetState = state.currentTab,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) { tabIndex ->
                        when (tabIndex) {
                            0 -> PrintStudioScreen(state = state, viewModel = viewModel)
                            1 -> PwaHubScreen(state = state, viewModel = viewModel)
                            2 -> EpsonDiagnosticsScreen(state = state, viewModel = viewModel)
                            3 -> HistoryScreen(jobs = state.printJobsHistory)
                        }
                    }
                }
            }
        }
    }
}
