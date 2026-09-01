#!/data/data/com.termux/files/usr/bin/bash
#
# setup-retroplayer.sh
#
# Prepara o projeto RetroPlayer recém-colocado numa pasta nova do Termux
# pra compilar: reaproveita o gradlew/gradle-wrapper.jar de um projeto
# já funcional (LatinIME, Launcher3, etc — mesmo padrão de sempre),
# concede as permissões de execução, e confere a config global do aapt2.
#
# Uso:
#   bash setup-retroplayer.sh /caminho/do/projeto-fonte-que-ja-builda /caminho/do/retroplayer
#
# Exemplo real:
#   bash setup-retroplayer.sh ~/AOSP-LatinIME-main ~/retroplayer-novo
#
# Se rodar sem argumentos, o script tenta autodetectar um projeto-fonte
# procurando por gradlew existentes na pasta home do Termux.

set -euo pipefail

SOURCE_PROJECT="${1:-}"
TARGET_PROJECT="${2:-$(pwd)}"

echo "=================================================="
echo " RetroPlayer — Setup de Build no Termux"
echo "=================================================="
echo

# ---------------------------------------------------------
# 1. Autodetecção do projeto-fonte, se não informado
# ---------------------------------------------------------
if [ -z "$SOURCE_PROJECT" ]; then
    echo "[*] Nenhum projeto-fonte informado — procurando um gradlew existente em \$HOME..."
    CANDIDATE=$(find "$HOME" -maxdepth 3 -iname "gradlew" -not -path "*/retroplayer*" 2>/dev/null | head -n 1 || true)

    if [ -z "$CANDIDATE" ]; then
        echo "[ERRO] Nenhum projeto com gradlew foi encontrado automaticamente em \$HOME."
        echo "       Rode novamente informando o caminho manualmente:"
        echo "       bash setup-retroplayer.sh ~/SeuProjetoQueJaBuilda ~/retroplayer-novo"
        exit 1
    fi

    SOURCE_PROJECT="$(dirname "$CANDIDATE")"
    echo "[OK] Projeto-fonte detectado automaticamente: $SOURCE_PROJECT"
fi

echo "[*] Projeto-fonte (de onde vem o wrapper): $SOURCE_PROJECT"
echo "[*] Projeto-alvo (RetroPlayer novo):        $TARGET_PROJECT"
echo

# ---------------------------------------------------------
# 2. Validações básicas
# ---------------------------------------------------------
if [ ! -d "$SOURCE_PROJECT" ]; then
    echo "[ERRO] Projeto-fonte não existe: $SOURCE_PROJECT"
    exit 1
fi

if [ ! -f "$SOURCE_PROJECT/gradlew" ] || [ ! -d "$SOURCE_PROJECT/gradle/wrapper" ]; then
    echo "[ERRO] O projeto-fonte não tem um gradlew/gradle-wrapper válido:"
    echo "       Esperado: $SOURCE_PROJECT/gradlew"
    echo "       Esperado: $SOURCE_PROJECT/gradle/wrapper/gradle-wrapper.jar"
    exit 1
fi

if [ ! -d "$TARGET_PROJECT" ]; then
    echo "[ERRO] Projeto-alvo (RetroPlayer) não existe: $TARGET_PROJECT"
    echo "       Extraia o zip do RetroPlayer antes de rodar este script."
    exit 1
fi

if [ ! -f "$TARGET_PROJECT/settings.gradle.kts" ] && [ ! -f "$TARGET_PROJECT/settings.gradle" ]; then
    echo "[ERRO] $TARGET_PROJECT não parece ser a raiz de um projeto Gradle"
    echo "       (settings.gradle.kts não encontrado)."
    exit 1
fi

# ---------------------------------------------------------
# 3. Copiar gradlew + gradlew.bat + gradle/wrapper/* do projeto-fonte
# ---------------------------------------------------------
echo "[*] Copiando gradlew e gradle/wrapper/ do projeto-fonte..."

cp "$SOURCE_PROJECT/gradlew" "$TARGET_PROJECT/gradlew"
[ -f "$SOURCE_PROJECT/gradlew.bat" ] && cp "$SOURCE_PROJECT/gradlew.bat" "$TARGET_PROJECT/gradlew.bat"

mkdir -p "$TARGET_PROJECT/gradle/wrapper"
cp "$SOURCE_PROJECT/gradle/wrapper/gradle-wrapper.jar" "$TARGET_PROJECT/gradle/wrapper/gradle-wrapper.jar"
cp "$SOURCE_PROJECT/gradle/wrapper/gradle-wrapper.properties" "$TARGET_PROJECT/gradle/wrapper/gradle-wrapper.properties"

echo "[OK] Wrapper copiado."
echo

# ---------------------------------------------------------
# 4. Conferir/ajustar a versão do Gradle no wrapper.properties
#    (o projeto foi preparado para Gradle 9.6.1 + AGP 8.7)
# ---------------------------------------------------------
WRAPPER_PROPS="$TARGET_PROJECT/gradle/wrapper/gradle-wrapper.properties"
EXPECTED_GRADLE="gradle-9.6.1-bin.zip"

if grep -q "$EXPECTED_GRADLE" "$WRAPPER_PROPS"; then
    echo "[OK] gradle-wrapper.properties já aponta para $EXPECTED_GRADLE"
else
    CURRENT=$(grep "distributionUrl" "$WRAPPER_PROPS" || true)
    echo "[AVISO] O wrapper copiado não é o Gradle 9.6.1:"
    echo "        $CURRENT"
    echo "        Ajustando para a versão correta usada por este projeto..."
    sed -i "s#distributionUrl=.*#distributionUrl=https\\\\://services.gradle.org/distributions/${EXPECTED_GRADLE}#" "$WRAPPER_PROPS"
    echo "[OK] gradle-wrapper.properties atualizado para $EXPECTED_GRADLE"
fi
echo

# ---------------------------------------------------------
# 5. chmod — permissões de execução
# ---------------------------------------------------------
echo "[*] Aplicando permissões de execução (chmod)..."

chmod +x "$TARGET_PROJECT/gradlew"
[ -f "$TARGET_PROJECT/gradlew.bat" ] && chmod +x "$TARGET_PROJECT/gradlew.bat"

# Termux às vezes precisa disso também pro daemon do Gradle escrever cache
chmod -R u+rwX "$TARGET_PROJECT"

echo "[OK] Permissões aplicadas em gradlew e no diretório do projeto."
echo

# ---------------------------------------------------------
# 6. Conferir a config global do aapt2 nativo (~/.gradle/gradle.properties)
#    — não sobrescreve nada, só avisa se estiver faltando
# ---------------------------------------------------------
GLOBAL_GRADLE_PROPS="$HOME/.gradle/gradle.properties"
AAPT2_LINE_PATTERN="android.aapt2FromMavenOverride"

echo "[*] Conferindo aapt2 nativo em $GLOBAL_GRADLE_PROPS ..."

mkdir -p "$HOME/.gradle"

if [ -f "$GLOBAL_GRADLE_PROPS" ] && grep -q "$AAPT2_LINE_PATTERN" "$GLOBAL_GRADLE_PROPS"; then
    echo "[OK] aapt2FromMavenOverride já está configurado globalmente:"
    grep "$AAPT2_LINE_PATTERN" "$GLOBAL_GRADLE_PROPS"
else
    AAPT2_BIN=$(command -v aapt2 || true)
    if [ -z "$AAPT2_BIN" ]; then
        echo "[AVISO] aapt2 não encontrado no PATH do Termux."
        echo "        Instale com: pkg install aapt"
        echo "        Depois rode este script de novo, ou adicione manualmente em"
        echo "        $GLOBAL_GRADLE_PROPS a linha:"
        echo "        android.aapt2FromMavenOverride=/data/data/com.termux/files/usr/bin/aapt2"
    else
        echo "[*] aapt2 nativo encontrado em: $AAPT2_BIN"
        echo "android.aapt2FromMavenOverride=$AAPT2_BIN" >> "$GLOBAL_GRADLE_PROPS"
        echo "[OK] Linha adicionada em $GLOBAL_GRADLE_PROPS"
    fi
fi
echo

# ---------------------------------------------------------
# 7. Configurações básicas de gradle.properties GLOBAL
#    que projetos deste tipo (Compose + Kotlin + AGP 8.7) precisam
#    — só adiciona o que estiver faltando, nunca duplica
# ---------------------------------------------------------
echo "[*] Conferindo flags básicas em $GLOBAL_GRADLE_PROPS ..."

declare -A REQUIRED_PROPS=(
    ["org.gradle.jvmargs"]="org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8"
    ["org.gradle.parallel"]="org.gradle.parallel=true"
    ["org.gradle.caching"]="org.gradle.caching=true"
    ["android.useAndroidX"]="android.useAndroidX=true"
    ["android.nonTransitiveRClass"]="android.nonTransitiveRClass=true"
    ["kotlin.code.style"]="kotlin.code.style=official"
)

touch "$GLOBAL_GRADLE_PROPS"

for key in "${!REQUIRED_PROPS[@]}"; do
    if grep -q "^${key}=" "$GLOBAL_GRADLE_PROPS" 2>/dev/null; then
        echo "    [já existe] $key"
    else
        echo "${REQUIRED_PROPS[$key]}" >> "$GLOBAL_GRADLE_PROPS"
        echo "    [adicionado] ${REQUIRED_PROPS[$key]}"
    fi
done
echo

# ---------------------------------------------------------
# 8. Resumo final
# ---------------------------------------------------------
echo "=================================================="
echo " Setup concluído."
echo "=================================================="
echo
echo "Projeto pronto em: $TARGET_PROJECT"
echo
echo "Para compilar, rode a partir da pasta do projeto:"
echo
echo "    cd \"$TARGET_PROJECT\""
echo "    ./gradlew assembleDebug"
echo
echo "Para o build assinado de release (precisa das env vars"
echo "KEYSTORE_PATH / KEYSTORE_PASSWORD / KEY_ALIAS / KEY_PASSWORD):"
echo
echo "    ./gradlew assembleRelease"
echo
echo "Se o daemon do Gradle já estiver rodando com config antiga, rode:"
echo "    ./gradlew --stop"
echo "antes do primeiro build para forçar reinicialização com as configs novas."
echo
