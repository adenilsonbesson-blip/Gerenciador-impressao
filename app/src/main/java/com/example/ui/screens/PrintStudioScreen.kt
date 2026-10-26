package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.PaperSize
import com.example.model.PaperType
import com.example.model.PrintLayoutMode
import com.example.model.PrintQuality
import com.example.ui.components.BookletStudioCard
import com.example.ui.components.PrintQualitySelector
import com.example.ui.components.PrinterStatusCard
import com.example.ui.theme.EpsonBluePrimary
import com.example.ui.theme.VividDraftOrange
import com.example.viewmodel.PrintUiState
import com.example.viewmodel.PrintViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintStudioScreen(
    state: PrintUiState,
    viewModel: PrintViewModel,
    modifier: Modifier = Modifier
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setSelectedImageUri(uri)
    }

    var paperTypeExpanded by remember { mutableStateOf(false) }
    var paperSizeExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("print_studio_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        // Banner Hero Ilustrativo da Impressora Epson L3250
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.hero_epson_printer_1793019994897),
                        contentDescription = "Epson EcoTank L3250 Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradiente de sobreposição para texto legível
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.75f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(0.65f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(VividDraftOrange)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "RASCUNHO VÍVIDO & LIVRETO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "EcoPrint Wi-Fi Hub",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Driver Wi-Fi Epson EcoTank L3250+",
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Cartão da Impressora Epson e Níveis de Tinta EcoTank
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

        // Seletor de Modo Livreto (Brochura Dobrável)
        item {
            BookletStudioCard(
                layoutMode = state.selectedLayoutMode,
                onToggleBooklet = { isEnabled ->
                    viewModel.setLayoutMode(if (isEnabled) PrintLayoutMode.BOOKLET else PrintLayoutMode.STANDARD)
                },
                bookletPlan = state.bookletPlan,
                onLoadSampleBooklet = { viewModel.loadPresetSample("booklet") }
            )
        }

        // Seletor de Qualidades com destaque no Rascunho Vívido
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.padding(18.dp)) {
                    PrintQualitySelector(
                        selectedQuality = state.selectedQuality,
                        onSelectQuality = { viewModel.setQuality(it) }
                    )
                }
            }
        }

        // Tipo e Tamanho de Papel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "PAPEL E MÍDIA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Dropdown Tipo de Papel
                        ExposedDropdownMenuBox(
                            expanded = paperTypeExpanded,
                            onExpandedChange = { paperTypeExpanded = it },
                            modifier = Modifier.weight(1.3f)
                        ) {
                            OutlinedTextField(
                                value = state.selectedPaperType.displayName.take(18) + "...",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo Papel", fontSize = 11.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paperTypeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = paperTypeExpanded,
                                onDismissRequest = { paperTypeExpanded = false }
                            ) {
                                PaperType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.displayName, fontSize = 13.sp) },
                                        onClick = {
                                            viewModel.setPaperType(type)
                                            paperTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Dropdown Tamanho
                        ExposedDropdownMenuBox(
                            expanded = paperSizeExpanded,
                            onExpandedChange = { paperSizeExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = state.selectedPaperSize.displayName.take(12),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tamanho", fontSize = 11.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paperSizeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = paperSizeExpanded,
                                onDismissRequest = { paperSizeExpanded = false }
                            ) {
                                PaperSize.values().forEach { size ->
                                    DropdownMenuItem(
                                        text = { Text(size.displayName, fontSize = 13.sp) },
                                        onClick = {
                                            viewModel.setPaperSize(size)
                                            paperSizeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cópias e Modo de Cor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Cópias: ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            OutlinedButton(
                                onClick = { viewModel.setCopies(state.copies - 1) },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text("-", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${state.copies}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.setCopies(state.copies + 1) },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text("+", fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { viewModel.setColorMode(!state.isColor) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (state.isColor) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                            ) {
                                Text(
                                    text = if (state.isColor) "🎨 Colorido" else "⬛ Preto & Branco",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Editor do Documento e Conteúdo
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONTEÚDO DO DOCUMENTO",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        TextButton(onClick = { viewModel.loadPresetSample("vivid_draft") }) {
                            Text("Carregar Exemplo Vívido", fontSize = 11.sp, color = VividDraftOrange)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.documentTitle,
                        onValueChange = { viewModel.setDocumentTitle(it) },
                        label = { Text("Título do Documento") },
                        modifier = Modifier.fillMaxWidth().testTag("doc_title_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = state.documentText,
                        onValueChange = { viewModel.setDocumentText(it) },
                        label = { Text("Texto do Documento") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .testTag("doc_text_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Anexo de Imagem / Foto
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.testTag("attach_photo_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.selectedImageUri != null) "Foto Anexada ✓" else "Anexar Imagem / Foto",
                                fontSize = 12.sp
                            )
                        }

                        if (state.selectedImageUri != null) {
                            TextButton(onClick = { viewModel.setSelectedImageUri(null) }) {
                                Text("Remover", fontSize = 11.sp, color = Color.Red)
                            }
                        }
                    }

                    if (state.selectedImageUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = state.selectedImageUri,
                                contentDescription = "Imagem selecionada",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // Botões Principais de Impressão
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.startPrintJob(useAndroidSpooler = false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("print_direct_wifi_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.selectedQuality == PrintQuality.VIVID_DRAFT) VividDraftOrange else EpsonBluePrimary
                    )
                ) {
                    Icon(
                        imageVector = if (state.selectedQuality == PrintQuality.VIVID_DRAFT) Icons.Default.Bolt else Icons.Default.Print,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (state.selectedLayoutMode == PrintLayoutMode.BOOKLET)
                            "Imprimir Livreto via Wi-Fi (Epson L3250)"
                        else
                            "Imprimir via Wi-Fi Direto (${state.selectedQuality.title})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.startPrintJob(useAndroidSpooler = true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("print_android_spooler_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Imprimir via Spooler do Android / PDF", fontSize = 13.sp)
                }
            }
        }
    }

    // Diálogo de Progresso do Envio para a Impressora Wi-Fi
    if (state.isPrinting && state.printProgress != null) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Transmitindo para Epson L3250", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = state.printProgress.stage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { state.printProgress.percent / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${state.printProgress.percent}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${state.printProgress.bytesSent / 1024} KB enviados",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Diálogo de Sucesso
    if (state.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSuccessDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF00A86B),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Impressão Concluída!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "O trabalho \"${state.documentTitle}\" foi enviado com sucesso para a Epson EcoTank L3250 via Wi-Fi.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (state.selectedLayoutMode == PrintLayoutMode.BOOKLET) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Livreto: Não esqueça de virar as folhas no alimentador traseiro conforme as instruções para imprimir o verso!",
                                fontSize = 12.sp,
                                color = Color(0xFF1B5E20),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissSuccessDialog() }) {
                    Text("OK")
                }
            }
        )
    }
}
