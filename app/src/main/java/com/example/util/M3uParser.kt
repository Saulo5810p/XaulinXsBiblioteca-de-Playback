package com.example.util

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

/**
 * Parser de arquivos .m3u / .m3u8 — formato de playlist usado por praticamente
 * todo player de música Android/desktop (Poweramp, VLC, foobar2000, etc).
 *
 * Formato básico de uma linha de entrada M3U:
 *   #EXTM3U                      (cabeçalho opcional)
 *   #EXTINF:duração,Artista - Título   (metadado opcional da entrada seguinte)
 *   /caminho/absoluto/ou/relativo/arquivo.mp3
 *
 * Linhas começando com '#' são comentários/metadados e são ignoradas para
 * fins de resolução de caminho — só usamos a duração/título como fallback
 * caso a faixa não seja encontrada no MediaStore por caminho.
 */
object M3uParser {

    private const val TAG = "M3uParser"

    data class M3uEntry(
        val rawPath: String,
        val title: String? = null
    )

    data class ParsedPlaylist(
        val name: String,
        val filePath: String,
        val entries: List<M3uEntry>
    )

    /**
     * Varre os diretórios padrão onde playlists M3U costumam ficar:
     * a raiz de Music, e a própria pasta Music (onde muitos players como
     * Poweramp/VLC salvam .m3u junto dos arquivos de áudio).
     */
    fun findM3uFiles(): List<File> {
        val results = mutableListOf<File>()
        val candidateDirs = listOf(
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), ""),
            File(Environment.getExternalStorageDirectory(), "Music"),
            File(Environment.getExternalStorageDirectory(), "Playlists"),
            Environment.getExternalStorageDirectory()
        )

        for (dir in candidateDirs.distinctBy { it.absolutePath }) {
            if (!dir.exists() || !dir.isDirectory) continue
            try {
                dir.listFiles { f ->
                    f.isFile && (f.name.endsWith(".m3u", ignoreCase = true) || f.name.endsWith(".m3u8", ignoreCase = true))
                }?.let { results.addAll(it) }
            } catch (e: SecurityException) {
                Log.w(TAG, "Sem permissão para ler ${dir.absolutePath}", e)
            }
        }
        return results.distinctBy { it.absolutePath }
    }

    /**
     * Faz o parse de um arquivo .m3u/.m3u8 em disco.
     * Nome da playlist = nome do arquivo sem extensão.
     */
    fun parse(file: File): ParsedPlaylist? {
        if (!file.exists() || !file.canRead()) return null

        val entries = mutableListOf<M3uEntry>()
        var pendingTitle: String? = null

        try {
            FileInputStream(file).use { fis ->
                InputStreamReader(fis, Charsets.UTF_8).buffered().useLines { lines ->
                    for (rawLine in lines) {
                        val line = rawLine.trim()
                        if (line.isEmpty()) continue

                        if (line.startsWith("#EXTINF:")) {
                            // #EXTINF:213,Artista - Título da Faixa
                            val commaIndex = line.indexOf(',')
                            pendingTitle = if (commaIndex != -1 && commaIndex < line.length - 1) {
                                line.substring(commaIndex + 1).trim()
                            } else null
                            continue
                        }

                        if (line.startsWith("#")) continue // outros comentários/metadados M3U

                        // Linha de caminho de arquivo (absoluto, relativo, ou file://)
                        val cleanPath = line.removePrefix("file://")
                        entries.add(M3uEntry(rawPath = cleanPath, title = pendingTitle))
                        pendingTitle = null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao ler playlist M3U: ${file.absolutePath}", e)
            return null
        }

        if (entries.isEmpty()) return null

        return ParsedPlaylist(
            name = file.nameWithoutExtension,
            filePath = file.absolutePath,
            entries = entries
        )
    }

    /**
     * Resolve o caminho de uma entrada M3U (que pode ser absoluto ou relativo
     * ao diretório do próprio arquivo .m3u) para um File real.
     */
    fun resolveEntryFile(entry: M3uEntry, m3uFile: File): File {
        val direct = File(entry.rawPath)
        if (direct.isAbsolute) return direct
        // Caminho relativo: resolve a partir da pasta onde está o .m3u
        return File(m3uFile.parentFile, entry.rawPath)
    }
}
