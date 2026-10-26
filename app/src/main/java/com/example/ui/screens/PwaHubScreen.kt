package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.PrintUiState
import com.example.viewmodel.PrintViewModel

@Composable
fun PwaHubScreen(
    state: PrintUiState,
    viewModel: PrintViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pwaUrl = "http://${state.pwaServerIp}:${state.pwaServerPort}"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("pwa_hub_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        // Card do Servidor PWA Local
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF0066CC), Color(0xFF00AEEF))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Servidor Web PWA Local",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Impressão PWA em qualquer dispositivo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (state.isPwaServerRunning) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (state.isPwaServerRunning) "Ativo" else "Parado",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isPwaServerRunning) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Caixa de URL do PWA
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ENDEREÇO NA REDE WI-FI:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = pwaUrl,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("URL PWA", pwaUrl))
                                        Toast.makeText(context, "Link copiado!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(40.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copiar",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pwaUrl))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.size(40.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInBrowser,
                                        contentDescription = "Abrir Navegador",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botão Ligar/Desligar Servidor
                    Button(
                        onClick = { viewModel.togglePwaServer() },
                        modifier = Modifier.fillMaxWidth().testTag("toggle_pwa_server_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isPwaServerRunning) Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isPwaServerRunning) "Desativar Servidor PWA" else "Iniciar Servidor PWA",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Como Funciona o PWA no Wi-Fi
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "COMO IMPRIMIR DE OUTROS APARELHOS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    InstructionStep(
                        number = "1",
                        icon = Icons.Default.Wifi,
                        title = "Mesma Rede Wi-Fi",
                        desc = "Certifique-se de que o computador, tablet ou outro celular esteja conectado no mesmo Wi-Fi."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    InstructionStep(
                        number = "2",
                        icon = Icons.Default.Computer,
                        title = "Abra o Navegador ou Instale como PWA",
                        desc = "Digite $pwaUrl no Chrome, Edge ou Safari. Clique em \"Instalar Aplicativo (PWA)\" se desejar acesso direto da área de trabalho."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    InstructionStep(
                        number = "3",
                        icon = Icons.Default.Smartphone,
                        title = "Impressão Direta",
                        desc = "Selecione Rascunho Vívido ou Livreto na tela do PWA e clique em Imprimir. O comando é roteado instantaneamente para a Epson L3250!"
                    )
                }
            }
        }

        // Recursos Suportados no PWA
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "RECURSOS INCLUÍDOS NO PWA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PwaFeatureRow("⚡ Rascunho Vívido (Vivid Draft)", "Atalho com saturação aprimorada e alta velocidade")
                    PwaFeatureRow("📖 Modo Livreto (Booklet)", "Reordenação de páginas em cadernos")
                    PwaFeatureRow("📶 Driver Wi-Fi Epson L3250+", "Compatível com EcoTank L3250, L3251, L4260, L3150")
                    PwaFeatureRow("📊 Monitor dos Tanques de Tinta", "Leitura em tempo real de Preto, Ciano, Magenta e Amarelo")
                    PwaFeatureRow("🌐 Service Worker Offline", "Cache de interface para carregamento instantâneo")
                }
            }
        }
    }
}

@Composable
fun InstructionStep(
    number: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun PwaFeatureRow(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
