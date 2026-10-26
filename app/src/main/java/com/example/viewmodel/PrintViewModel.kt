package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.driver.AndroidPrintHelper
import com.example.driver.BookletPaginationEngine
import com.example.driver.BookletPlan
import com.example.driver.EpsonL3250Driver
import com.example.model.EpsonPrinterDevice
import com.example.model.PaperSize
import com.example.model.PaperType
import com.example.model.PrintJobRecord
import com.example.model.PrintLayoutMode
import com.example.model.PrintQuality
import com.example.pwa.LocalPrintWebServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class PrintUiState(
    val printer: EpsonPrinterDevice = EpsonPrinterDevice(),
    val discoveredPrinters: List<EpsonPrinterDevice> = listOf(
        EpsonPrinterDevice(
            id = "epson_l3250_main",
            modelName = "Epson EcoTank L3250 (Wi-Fi)",
            ipAddress = "192.168.1.150",
            signalStrengthDbm = -54,
            blackInkLevel = 88,
            cyanInkLevel = 76,
            magentaInkLevel = 92,
            yellowInkLevel = 68
        ),
        EpsonPrinterDevice(
            id = "epson_l4260_office",
            modelName = "Epson EcoTank L4260 (Duplex)",
            ipAddress = "192.168.1.162",
            signalStrengthDbm = -65,
            blackInkLevel = 45,
            cyanInkLevel = 60,
            magentaInkLevel = 55,
            yellowInkLevel = 70
        )
    ),
    val isScanningWifi: Boolean = false,
    val selectedQuality: PrintQuality = PrintQuality.VIVID_DRAFT,
    val selectedLayoutMode: PrintLayoutMode = PrintLayoutMode.STANDARD,
    val selectedPaperType: PaperType = PaperType.PLAIN,
    val selectedPaperSize: PaperSize = PaperSize.A4,
    val copies: Int = 1,
    val isColor: Boolean = true,
    val gutterMarginMm: Int = 5,
    val documentTitle: String = "Manual de Instruções e Relatório",
    val documentText: String = DEFAULT_SAMPLE_TEXT,
    val selectedImageUri: Uri? = null,
    val bookletPlan: BookletPlan = BookletPaginationEngine.calculateBooklet(8),
    val printJobsHistory: List<PrintJobRecord> = listOf(
        PrintJobRecord(
            id = "job-001",
            title = "Apresentação Trimestral (Livreto)",
            quality = PrintQuality.VIVID_DRAFT,
            layoutMode = PrintLayoutMode.BOOKLET,
            paperType = PaperType.PLAIN,
            paperSize = PaperSize.A4,
            totalPages = 8,
            totalSheets = 2,
            copies = 1
        ),
        PrintJobRecord(
            id = "job-002",
            title = "Gráficos de Vendas Rápidos",
            quality = PrintQuality.DRAFT,
            layoutMode = PrintLayoutMode.STANDARD,
            paperType = PaperType.PLAIN,
            paperSize = PaperSize.A4,
            totalPages = 2,
            totalSheets = 2,
            copies = 1
        )
    ),
    val isPrinting: Boolean = false,
    val printProgress: EpsonL3250Driver.DriverProgress? = null,
    val isPwaServerRunning: Boolean = false,
    val pwaServerIp: String = "192.168.1.100",
    val pwaServerPort: Int = 8080,
    val maintenanceStatus: String? = null,
    val showSuccessDialog: Boolean = false,
    val currentTab: Int = 0 // 0: Imprimir, 1: Livreto, 2: PWA Hub, 3: Manutenção Epson
)

private val DEFAULT_SAMPLE_TEXT = """
RELATÓRIO OPERACIONAL & GUIA DE PROCESSO
EcoPrint Hub • Impressão Wi-Fi Epson EcoTank L3250

1. INTRODUÇÃO
Este documento demonstra o algoritmo avançado de impressão da linha Epson L3250 em diante.
O sistema implementa o perfil exclusivo Rascunho Vívido (Vivid Draft), permitindo imprimir rapidamente com compensação dinâmica de densidade nas cores.

2. BENEFÍCIOS DO RASCUNHO VÍVIDO
- Redução de até 45% do tempo de impressão em comparação ao modo Normal.
- Saturação de cores vívidas sem o desbotamento característico do rascunho comum.
- Redução do desgaste da cabeça de impressão Micro Piezo.
- Ausência de marcas de bandagem (banding) graças ao micro-passo calibrado.

3. MODO LIVRETO (BOOKLET)
O modo livreto organiza as páginas em cadernos dobráveis de 4 páginas por folha física (2 na frente e 2 no verso).
Após imprimir, basta dobrar a folha ao meio na linha indicada e grampear no centro.

4. MONITORAMENTO ECOTANK
O reservatório de alta capacidade permite imprimir milhares de páginas com recarga rápida por garrafas anti-gotejamento.
Mantenha os níveis de Ciano, Magenta, Amarelo e Preto sempre acima da linha mínima recomendada.

5. VERIFICAÇÃO DE INJETORES
Caso note falhas ou linhas horizontais no documento, utilize a aba Diagnóstico para emitir o teste de injetores e realizar a limpeza do cabeçote.
""".trimIndent()

class PrintViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PrintUiState())
    val uiState: StateFlow<PrintUiState> = _uiState.asStateFlow()

    private val driver = EpsonL3250Driver(application)
    private val pwaServer = LocalPrintWebServer(application)

    init {
        // Inicializa o servidor PWA local para permitir conexão Wi-Fi de computadores e smartphones
        val localIp = pwaServer.getLocalIpAddress()
        _uiState.update { it.copy(pwaServerIp = localIp) }

        pwaServer.onPrintRequestReceived = { title, qualityStr, isBooklet, text ->
            viewModelScope.launch {
                val q = try { PrintQuality.valueOf(qualityStr) } catch (_: Exception) { PrintQuality.VIVID_DRAFT }
                val layout = if (isBooklet) PrintLayoutMode.BOOKLET else PrintLayoutMode.STANDARD
                _uiState.update {
                    it.copy(
                        documentTitle = title,
                        selectedQuality = q,
                        selectedLayoutMode = layout,
                        documentText = text
                    )
                }
                startPrintJob(useAndroidSpooler = false)
            }
        }
        pwaServer.startServer(8080)
        _uiState.update { it.copy(isPwaServerRunning = true) }

        recalculateBooklet()
    }

    override fun onCleared() {
        super.onCleared()
        pwaServer.stopServer()
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(currentTab = index) }
    }

    fun setQuality(quality: PrintQuality) {
        _uiState.update { it.copy(selectedQuality = quality) }
    }

    fun setLayoutMode(mode: PrintLayoutMode) {
        _uiState.update { it.copy(selectedLayoutMode = mode) }
        recalculateBooklet()
    }

    fun setPaperType(type: PaperType) {
        _uiState.update { it.copy(selectedPaperType = type) }
    }

    fun setPaperSize(size: PaperSize) {
        _uiState.update { it.copy(selectedPaperSize = size) }
    }

    fun setCopies(copies: Int) {
        _uiState.update { it.copy(copies = copies.coerceIn(1, 99)) }
    }

    fun setColorMode(isColor: Boolean) {
        _uiState.update { it.copy(isColor = isColor) }
    }

    fun setDocumentTitle(title: String) {
        _uiState.update { it.copy(documentTitle = title) }
    }

    fun setDocumentText(text: String) {
        _uiState.update { it.copy(documentText = text) }
        recalculateBooklet()
    }

    fun setSelectedImageUri(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    fun selectPrinter(printer: EpsonPrinterDevice) {
        _uiState.update { it.copy(printer = printer) }
    }

    fun updatePrinterIp(ip: String) {
        _uiState.update {
            it.copy(printer = it.printer.copy(ipAddress = ip))
        }
    }

    fun scanWifiPrinters() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanningWifi = true) }
            delay(1200) // Simulação de varredura mDNS / SSDP na sub-rede Wi-Fi
            _uiState.update {
                it.copy(
                    isScanningWifi = false,
                    discoveredPrinters = listOf(
                        EpsonPrinterDevice(
                            id = "epson_l3250_found",
                            modelName = "Epson EcoTank L3250 (Wi-Fi)",
                            ipAddress = "192.168.1.150",
                            signalStrengthDbm = -52,
                            blackInkLevel = 88,
                            cyanInkLevel = 76,
                            magentaInkLevel = 92,
                            yellowInkLevel = 68,
                            statusMessage = "Pronta na rede Wi-Fi"
                        ),
                        EpsonPrinterDevice(
                            id = "epson_l3251_found",
                            modelName = "Epson EcoTank L3251",
                            ipAddress = "192.168.1.177",
                            signalStrengthDbm = -68,
                            blackInkLevel = 65,
                            cyanInkLevel = 80,
                            magentaInkLevel = 70,
                            yellowInkLevel = 85,
                            statusMessage = "Pronta"
                        )
                    )
                )
            }
        }
    }

    private fun recalculateBooklet() {
        val lines = _uiState.value.documentText.lines()
        val estimatedPages = Math.max(1, (lines.size / 18) + 1)
        val plan = BookletPaginationEngine.calculateBooklet(estimatedPages)
        _uiState.update { it.copy(bookletPlan = plan) }
    }

    fun startPrintJob(useAndroidSpooler: Boolean) {
        val state = _uiState.value
        if (state.isPrinting) return

        if (useAndroidSpooler) {
            AndroidPrintHelper.printDocument(
                context = getApplication(),
                jobTitle = state.documentTitle,
                contentLines = state.documentText.lines(),
                quality = state.selectedQuality,
                layoutMode = state.selectedLayoutMode,
                paperSize = state.selectedPaperSize,
                isColor = state.isColor
            )
            recordJobInHistory("Spooler Android")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPrinting = true, printProgress = null) }

            // Monta dados raster em bytes
            val rawLinesBytes = state.documentText.toByteArray(Charsets.UTF_8)
            val escPrPayload = driver.buildEscPrCommandStream(
                quality = state.selectedQuality,
                paperType = state.selectedPaperType,
                paperSize = state.selectedPaperSize,
                isColor = state.isColor,
                documentTitle = state.documentTitle,
                pageData = rawLinesBytes
            )

            driver.printViaWifiSocket(
                ipAddress = state.printer.ipAddress,
                port = state.printer.port,
                payload = escPrPayload
            ) { progress ->
                _uiState.update { it.copy(printProgress = progress) }
            }

            delay(1000)
            _uiState.update {
                it.copy(
                    isPrinting = false,
                    showSuccessDialog = true
                )
            }
            recordJobInHistory("Wi-Fi Direto (Porta 9100)")
        }
    }

    private fun recordJobInHistory(channel: String) {
        val state = _uiState.value
        val newJob = PrintJobRecord(
            id = "job-${UUID.randomUUID().toString().take(6)}",
            title = state.documentTitle,
            quality = state.selectedQuality,
            layoutMode = state.selectedLayoutMode,
            paperType = state.selectedPaperType,
            paperSize = state.selectedPaperSize,
            totalPages = state.bookletPlan.originalPageCount,
            totalSheets = if (state.selectedLayoutMode == PrintLayoutMode.BOOKLET) state.bookletPlan.totalSheets else state.bookletPlan.originalPageCount,
            copies = state.copies,
            channel = channel
        )
        _uiState.update {
            it.copy(printJobsHistory = listOf(newJob) + it.printJobsHistory.take(15))
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(showSuccessDialog = false, printProgress = null) }
    }

    fun executeNozzleCheck() {
        viewModelScope.launch {
            _uiState.update { it.copy(maintenanceStatus = "Enviando comando de Verificação de Injetores para a Epson L3250...") }
            val cmd = driver.createNozzleCheckCommand()
            driver.printViaWifiSocket(_uiState.value.printer.ipAddress, _uiState.value.printer.port, cmd) {
                // progresso
            }
            delay(1500)
            _uiState.update { it.copy(maintenanceStatus = "Página de teste de injetores (CMYK) impressa com sucesso! Verifique se há traços ausentes no padrão.") }
        }
    }

    fun executeHeadCleaning() {
        viewModelScope.launch {
            _uiState.update { it.copy(maintenanceStatus = "Iniciando ciclo de Limpeza de Cabeçote na Epson L3250 (aproximadamente 2 minutos)...") }
            val cmd = driver.createHeadCleaningCommand()
            driver.printViaWifiSocket(_uiState.value.printer.ipAddress, _uiState.value.printer.port, cmd) {
                // progresso
            }
            delay(2000)
            _uiState.update { it.copy(maintenanceStatus = "Ciclo de limpeza de cabeçote concluído! Recomendamos imprimir o teste de injetores para conferir o resultado.") }
        }
    }

    fun clearMaintenanceStatus() {
        _uiState.update { it.copy(maintenanceStatus = null) }
    }

    fun togglePwaServer() {
        val currentlyRunning = _uiState.value.isPwaServerRunning
        if (currentlyRunning) {
            pwaServer.stopServer()
            _uiState.update { it.copy(isPwaServerRunning = false) }
        } else {
            pwaServer.startServer(8080)
            _uiState.update { it.copy(isPwaServerRunning = true) }
        }
    }

    fun loadPresetSample(sampleType: String) {
        when (sampleType) {
            "vivid_draft" -> {
                _uiState.update {
                    it.copy(
                        selectedQuality = PrintQuality.VIVID_DRAFT,
                        documentTitle = "Relatório Executivo com Gráficos",
                        documentText = """
RELATÓRIO SEMANAL COM GRÁFICOS E TABELAS
Modo de Alta Produtividade: Rascunho Vívido (Epson EcoTank)

DESTAQUES DO PERÍODO:
- Produção Acelerada: +34% de documentos expedidos.
- Consumo de Tinta: Economia média de 42% no reservatório preto e colorido.
- Velocidade da Cabeça Micro Piezo: Impressão bidirecional sem resíduo de banding.
- Avaliação de Qualidade: Textos nítidos em fontes serifadas e gráficos com cores vivas.

DISTRIBUIÇÃO DE METAS:
[■■■■■■■■■□] 90% Concluído - Projeto Alfa
[■■■■■■■□□□] 70% Concluído - Projeto Beta
[■■■■■■■■■■] 100% Concluído - Suporte Wi-Fi Epson L3250
                        """.trimIndent()
                    )
                }
            }
            "booklet" -> {
                _uiState.update {
                    it.copy(
                        selectedLayoutMode = PrintLayoutMode.BOOKLET,
                        documentTitle = "Manual de Bolso - Guia Prático",
                        documentText = """
MANUAL DE BOLSO: BROCHURA DOBRADA
Edição em Modo Livreto (Booklet)

PÁGINA 1: BEM-VINDO
Este folheto foi formatado para montagem em livreto.
As folhas são impressas de modo que a dobra central forme a brochura perfeita.

PÁGINA 2: INSTRUÇÕES DE MONTAGEM
1. Dobre as folhas ao meio na linha tracejada.
2. Posicione o grampo no vinco central.
3. Seu livreto de bolso está pronto para distribuição!

PÁGINA 3: DICAS DE PAPEL
Para capas mais firmes, utilize papel Sulfite Especial 90g ou Papel Matte 120g no alimentador traseiro da Epson L3250.

PÁGINA 4: CONTRACAPA & CRÉDITOS
Produzido com EcoPrint Hub • Driver Wi-Fi Epson EcoTank
                        """.trimIndent()
                    )
                }
                recalculateBooklet()
            }
        }
    }
}
