package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driver.BookletPlan
import com.example.driver.BookletSheetFace
import com.example.model.PrintLayoutMode

@Composable
fun BookletStudioCard(
    layoutMode: PrintLayoutMode,
    onToggleBooklet: (Boolean) -> Unit,
    bookletPlan: BookletPlan,
    onLoadSampleBooklet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBookletActive = layoutMode == PrintLayoutMode.BOOKLET

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("booklet_studio_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBookletActive) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isBookletActive) 2.dp else 1.dp,
            color = if (isBookletActive) Color(0xFF00A86B) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Cabeçalho do modo livreto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isBookletActive) Color(0xFF00A86B) else MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Ícone Livreto",
                            tint = if (isBookletActive) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Modo Livreto (Booklet)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Brochura dobrável com 2 páginas por face",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isBookletActive,
                    onCheckedChange = { onToggleBooklet(it) },
                    modifier = Modifier.testTag("booklet_mode_switch")
                )
            }

            AnimatedVisibility(visible = isBookletActive) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // Resumo das Folhas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFD1F4E4), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatBox(
                                label = "Páginas Originais",
                                value = "${bookletPlan.originalPageCount}"
                            )
                            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFFE2E8F0)))
                            StatBox(
                                label = "Caderno Múltiplo de 4",
                                value = "${bookletPlan.paddedPageCount}"
                            )
                            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFFE2E8F0)))
                            StatBox(
                                label = "Folhas Físicas A4",
                                value = "${bookletPlan.totalSheets} folhas"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Esquema de Imposição Visual das Páginas
                    Text(
                        text = "ORGANIZAÇÃO DAS FOLHAS (FRENTE E VERSO)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF007A4D),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 0 until bookletPlan.totalSheets) {
                            val front = bookletPlan.frontFaces.getOrNull(i)
                            val back = bookletPlan.backFaces.getOrNull(i)
                            if (front != null && back != null) {
                                SheetPairCard(front = front, back = back)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Guia de Duplex Manual na Epson L3250
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Guia Duplex Manual • Alimentador Epson L3250",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            bookletPlan.foldingInstructions.forEach { step ->
                                Text(
                                    text = "• $step",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { onLoadSampleBooklet() },
                        modifier = Modifier.fillMaxWidth().testTag("load_sample_booklet_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Carregar Exemplo de Livreto Formatado", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SheetPairCard(front: BookletSheetFace, back: BookletSheetFace) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "Folha Física #${front.sheetNumber}",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Face Frente
                FaceMiniature(
                    title = "Frente",
                    leftPage = front.leftPageNumber,
                    rightPage = front.rightPageNumber,
                    bgColor = Color(0xFFEBF5FF),
                    borderColor = Color(0xFF93C5FD),
                    modifier = Modifier.weight(1f)
                )

                // Face Verso
                FaceMiniature(
                    title = "Verso",
                    leftPage = back.leftPageNumber,
                    rightPage = back.rightPageNumber,
                    bgColor = Color(0xFFF0FDF4),
                    borderColor = Color(0xFF86EFAC),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun FaceMiniature(
    title: String,
    leftPage: Int?,
    rightPage: Int?,
    bgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PageBadge(leftPage)
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(Color.Gray.copy(alpha = 0.4f))
                )
                PageBadge(rightPage)
            }
        }
    }
}

@Composable
fun PageBadge(pageNumber: Int?) {
    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (pageNumber != null) "Pág $pageNumber" else "Em branco",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (pageNumber != null) Color(0xFF0F172A) else Color.LightGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF0F172A))
        Text(text = label, fontSize = 10.sp, color = Color(0xFF64748B))
    }
}
