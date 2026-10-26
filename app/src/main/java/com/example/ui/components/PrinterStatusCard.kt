package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EpsonPrinterDevice
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkCyan
import com.example.ui.theme.InkMagenta
import com.example.ui.theme.InkYellow

@Composable
fun PrinterStatusCard(
    printer: EpsonPrinterDevice,
    discoveredPrinters: List<EpsonPrinterDevice>,
    isScanning: Boolean,
    onScan: () -> Unit,
    onSelectPrinter: (EpsonPrinterDevice) -> Unit,
    onUpdateIp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPrinterDialog by remember { mutableStateOf(false) }
    var showEditIpDialog by remember { mutableStateOf(false) }
    var tempIp by remember { mutableStateOf(printer.ipAddress) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("printer_status_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Linha do Título com Ícone e Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Ícone Impressora",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = printer.modelName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "Wi-Fi",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${printer.ipAddress}:${printer.port}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Badge de Status Online
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pronta",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Medidores dos Tanques de Tinta EcoTank (CMYK)
            Text(
                text = "NÍVEIS DE TINTA ECOTANK",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InkTankGauge(
                    name = "Preto (BK)",
                    level = printer.blackInkLevel,
                    inkColor = InkBlack,
                    modifier = Modifier.weight(1f)
                )
                InkTankGauge(
                    name = "Ciano (C)",
                    level = printer.cyanInkLevel,
                    inkColor = InkCyan,
                    modifier = Modifier.weight(1f)
                )
                InkTankGauge(
                    name = "Magenta (M)",
                    level = printer.magentaInkLevel,
                    inkColor = InkMagenta,
                    modifier = Modifier.weight(1f)
                )
                InkTankGauge(
                    name = "Amarelo (Y)",
                    level = printer.yellowInkLevel,
                    inkColor = InkYellow,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ações de conexão
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showEditIpDialog = true },
                    modifier = Modifier.testTag("edit_ip_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar IP",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Configurar IP", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        showPrinterDialog = true
                        onScan()
                    },
                    modifier = Modifier.testTag("switch_printer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Buscar Wi-Fi",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buscar na Rede", fontSize = 12.sp)
                }
            }
        }
    }

    // Diálogo de Seleção de Impressora na Rede Wi-Fi
    if (showPrinterDialog) {
        AlertDialog(
            onDismissRequest = { showPrinterDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Impressoras Epson na Rede Wi-Fi", fontWeight = FontWeight.Bold)
                    if (isScanning) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Dispositivos Epson compatíveis detectados na sub-rede local:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    discoveredPrinters.forEach { device ->
                        val isSelected = device.ipAddress == printer.ipAddress
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onSelectPrinter(device)
                                    showPrinterDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = device.modelName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "IP: ${device.ipAddress} • Porta ${device.port}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selecionada",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onScan() }) {
                    Text("Reescanear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrinterDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    // Diálogo de Alteração de IP Manual
    if (showEditIpDialog) {
        AlertDialog(
            onDismissRequest = { showEditIpDialog = false },
            title = { Text("Endereço IP da Epson L3250") },
            text = {
                Column {
                    Text(
                        "Insira o IP fixo ou atribuído pelo roteador Wi-Fi à sua impressora Epson EcoTank:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempIp,
                        onValueChange = { tempIp = it },
                        label = { Text("Ex: 192.168.1.150") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("printer_ip_input")
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateIp(tempIp)
                    showEditIpDialog = false
                }) {
                    Text("Salvar IP")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditIpDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun InkTankGauge(
    name: String,
    level: Int,
    inkColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 8.dp, horizontal = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Barra vertical do nível
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFE2E8F0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (level / 100f).coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(inkColor)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$level%",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
