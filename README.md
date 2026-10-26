# EcoPrint Hub • Impressão Wi-Fi Epson EcoTank (L3250+)

Aplicativo Android e PWA integrado para impressão via rede Wi-Fi com driver nativo para impressoras Epson EcoTank (L3250 em diante), suporte a **Rascunho Vívido (Vivid Draft)** e **Modo Livreto (Booklet)**.

---

## 🚀 Geração Automática de APK no GitHub

Este repositório está configurado com **GitHub Actions** (`.github/workflows/build-apk.yml`).

### Como Funciona:
1. Ao fazer **Push** para qualquer branch do GitHub (ou enviar via menu de sincronização do AI Studio), a automação inicia imediatamente.
2. O fluxo prepara o ambiente Android SDK, Java 17, compila o projeto com Gradle e gera o arquivo `.apk`.
3. O APK gerado fica disponível para download direto na aba **Actions** do seu repositório GitHub:
   - Acesse seu repositório no GitHub.
   - Clique na aba **Actions**.
   - Clique no workflow mais recente (`Build Android APK`).
   - Na seção **Artifacts** (no final da página da execução), baixe o arquivo `EcoPrint-Hub-APK.zip`.
4. Extraia o ZIP e instale o arquivo `.apk` diretamente em qualquer celular ou tablet Android!

---

## ✨ Recursos do Aplicativo

- **Driver Wi-Fi Epson EcoTank**: Envio de comandos ESC/P-R pela porta RAW 9100 / JetDirect, leitura de níveis de tinta CMYK e comandos de limpeza/teste de injetores.
- **Rascunho Vívido (Vivid Draft)**: Alta velocidade com compensação de saturação de cores e nitidez de texto.
- **Modo Livreto (Booklet)**: Cálculo de imposição de cadernos múltiplos de 4 páginas e guia visual para o alimentador traseiro da Epson L3250.
- **Servidor Web PWA Embutido**: Permite que computadores e celulares na mesma rede acessem via navegador e enviem trabalhos de impressão diretamente para a impressora.
