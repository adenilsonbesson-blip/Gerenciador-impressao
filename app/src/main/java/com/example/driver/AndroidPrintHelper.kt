package com.example.driver

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.example.model.PaperSize
import com.example.model.PrintLayoutMode
import com.example.model.PrintQuality
import java.io.FileOutputStream
import java.io.IOException

object AndroidPrintHelper {

    fun printDocument(
        context: Context,
        jobTitle: String,
        contentLines: List<String>,
        quality: PrintQuality,
        layoutMode: PrintLayoutMode,
        paperSize: PaperSize,
        isColor: Boolean
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val printAdapter = object : PrintDocumentAdapter() {
            private var pdfDocument: PdfDocument? = null
            private var totalCalculatedPages = 1

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                totalCalculatedPages = if (layoutMode == PrintLayoutMode.BOOKLET) {
                    val rawPages = Math.max(1, (contentLines.size / 15) + 1)
                    val bookletPlan = BookletPaginationEngine.calculateBooklet(rawPages)
                    bookletPlan.totalSheets * 2 // Frentes e Versos
                } else {
                    Math.max(1, (contentLines.size / 22) + 1)
                }

                val info = PrintDocumentInfo.Builder("$jobTitle.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(totalCalculatedPages)
                    .build()

                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                pdfDocument = PdfDocument()

                val pageWidth = 595 // A4 standard pt (72 dpi)
                val pageHeight = 842

                for (i in 1..totalCalculatedPages) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onWriteCancelled()
                        pdfDocument?.close()
                        pdfDocument = null
                        return
                    }

                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i).create()
                    val page = pdfDocument?.startPage(pageInfo) ?: continue
                    val canvas: Canvas = page.canvas

                    drawDocumentPage(canvas, pageWidth, pageHeight, i, totalCalculatedPages, jobTitle, contentLines, quality, layoutMode)
                    pdfDocument?.finishPage(page)
                }

                try {
                    val outputStream = FileOutputStream(destination?.fileDescriptor)
                    pdfDocument?.writeTo(outputStream)
                    outputStream.close()
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: IOException) {
                    callback?.onWriteFailed(e.message)
                } finally {
                    pdfDocument?.close()
                    pdfDocument = null
                }
            }
        }

        val attributes = PrintAttributes.Builder()
            .setColorMode(if (isColor) PrintAttributes.COLOR_MODE_COLOR else PrintAttributes.COLOR_MODE_MONOCHROME)
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .build()

        printManager.print(jobTitle, printAdapter, attributes)
    }

    private fun drawDocumentPage(
        canvas: Canvas,
        width: Int,
        height: Int,
        pageNumber: Int,
        totalPages: Int,
        title: String,
        lines: List<String>,
        quality: PrintQuality,
        layoutMode: PrintLayoutMode
    ) {
        // Fundo da folha branca
        val bgPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Moldura suave
        val borderPaint = Paint().apply {
            color = Color.rgb(230, 235, 245)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(24f, 24f, width - 24f, height - 24f, borderPaint)

        // Cabeçalho da página
        val headerPaint = Paint().apply {
            color = Color.rgb(0, 102, 204)
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(title, 40f, 60f, headerPaint)

        // Tag da qualidade selecionada
        val badgeBgPaint = Paint().apply {
            color = when (quality) {
                PrintQuality.VIVID_DRAFT -> Color.rgb(255, 243, 224)
                PrintQuality.DRAFT -> Color.rgb(240, 244, 248)
                PrintQuality.HIGH -> Color.rgb(243, 229, 245)
                else -> Color.rgb(227, 242, 253)
            }
            style = Paint.Style.FILL
        }
        val badgeRect = RectF(width - 220f, 42f, width - 40f, 70f)
        canvas.drawRoundRect(badgeRect, 6f, 6f, badgeBgPaint)

        val badgeTextPaint = Paint().apply {
            color = when (quality) {
                PrintQuality.VIVID_DRAFT -> Color.rgb(230, 81, 0)
                PrintQuality.DRAFT -> Color.rgb(96, 125, 139)
                PrintQuality.HIGH -> Color.rgb(123, 31, 162)
                else -> Color.rgb(21, 101, 192)
            }
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("${quality.title} • Epson L3250", width - 210f, 62f, badgeTextPaint)

        // Se for Modo Livreto, desenha vinco central e indicação de dobra
        if (layoutMode == PrintLayoutMode.BOOKLET) {
            val foldPaint = Paint().apply {
                color = Color.rgb(200, 200, 200)
                strokeWidth = 1.5f
                pathEffect = android.graphics.DashPathEffect(floatArrayOf(6f, 6f), 0f)
            }
            canvas.drawLine(width / 2f, 30f, width / 2f, height - 30f, foldPaint)

            val bookletTagPaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 9f
                isAntiAlias = true
            }
            canvas.drawText("LINHA DE DOBRA DO LIVRETO", (width / 2f) - 60f, 40f, bookletTagPaint)
        }

        // Conteúdo
        val textPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 12f
            isAntiAlias = true
        }

        var y = 100f
        val startLineIdx = (pageNumber - 1) * 20
        for (i in startLineIdx until Math.min(lines.size, startLineIdx + 20)) {
            val line = lines[i]
            canvas.drawText(line, 44f, y, textPaint)
            y += 24f
        }

        // Rodapé
        val footerPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 10f
            isAntiAlias = true
        }
        canvas.drawText("EcoPrint Hub Wi-Fi • Página $pageNumber de $totalPages • Driver Epson L3250+", 44f, height - 40f, footerPaint)
    }
}
