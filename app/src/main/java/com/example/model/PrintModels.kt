package com.example.model

/**
 * Qualidades de impressão suportadas, incluindo o exclusivo Rascunho Vívido.
 */
enum class PrintQuality(
    val title: String,
    val subtitle: String,
    val description: String,
    val speedRating: Int, // 1 a 5 (5 mais rápido)
    val inkEconomyRating: Int, // 1 a 5 (5 mais econômico)
    val dpiEquivalent: String,
    val colorBoost: Float
) {
    DRAFT(
        title = "Rascunho Rápido",
        subtitle = "Ultra rápido e econômico",
        description = "Ideal para leituras rápidas, conferências de texto e testes. Economiza até 60% de tinta.",
        speedRating = 5,
        inkEconomyRating = 5,
        dpiEquivalent = "360 DPI",
        colorBoost = 0.85f
    ),
    VIVID_DRAFT(
        title = "Rascunho Vívido",
        subtitle = "Rápido com cores reforçadas",
        description = "Rascunho inteligente calibrado para a Epson L3250: mantém alta velocidade de impressão mas compensa a saturação e nitidez do texto sem deixar marcas de bandagem.",
        speedRating = 4,
        inkEconomyRating = 4,
        dpiEquivalent = "360x720 DPI",
        colorBoost = 1.25f
    ),
    STANDARD(
        title = "Normal / Padrão",
        subtitle = "Equilibrado para o dia a dia",
        description = "Resolução balanceada para trabalhos escolares, relatórios de escritório e documentos com fotos.",
        speedRating = 3,
        inkEconomyRating = 3,
        dpiEquivalent = "720 DPI",
        colorBoost = 1.0f
    ),
    HIGH(
        title = "Alta Qualidade",
        subtitle = "Máxima nitidez e contraste",
        description = "Micro-pontos precisos da cabeça Micro Piezo da Epson. Ideal para gráficos detalhados e apresentações.",
        speedRating = 2,
        inkEconomyRating = 2,
        dpiEquivalent = "1440 DPI",
        colorBoost = 1.15f
    ),
    ADVANCED_PHOTO(
        title = "Avançada / Foto Pro",
        subtitle = "Resolução fotográfica 5760 DPI",
        description = "Calibração para papéis especiais e fotográficos. Cores vibrantes com transições de tom suaves.",
        speedRating = 1,
        inkEconomyRating = 1,
        dpiEquivalent = "5760 DPI otimizado",
        colorBoost = 1.35f
    )
}

/**
 * Modos de layout de página, incluindo o Modo Livreto (Booklet).
 */
enum class PrintLayoutMode(
    val title: String,
    val description: String
) {
    STANDARD(
        title = "Padrão (1 pág/folha)",
        description = "Impressão contínua normal, página por página."
    ),
    BOOKLET(
        title = "Modo Livreto (Brochura)",
        description = "Imprime 2 páginas por face da folha, ordenadas para dobrar ao meio formando um folheto encadernado."
    ),
    TWO_UP(
        title = "2 Páginas por Folha",
        description = "Economia de papel: 2 páginas reduzidas lado a lado em cada folha."
    ),
    FOUR_UP(
        title = "4 Páginas por Folha",
        description = "Compactação máxima: 4 páginas organizadas em grade por folha."
    )
}

/**
 * Tipos de papel compatíveis com a linha Epson EcoTank L3250 em diante.
 */
enum class PaperType(val displayName: String, val epsonMediaCode: String) {
    PLAIN("Papel Comum / Sulfite (75g-90g)", "PLAIN"),
    BRIGHT_WHITE("Papel Sulfite Especial Epson", "BRIGHT_WHITE"),
    MATTE("Papel Fosco / Matte", "MATTE"),
    GLOSSY_PHOTO("Papel Fotográfico Brilhante (Glossy)", "GLOSSY"),
    PREMIUM_GLOSSY("Papel Fotográfico Ultra Glossy", "PREMIUM_GLOSSY"),
    ENVELOPE("Envelope", "ENVELOPE")
}

/**
 * Tamanhos de papel padrão.
 */
enum class PaperSize(val displayName: String, val widthMm: Int, val heightMm: Int) {
    A4("A4 (210 x 297 mm)", 210, 297),
    LETTER("Carta / Letter (216 x 279 mm)", 216, 279),
    A5("A5 (148 x 210 mm)", 148, 210),
    PHOTO_10X15("10 x 15 cm (4 x 6 polegadas)", 100, 150),
    PHOTO_13X18("13 x 18 cm (5 x 7 polegadas)", 130, 180)
}

/**
 * Configuração de encadernação para Livreto.
 */
data class BookletConfig(
    val bindingSide: String = "Esquerda", // Esquerda ou Superior (Calendário)
    val gutterMarginMm: Int = 5,
    val includeCoverSheet: Boolean = true
)

/**
 * Informações e status da impressora Epson L3250 e similares.
 */
data class EpsonPrinterDevice(
    val id: String = "epson_l3250_default",
    val modelName: String = "Epson EcoTank L3250 Series",
    val ipAddress: String = "192.168.1.150",
    val port: Int = 9100,
    val httpPort: Int = 80,
    val ippPort: Int = 631,
    val macAddress: String = "AC:18:26:8F:42:3B",
    val wifiSsid: String = "MinhaRede_Wi-Fi",
    val signalStrengthDbm: Int = -58,
    val isOnline: Boolean = true,
    val statusMessage: String = "Pronta para impressão",
    // Níveis de tinta da EcoTank (0 a 100%)
    val blackInkLevel: Int = 88,
    val cyanInkLevel: Int = 76,
    val magentaInkLevel: Int = 92,
    val yellowInkLevel: Int = 68,
    val firmwareVersion: String = "20.54.FA12N3",
    val totalPagesPrinted: Int = 1420
)

/**
 * Registro de trabalho de impressão no histórico.
 */
data class PrintJobRecord(
    val id: String,
    val title: String,
    val quality: PrintQuality,
    val layoutMode: PrintLayoutMode,
    val paperType: PaperType,
    val paperSize: PaperSize,
    val totalPages: Int,
    val totalSheets: Int,
    val copies: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Concluído",
    val channel: String = "Wi-Fi Direto"
)
