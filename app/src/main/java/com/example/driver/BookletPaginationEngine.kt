package com.example.driver

import kotlin.math.ceil

/**
 * Representa uma face de folha física no modo livreto.
 */
data class BookletSheetFace(
    val sheetNumber: Int,
    val faceName: String, // "Frente" ou "Verso"
    val leftPageNumber: Int?, // null = página em branco
    val rightPageNumber: Int?,
    val isFront: Boolean
)

/**
 * Estrutura completa do livreto calculado para impressão.
 */
data class BookletPlan(
    val originalPageCount: Int,
    val paddedPageCount: Int,
    val totalSheets: Int,
    val frontFaces: List<BookletSheetFace>,
    val backFaces: List<BookletSheetFace>,
    val foldingInstructions: List<String>
)

object BookletPaginationEngine {

    /**
     * Calcula a imposição de páginas para o Modo Livreto (Booklet).
     */
    fun calculateBooklet(pageCount: Int): BookletPlan {
        val safeCount = if (pageCount <= 0) 1 else pageCount
        // Livreto precisa de múltiplo de 4 páginas
        val paddedCount = ceil(safeCount / 4.0).toInt() * 4
        val totalSheets = paddedCount / 4

        val fronts = mutableListOf<BookletSheetFace>()
        val backs = mutableListOf<BookletSheetFace>()

        for (sheet in 1..totalSheets) {
            // Frente: Esquerda = total - 2*(s-1), Direita = 2*s - 1
            val frontLeft = paddedCount - 2 * (sheet - 1)
            val frontRight = 2 * sheet - 1

            fronts.add(
                BookletSheetFace(
                    sheetNumber = sheet,
                    faceName = "Folha $sheet - Frente",
                    leftPageNumber = if (frontLeft <= safeCount) frontLeft else null,
                    rightPageNumber = if (frontRight <= safeCount) frontRight else null,
                    isFront = true
                )
            )

            // Verso: Esquerda = 2*s, Direita = total - 2*(s-1) - 1
            val backLeft = 2 * sheet
            val backRight = paddedCount - 2 * (sheet - 1) - 1

            backs.add(
                BookletSheetFace(
                    sheetNumber = sheet,
                    faceName = "Folha $sheet - Verso",
                    leftPageNumber = if (backLeft <= safeCount) backLeft else null,
                    rightPageNumber = if (backRight <= safeCount) backRight else null,
                    isFront = false
                )
            )
        }

        val instructions = listOf(
            "Etapa 1: Imprima todas as ${fronts.size} páginas da FRENTE primeiro.",
            "Etapa 2: Retire as folhas da bandeja de saída sem alterar a ordem das folhas.",
            "Etapa 3: Gire o maço 180° e recoloque no alimentador traseiro da Epson L3250.",
            "Etapa 4: Imprima todas as páginas do VERSO.",
            "Etapa 5: Dobre as folhas ao meio na linha de vinco central e grampeie no meio."
        )

        return BookletPlan(
            originalPageCount = safeCount,
            paddedPageCount = paddedCount,
            totalSheets = totalSheets,
            frontFaces = fronts,
            backFaces = backs,
            foldingInstructions = instructions
        )
    }
}
