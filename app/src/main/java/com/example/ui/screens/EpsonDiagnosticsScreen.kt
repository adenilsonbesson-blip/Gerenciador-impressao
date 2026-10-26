package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PrinterStatusCard
import com.example.viewmodel.PrintUiState
import com.example.viewmodel.PrintViewModel

@Composable
fun EpsonDiagnosticsScreen(
    state: PrintUiState,
    viewModel: PrintViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("epson_diagnostics_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        // Status atual dos tanques e impressora
        item {
            PrinterStatusCard(
                printer = state.printer,
                discoveredPrinters = state.discoveredPrinters,
                isScanning = state.isScanningWifi,
                onScan = { viewModel.scanWifiPrinters() },
                onSelectPrinter = { viewModel.selectPrinter(it) },
                onUpdateIp = { viewModel.updatePrinterIp(it) }
            )
        }

        // Mensagem de Status de Manutenção
        item {
            AnimatedVisibility(visible = state.maintenanceStatus != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.maintenanceStatus ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        }
                        TextButton(onClick = { viewModel.clearMaintenanceStatus() }) {
                            Text("Fechar", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Ferramentas de Manutenção do Cabeçote
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEDE7F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = Color(0xFF512DA8),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Manutenção Micro Piezo",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Comandos de serviço para Epson L3250 em diante",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Teste de Injetores
                    MaintenanceActionRow(
                        icon = Icons.Default.Print,
                        title = "Verificação de Injetores (Nozzle Check)",
                        description = "Imprime uma folha de teste com linhas padrão para os 4 tanques (BK, C, M, Y). Ajuda a diagnosticar falhas nas linhas.",
                        buttonText = "Imprimir Teste",
                        onClick = { viewModel.executeNozzleCheck() },
                        testTag = "nozzle_check_btn"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Limpeza de Cabeçote
                    MaintenanceActionRow(
                        icon = Icons.Default.CleaningServices,
                        title = "Limpeza de Cabeçote (Head Cleaning)",
                        description = "Pede para a Epson L3250 bombear tinta pelos micro-injetores desobstruindo bicos entupidos.",
                        buttonText = "Iniciar Limpeza",
                        onClick = { viewModel.executeHeadCleaning() },
                        testTag = "head_cleaning_btn"
                    )
                }
            }
        }

        // Informações Técnicas da Conexão Wi-Fi
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Router,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PARÂMETROS DA REDE WI-FI & DRIVER",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TechnicalDetailRow("Modelo Suportado", "${state.printer.modelName} (ESC/P-R)")
                    TechnicalDetailRow("Endereço IP", state.printer.ipAddress)
                    TechnicalDetailRow("Porta de Impressão Direta", "9100 (RAW / JetDirect Socket)")
                    TechnicalDetailRow("Porta de Internet Printing (IPP)", "631 (IPP / CUPS)")
                    TechnicalDetailRow("Endereço MAC", state.printer.macAddress)
                    TechnicalDetailRow("Firmware Epson", state.printer.firmwareVersion)
                    TechnicalDetailRow("Páginas Impressas no Ciclo", "${state.printer.totalPagesPrinted} páginas")
                }
            }
        }
    }
}

@Composable
fun MaintenanceActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().testTag(testTag),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun TechnicalDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
