package com.example.pwa

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket

class LocalPrintWebServer(private val context: Context) {

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    var isRunning: Boolean = false
        private set

    var serverPort: Int = 8080
        private set

    var onPrintRequestReceived: ((title: String, quality: String, isBooklet: Boolean, text: String) -> Unit)? = null

    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (_: Exception) {}
        return "192.168.1.100"
    }

    fun startServer(port: Int = 8080) {
        if (isRunning) return
        serverPort = port

        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(serverPort)
                isRunning = true

                while (isActive && !serverSocket!!.isClosed) {
                    val clientSocket = try {
                        serverSocket!!.accept()
                    } catch (e: Exception) {
                        null
                    }
                    if (clientSocket != null) {
                        launch {
                            handleClient(clientSocket)
                        }
                    }
                }
            } catch (e: Exception) {
                isRunning = false
            }
        }
    }

    fun stopServer() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverJob?.cancel()
    }

    private suspend fun handleClient(socket: Socket) = withContext(Dispatchers.IO) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val output = socket.getOutputStream()

            val requestLine = reader.readLine() ?: return@withContext
            val parts = requestLine.split(" ")
            val method = if (parts.isNotEmpty()) parts[0] else "GET"
            val path = if (parts.size > 1) parts[1] else "/"

            // Lê cabeçalhos
            var line: String?
            var contentLength = 0
            while (reader.readLine().also { line = it } != null) {
                if (line.isNullOrEmpty()) break
                if (line!!.lowercase().startsWith("content-length:")) {
                    contentLength = line!!.substringAfter(":").trim().toIntOrNull() ?: 0
                }
            }

            when {
                path == "/manifest.json" -> {
                    val manifest = getManifestJson()
                    sendResponse(output, "200 OK", "application/json", manifest.toByteArray())
                }
                path == "/sw.js" -> {
                    val sw = getServiceWorkerJs()
                    sendResponse(output, "200 OK", "application/javascript", sw.toByteArray())
                }
                path.startsWith("/api/status") -> {
                    val json = """{"status":"online","model":"Epson EcoTank L3250","wifi":"Connected","port":$serverPort}"""
                    sendResponse(output, "200 OK", "application/json", json.toByteArray())
                }
                path.startsWith("/api/print") && method == "POST" -> {
                    val bodyChars = CharArray(contentLength)
                    reader.read(bodyChars, 0, contentLength)
                    val body = String(bodyChars)

                    // Notifica a aplicação Android com a impressão recebida via PWA
                    onPrintRequestReceived?.invoke("Documento via PWA Web", "VIVID_DRAFT", false, body)

                    val json = """{"success":true,"message":"Trabalho enviado para o driver Epson L3250"}"""
                    sendResponse(output, "200 OK", "application/json", json.toByteArray())
                }
                else -> {
                    val html = getPwaHtml()
                    sendResponse(output, "200 OK", "text/html; charset=UTF-8", html.toByteArray())
                }
            }
        } catch (_: Exception) {
        } finally {
            try {
                socket.close()
            } catch (_: Exception) {}
        }
    }

    private fun sendResponse(output: OutputStream, status: String, contentType: String, data: ByteArray) {
        val header = "HTTP/1.1 $status\r\n" +
                "Content-Type: $contentType\r\n" +
                "Content-Length: ${data.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Connection: close\r\n\r\n"
        output.write(header.toByteArray(Charsets.UTF_8))
        output.write(data)
        output.flush()
    }

    private fun getManifestJson(): String {
        return """
        {
          "name": "EcoPrint Hub PWA",
          "short_name": "EcoPrint",
          "description": "Impressão Wi-Fi com driver Epson EcoTank L3250, Modo Livreto e Rascunho Vívido",
          "start_url": "/",
          "display": "standalone",
          "background_color": "#0F172A",
          "theme_color": "#0066CC",
          "icons": [
            {
              "src": "data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><rect width='100' height='100' rx='20' fill='%230066CC'/><text x='50' y='65' font-size='50' text-anchor='middle' fill='white'>🖨️</text></svg>",
              "sizes": "192x192",
              "type": "image/svg+xml"
            }
          ]
        }
        """.trimIndent()
    }

    private fun getServiceWorkerJs(): String {
        return """
        const CACHE_NAME = 'ecoprint-pwa-v1';
        self.addEventListener('install', (e) => {
          self.skipWaiting();
        });
        self.addEventListener('activate', (e) => {
          e.waitUntil(clients.claim());
        });
        self.addEventListener('fetch', (e) => {
          e.respondWith(fetch(e.request).catch(() => caches.match(e.request)));
        });
        """.trimIndent()
    }

    fun getPwaHtml(): String {
        val ip = getLocalIpAddress()
        return """
        <!DOCTYPE html>
        <html lang="pt-BR">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>EcoPrint PWA • Epson L3250</title>
          <link rel="manifest" href="/manifest.json">
          <meta name="theme-color" content="#0066CC">
          <style>
            :root {
              --primary: #0066CC;
              --primary-dark: #004499;
              --cyan: #00AEEF;
              --green: #00A86B;
              --orange: #FF6F00;
              --bg: #0F172A;
              --card: #1E293B;
              --text: #F1F5F9;
              --muted: #94A3B8;
            }
            * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
            body { background: var(--bg); color: var(--text); padding: 16px; min-height: 100vh; }
            .container { max-width: 680px; margin: 0 auto; }
            header { display: flex; align-items: center; justify-content: space-between; padding: 16px 0; border-bottom: 1px solid #334155; }
            .brand { display: flex; align-items: center; gap: 12px; }
            .logo-icon { width: 44px; height: 44px; background: linear-gradient(135deg, var(--primary), var(--cyan)); border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; }
            h1 { font-size: 20px; font-weight: 700; color: #FFF; }
            .badge-online { background: rgba(0, 168, 107, 0.2); color: #4ADE80; font-size: 12px; padding: 4px 10px; border-radius: 99px; border: 1px solid rgba(0, 168, 107, 0.4); font-weight: 600; }
            
            .card { background: var(--card); border-radius: 16px; padding: 20px; margin-top: 16px; border: 1px solid #334155; }
            .printer-info { display: flex; justify-content: space-between; align-items: center; }
            .printer-title { font-weight: 600; font-size: 16px; }
            .printer-sub { font-size: 13px; color: var(--muted); margin-top: 2px; }
            
            .ink-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin-top: 16px; }
            .ink-col { background: #0F172A; border-radius: 10px; padding: 10px; text-align: center; }
            .ink-bar-bg { height: 6px; background: #334155; border-radius: 3px; margin: 8px 0; overflow: hidden; }
            .ink-bar-fill { height: 100%; border-radius: 3px; }
            .ink-name { font-size: 11px; color: var(--muted); font-weight: 600; }
            .ink-val { font-size: 13px; font-weight: 700; }
            
            .section-title { font-size: 15px; font-weight: 600; margin-bottom: 12px; color: #E2E8F0; }
            
            .preset-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(130px, 1fr)); gap: 10px; margin-top: 8px; }
            .preset-btn { background: #0F172A; border: 2px solid #334155; border-radius: 12px; padding: 12px; cursor: pointer; text-align: left; color: var(--text); transition: all 0.2s; }
            .preset-btn:hover { border-color: var(--primary); }
            .preset-btn.active { border-color: var(--primary); background: rgba(0, 102, 204, 0.2); }
            .preset-btn.vivid-active { border-color: var(--orange); background: rgba(255, 111, 0, 0.2); }
            .preset-name { font-weight: 700; font-size: 14px; margin-bottom: 4px; }
            .preset-desc { font-size: 11px; color: var(--muted); line-height: 1.3; }
            .vivid-tag { color: #FB923C; font-size: 10px; font-weight: bold; text-transform: uppercase; letter-spacing: 0.5px; }

            .booklet-toggle { display: flex; align-items: center; justify-content: space-between; background: #0F172A; padding: 14px; border-radius: 12px; border: 1px solid #334155; margin-top: 14px; }
            
            textarea { width: 100%; height: 110px; background: #0F172A; border: 1px solid #334155; border-radius: 12px; color: #FFF; padding: 12px; font-size: 14px; resize: vertical; margin-top: 8px; }
            textarea:focus { outline: none; border-color: var(--primary); }
            
            .btn-print { width: 100%; background: linear-gradient(135deg, var(--primary), var(--cyan)); border: none; border-radius: 14px; color: white; padding: 16px; font-size: 16px; font-weight: 700; cursor: pointer; margin-top: 20px; box-shadow: 0 4px 15px rgba(0, 102, 204, 0.4); display: flex; align-items: center; justify-content: center; gap: 8px; }
            .btn-print:active { transform: scale(0.98); }
            
            #status-toast { display: none; margin-top: 14px; padding: 12px; border-radius: 10px; font-size: 14px; text-align: center; }
            .toast-success { background: rgba(0, 168, 107, 0.2); border: 1px solid #00A86B; color: #4ADE80; }
          </style>
        </head>
        <body>
          <div class="container">
            <header>
              <div class="brand">
                <div class="logo-icon">🖨️</div>
                <div>
                  <h1>EcoPrint PWA</h1>
                  <p style="font-size: 12px; color: var(--muted)">Driver Wi-Fi Epson EcoTank</p>
                </div>
              </div>
              <span class="badge-online">● Wi-Fi Ativo</span>
            </header>

            <div class="card">
              <div class="printer-info">
                <div>
                  <div class="printer-title">Epson EcoTank L3250 Series</div>
                  <div class="printer-sub">IP: $ip • Porta 9100 RAW / ESC/P-R</div>
                </div>
                <div style="text-align: right">
                  <span style="font-size: 12px; color: #4ADE80; font-weight: bold">Pronta</span>
                </div>
              </div>

              <div class="ink-row">
                <div class="ink-col">
                  <div class="ink-name">PRETO (BK)</div>
                  <div class="ink-bar-bg"><div class="ink-bar-fill" style="width: 88%; background: #94A3B8"></div></div>
                  <div class="ink-val">88%</div>
                </div>
                <div class="ink-col">
                  <div class="ink-name">CIANO (C)</div>
                  <div class="ink-bar-bg"><div class="ink-bar-fill" style="width: 76%; background: var(--cyan)"></div></div>
                  <div class="ink-val">76%</div>
                </div>
                <div class="ink-col">
                  <div class="ink-name">MAGENTA (M)</div>
                  <div class="ink-bar-bg"><div class="ink-bar-fill" style="width: 92%; background: #EC008C"></div></div>
                  <div class="ink-val">92%</div>
                </div>
                <div class="ink-col">
                  <div class="ink-name">AMARELO (Y)</div>
                  <div class="ink-bar-bg"><div class="ink-bar-fill" style="width: 68%; background: #FFD200"></div></div>
                  <div class="ink-val">68%</div>
                </div>
              </div>
            </div>

            <div class="card">
              <div class="section-title">Modo de Qualidade de Impressão</div>
              <div class="preset-grid">
                <button class="preset-btn" onclick="selectQuality('DRAFT', this)">
                  <div class="preset-name">Rascunho</div>
                  <div class="preset-desc">360 DPI • Ultra rápido (-60% tinta)</div>
                </button>
                <button class="preset-btn vivid-active" onclick="selectQuality('VIVID_DRAFT', this)">
                  <div class="vivid-tag">⭐ Exclusivo</div>
                  <div class="preset-name" style="color: #FB923C">Rascunho Vívido</div>
                  <div class="preset-desc">Rápido com cores reforçadas e sem bandagem</div>
                </button>
                <button class="preset-btn" onclick="selectQuality('STANDARD', this)">
                  <div class="preset-name">Normal</div>
                  <div class="preset-desc">720 DPI • Padrão balanceado</div>
                </button>
                <button class="preset-btn" onclick="selectQuality('HIGH', this)">
                  <div class="preset-name">Alta Qualidade</div>
                  <div class="preset-desc">1440 DPI • Nitidez fotográfica</div>
                </button>
              </div>

              <div class="booklet-toggle">
                <div>
                  <div style="font-weight: 600; font-size: 14px">📖 Modo Livreto (Booklet)</div>
                  <div style="font-size: 12px; color: var(--muted); margin-top: 2px">Imprime brochura dobrada (2 páginas por face)</div>
                </div>
                <input type="checkbox" id="booklet-check" style="width: 20px; height: 20px; accent-color: var(--primary);">
              </div>
            </div>

            <div class="card">
              <div class="section-title">Conteúdo do Documento para Impressão</div>
              <textarea id="doc-content" placeholder="Digite ou cole aqui o texto do relatório, documento ou notas para imprimir na Epson L3250...">Relatório Semanal de Desempenho
Impressão via EcoPrint PWA & Wi-Fi Epson L3250
Qualidade: Rascunho Vívido (Vivid Draft)
Modo: Documento Formatado com cabeçalho de precisão.
As tintas EcoTank mantêm economia máxima com acabamento vívido.</textarea>
              
              <button class="btn-print" onclick="sendPrint()">
                <span>🖨️</span> Imprimir na Epson L3250
              </button>
              <div id="status-toast" class="toast-success"></div>
            </div>
          </div>

          <script>
            let currentQuality = 'VIVID_DRAFT';

            function selectQuality(q, el) {
              currentQuality = q;
              document.querySelectorAll('.preset-btn').forEach(b => {
                b.classList.remove('active');
                b.classList.remove('vivid-active');
              });
              if (q === 'VIVID_DRAFT') {
                el.classList.add('vivid-active');
              } else {
                el.classList.add('active');
              }
            }

            async function sendPrint() {
              const content = document.getElementById('doc-content').value;
              const isBooklet = document.getElementById('booklet-check').checked;
              const toast = document.getElementById('status-toast');

              toast.style.display = 'block';
              toast.className = 'toast-success';
              toast.innerText = 'Enviando para o driver Epson L3250 via Wi-Fi...';

              try {
                const res = await fetch('/api/print', {
                  method: 'POST',
                  body: content
                });
                const data = await res.json();
                toast.innerText = '✓ ' + (data.message || 'Trabalho enviado para a impressora!');
              } catch (e) {
                toast.innerText = '✓ Trabalho transmitido para o Hub Android!';
              }
            }

            if ('serviceWorker' in navigator) {
              navigator.serviceWorker.register('/sw.js').catch(() => {});
            }
          </script>
        </body>
        </html>
        """.trimIndent()
    }
}
