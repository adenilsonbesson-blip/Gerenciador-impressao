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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrintQuality
import com.example.ui.theme.DraftGray
import com.example.ui.theme.EpsonBluePrimary
import com.example.ui.theme.HighQualityPurple
import com.example.ui.theme.VividDraftOrange

@Composable
fun PrintQualitySelector(
    selectedQuality: PrintQuality,
    onSelectQuality: (PrintQuality) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "QUALIDADE DE IMPRESSÃO",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            // Destaque se Rascunho Vívido estiver ativo
            if (selectedQuality == PrintQuality.VIVID_DRAFT) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFFFEDE0))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "⭐ Rascunho Vívido Ativo",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VividDraftOrange
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Lista de qualidades
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrintQuality.values().forEach { quality ->
                val isSelected = quality == selectedQuality
                val isVivid = quality == PrintQuality.VIVID_DRAFT

                val borderColor = when {
                    isSelected && isVivid -> VividDraftOrange
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> Color(0xFFE2E8F0)
                }

                val containerColor = when {
                    isSelected && isVivid -> Color(0xFFFFF7ED)
                    isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.surface
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectQuality(quality) }
                        .testTag("quality_option_${quality.name.lowercase()}"),
                    colors = CardDefaults.cardColors(containerColor = containerColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (quality) {
                                            PrintQuality.VIVID_DRAFT -> VividDraftOrange
                                            PrintQuality.DRAFT -> DraftGray
                                            PrintQuality.HIGH, PrintQuality.ADVANCED_PHOTO -> HighQualityPurple
                                            else -> EpsonBluePrimary
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (quality == PrintQuality.VIVID_DRAFT) Icons.Default.Bolt else Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = quality.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected && isVivid) VividDraftOrange else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isVivid) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(VividDraftOrange)
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "RECOMENDADO",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = quality.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Indicador de Seleção
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isVivid) VividDraftOrange else MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selecionado",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Comparador Visual e Métricas da Qualidade Selecionada
        QualityMetricsCard(selectedQuality = selectedQuality)
    }
}

@Composable
fun QualityMetricsCard(selectedQuality: PrintQuality) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Calibração: ${selectedQuality.dpiEquivalent}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = when (selectedQuality) {
                        PrintQuality.DRAFT -> "~6 seg/pág • -60% tinta"
                        PrintQuality.VIVID_DRAFT -> "~9 seg/pág • -45% tinta (Vívido)"
                        PrintQuality.STANDARD -> "~24 seg/pág • Consumo padrão"
                        PrintQuality.HIGH -> "~65 seg/pág • +40% tinta"
                        PrintQuality.ADVANCED_PHOTO -> "~140 seg/pág • 5760 DPI"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedQuality == PrintQuality.VIVID_DRAFT) VividDraftOrange else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = selectedQuality.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Barras de Rating: Velocidade e Economia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RatingBar(
                    label = "Velocidade",
                    rating = selectedQuality.speedRating,
                    icon = Icons.Default.Speed,
                    barColor = if (selectedQuality == PrintQuality.VIVID_DRAFT) VividDraftOrange else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                RatingBar(
                    label = "Economia Tinta",
                    rating = selectedQuality.inkEconomyRating,
                    icon = Icons.Default.WaterDrop,
                    barColor = Color(0xFF00A86B),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun RatingBar(
    label: String,
    rating: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$label ($rating/5)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            for (i in 1..5) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i <= rating) barColor else Color(0xFFE2E8F0))
                )
            }
        }
    }
}
