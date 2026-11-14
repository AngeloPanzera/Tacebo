package com.example.myapplication.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

// Funções de apoio ao recurso nativo (câmera e galeria): guardam a foto de perfil
// dentro da pasta privada do app e devolvem o caminho do arquivo.
object ImagemUtil {
    private const val LADO_MAXIMO = 600
    private const val PASTA_FOTOS = "fotos"

    // Foto escolhida na galeria: chega como Uri, então lemos, reduzimos e salvamos.
    fun salvarFotoDaGaleria(context: Context, uri: Uri, usuarioId: Long): String {
        val resolver = context.contentResolver

        // 1) Lê só as dimensões da imagem (sem carregá-la inteira na memória)
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, medidas) }
        require(medidas.outWidth > 0 && medidas.outHeight > 0) {
            "Não foi possível ler a imagem escolhida"
        }

        // 2) Lê a imagem já reduzida (inSampleSize) para não estourar a memória
        val opcoes = BitmapFactory.Options().apply {
            inSampleSize = calcularReducao(medidas.outWidth, medidas.outHeight)
        }
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opcoes)
        }
        requireNotNull(bitmap) { "Não foi possível ler a imagem escolhida" }

        // 3) Gira a foto caso o celular a tenha gravado "deitada" (informação EXIF)
        return salvarBitmap(context, girar(bitmap, lerRotacao(context, uri)), usuarioId)
    }

    // Foto tirada na câmera: já chega como Bitmap, só falta salvar no arquivo.
    fun salvarBitmap(context: Context, bitmap: Bitmap, usuarioId: Long): String {
        val pasta = File(context.filesDir, PASTA_FOTOS)
        pasta.mkdirs()
        // O nome muda a cada foto nova, assim a tela sempre carrega a imagem atualizada
        val arquivo = File(pasta, "usuario_${usuarioId}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(arquivo).use { saida ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, saida)
        }
        return arquivo.absolutePath
    }

    fun apagar(caminho: String) {
        try {
            File(caminho).delete()
        } catch (e: Exception) {
            // Se não conseguir apagar o arquivo antigo, não atrapalha o resto do app
        }
    }

    private fun calcularReducao(largura: Int, altura: Int): Int {
        var reducao = 1
        while (largura / (reducao * 2) >= LADO_MAXIMO && altura / (reducao * 2) >= LADO_MAXIMO) {
            reducao *= 2
        }
        return reducao
    }

    private fun lerRotacao(context: Context, uri: Uri): Float {
        return try {
            context.contentResolver.openInputStream(uri)?.use { entrada ->
                val orientacao = ExifInterface(entrada).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                when (orientacao) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        } catch (e: Exception) {
            0f
        }
    }

    private fun girar(bitmap: Bitmap, graus: Float): Bitmap {
        if (graus == 0f) return bitmap
        val matriz = Matrix().apply { postRotate(graus) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
    }
}
