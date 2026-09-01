#!/data/data/com.termux/files/usr/bin/bash
#
# fix-build-jvmtarget.sh
#
# Corrige a falha de build do RetroPlayer:
#
#   ⛔ Inconsistent JVM Target Compatibility Between Java and Kotlin Tasks
#   Inconsistent JVM-target compatibility detected for tasks
#   'compileDebugJavaWithJavac' (11) and 'compileDebugKotlin' (21).
#
# Causa: o app/build.gradle.kts fixava sourceCompatibility/targetCompatibility
# em Java 11 (compileOptions), mas não existia bloco kotlinOptions — então o
# compilador Kotlin usava o JVM target do JDK do ambiente (21 no Termux),
# gerando o descompasso. A correção adiciona kotlinOptions.jvmTarget = "11"
# alinhado ao compileOptions.
#
# Também silencia dois avisos inofensivos que poluíam o log:
#   - "We recommend using a newer AGP to use compileSdk = 36"
#   - NullPointerException do KSP em ambiente headless (AWT-EventQueue)
#
# Uso:
#   bash fix-build-jvmtarget.sh /caminho/do/projeto/retroplayer
#
# Se rodar sem argumento, assume o diretório atual.

set -euo pipefail

TARGET_PROJECT="${1:-$(pwd)}"
APP_GRADLE="$TARGET_PROJECT/app/build.gradle.kts"
ROOT_PROPS="$TARGET_PROJECT/gradle.properties"

echo "=================================================="
echo " RetroPlayer — Fix: JVM Target Kotlin/Java"
echo "=================================================="
echo

# ---------------------------------------------------------
# 1. Validações
# ---------------------------------------------------------
if [ ! -f "$APP_GRADLE" ]; then
    echo "[ERRO] Não encontrei $APP_GRADLE"
    echo "       Rode este script a partir da raiz do projeto, ou informe o caminho:"
    echo "       bash fix-build-jvmtarget.sh /caminho/do/retroplayer"
    exit 1
fi

echo "[*] Projeto: $TARGET_PROJECT"
echo "[*] Arquivo alvo: $APP_GRADLE"
echo

# ---------------------------------------------------------
# 2. Corrigir o descompasso de JVM target
#    Adiciona kotlinOptions { jvmTarget = "11" } logo após compileOptions,
#    só se ainda não existir.
# ---------------------------------------------------------
if grep -q "kotlinOptions" "$APP_GRADLE"; then
    echo "[OK] kotlinOptions já existe em app/build.gradle.kts — nada a fazer aqui."
else
    echo "[*] Adicionando kotlinOptions { jvmTarget = \"11\" } alinhado ao compileOptions..."

    # Insere o bloco kotlinOptions logo depois do fechamento de compileOptions { ... }
    python3 - "$APP_GRADLE" << 'PYEOF'
import sys
import re

path = sys.argv[1]
with open(path, "r") as f:
    content = f.read()

pattern = re.compile(
    r"(compileOptions\s*\{\s*"
    r"sourceCompatibility\s*=\s*JavaVersion\.VERSION_11\s*"
    r"targetCompatibility\s*=\s*JavaVersion\.VERSION_11\s*"
    r"\})"
)

replacement = '\\1\n  kotlinOptions {\n    jvmTarget = "11"\n  }'

new_content, count = pattern.subn(replacement, content)

if count == 0:
    print("[AVISO] Não encontrei o bloco compileOptions no formato esperado.")
    print("        Adicione manualmente dentro do bloco android { ... }:")
    print()
    print('        kotlinOptions {')
    print('            jvmTarget = "11"')
    print('        }')
    sys.exit(1)

with open(path, "w") as f:
    f.write(new_content)

print(f"[OK] kotlinOptions inserido ({count} ocorrência corrigida).")
PYEOF
fi
echo

# ---------------------------------------------------------
# 3. Silenciar warning de compileSdk 36 acima do testado pelo AGP 8.7
# ---------------------------------------------------------
echo "[*] Conferindo supressão do warning compileSdk 36..."

if [ -f "$ROOT_PROPS" ] && grep -q "android.suppressUnsupportedCompileSdk" "$ROOT_PROPS"; then
    echo "[OK] android.suppressUnsupportedCompileSdk já está configurado."
else
    {
        echo ""
        echo "# AGP 8.7 foi testado oficialmente até compileSdk 35; compileSdk 36"
        echo "# funciona normalmente, isso apenas silencia o aviso no log de build."
        echo "android.suppressUnsupportedCompileSdk=36"
    } >> "$ROOT_PROPS"
    echo "[OK] Linha adicionada em $ROOT_PROPS"
fi
echo

# ---------------------------------------------------------
# 4. Evitar o ruído do KSP em ambiente headless (Termux sem display)
#    Adiciona -Djava.awt.headless=true aos jvmargs do daemon, se ainda
#    não estiver presente, e só nessa linha (sem duplicar).
# ---------------------------------------------------------
echo "[*] Conferindo flag headless nos jvmargs (evita erro AWT-EventQueue do KSP)..."

if [ -f "$ROOT_PROPS" ] && grep -q "org.gradle.jvmargs" "$ROOT_PROPS"; then
    if grep "org.gradle.jvmargs" "$ROOT_PROPS" | grep -q "java.awt.headless"; then
        echo "[OK] Flag headless já presente em org.gradle.jvmargs."
    else
        echo "[*] Adicionando -Djava.awt.headless=true à linha org.gradle.jvmargs existente..."
        sed -i 's#^org.gradle.jvmargs=\(.*\)#org.gradle.jvmargs=\1 -Djava.awt.headless=true#' "$ROOT_PROPS"
        echo "[OK] org.gradle.jvmargs atualizado:"
        grep "org.gradle.jvmargs" "$ROOT_PROPS"
    fi
else
    echo "[*] org.gradle.jvmargs não encontrado — adicionando linha nova..."
    echo "org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8 -Djava.awt.headless=true" >> "$ROOT_PROPS"
    echo "[OK] Linha adicionada."
fi
echo

# ---------------------------------------------------------
# 5. Parar o daemon do Gradle antigo (crucial — o log mostra que um
#    daemon "stopped" já não pôde ser reaproveitado; forçar um novo
#    daemon garante que ele nasce já com as configs corrigidas)
# ---------------------------------------------------------
echo "[*] Parando qualquer daemon do Gradle em execução para aplicar as novas configs..."

if [ -x "$TARGET_PROJECT/gradlew" ]; then
    (cd "$TARGET_PROJECT" && ./gradlew --stop) || true
    echo "[OK] Daemon(s) parado(s)."
else
    echo "[AVISO] gradlew não encontrado ou sem permissão de execução em $TARGET_PROJECT"
    echo "        Rode: chmod +x \"$TARGET_PROJECT/gradlew\""
fi
echo

# ---------------------------------------------------------
# 6. Resumo final
# ---------------------------------------------------------
echo "=================================================="
echo " Correção aplicada."
echo "=================================================="
echo
echo "O que foi mudado:"
echo "  1. app/build.gradle.kts — kotlinOptions.jvmTarget = \"11\" adicionado"
echo "     (alinhado ao compileOptions que já era Java 11), corrigindo o erro"
echo "     'Inconsistent JVM Target Compatibility'."
echo "  2. gradle.properties — android.suppressUnsupportedCompileSdk=36"
echo "     (silencia aviso, não afeta o build)."
echo "  3. gradle.properties — -Djava.awt.headless=true adicionado aos"
echo "     jvmargs (evita o NullPointerException do AWT-EventQueue do KSP"
echo "     em ambiente sem display, visto no log anterior)."
echo "  4. Daemon do Gradle reiniciado."
echo
echo "Rode a build novamente:"
echo
echo "    cd \"$TARGET_PROJECT\""
echo "    ./gradlew assembleDebug"
echo
