package com.example

import com.example.driver.BookletPaginationEngine
import com.example.model.PrintQuality
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun bookletCalculation_8Pages_producesCorrectImposition() {
        val plan = BookletPaginationEngine.calculateBooklet(8)
        assertEquals(8, plan.originalPageCount)
        assertEquals(8, plan.paddedPageCount)
        assertEquals(2, plan.totalSheets)
        assertEquals(2, plan.frontFaces.size)
        assertEquals(2, plan.backFaces.size)

        // Folha 1 Frente: [8, 1]
        val sheet1Front = plan.frontFaces[0]
        assertEquals(8, sheet1Front.leftPageNumber)
        assertEquals(1, sheet1Front.rightPageNumber)

        // Folha 1 Verso: [2, 7]
        val sheet1Back = plan.backFaces[0]
        assertEquals(2, sheet1Back.leftPageNumber)
        assertEquals(7, sheet1Back.rightPageNumber)

        // Folha 2 Frente: [6, 3]
        val sheet2Front = plan.frontFaces[1]
        assertEquals(6, sheet2Front.leftPageNumber)
        assertEquals(3, sheet2Front.rightPageNumber)

        // Folha 2 Verso: [4, 5]
        val sheet2Back = plan.backFaces[1]
        assertEquals(4, sheet2Back.leftPageNumber)
        assertEquals(5, sheet2Back.rightPageNumber)
    }

    @Test
    fun bookletCalculation_oddPagesPaddedToMultipleOfFour() {
        // 5 páginas devem ser estendidas para 8
        val plan = BookletPaginationEngine.calculateBooklet(5)
        assertEquals(5, plan.originalPageCount)
        assertEquals(8, plan.paddedPageCount)
        assertEquals(2, plan.totalSheets)
    }

    @Test
    fun vividDraftQuality_hasEnhancedSpeedAndEconomy() {
        val vivid = PrintQuality.VIVID_DRAFT
        assertTrue("Rascunho Vívido deve ser rápido", vivid.speedRating >= 4)
        assertTrue("Rascunho Vívido deve ter boost de cor", vivid.colorBoost > 1.0f)
    }
}
