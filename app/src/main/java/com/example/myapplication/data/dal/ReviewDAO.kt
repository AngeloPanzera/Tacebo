package com.example.myapplication.data.dal

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.myapplication.data.Review
import com.example.myapplication.data.local.AppDatabase

internal class ReviewDAO(context: Context) {
    private val appContext = context.applicationContext

    private val colunas = arrayOf("id", "usuario_id", "musica", "artista", "nota", "texto")

    private fun lerReview(c: Cursor) = Review(
        c.getLong(0),
        c.getLong(1),
        c.getString(2),
        c.getString(3),
        c.getInt(4),
        c.getString(5)
    )

    fun cadastrar(usuarioId: Long, musica: String, artista: String, nota: Int, texto: String) {
        AppDatabase(appContext).use { db ->
            val dados = ContentValues().apply {
                put("usuario_id", usuarioId)
                put("musica", musica)
                put("artista", artista)
                put("nota", nota)
                put("texto", texto)
            }
            db.writableDatabase.insertOrThrow("reviews", null, dados)
        }
    }

    fun listar(usuarioId: Long): List<Review> = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "reviews", colunas,
            "usuario_id = ?", arrayOf(usuarioId.toString()),
            null, null, "id DESC"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(lerReview(cursor))
            }
        }
    }

    fun buscarPorId(id: Long, usuarioId: Long): Review? = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "reviews", colunas,
            "id = ? AND usuario_id = ?", arrayOf(id.toString(), usuarioId.toString()),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) lerReview(cursor) else null
        }
    }

    fun alterar(id: Long, usuarioId: Long, musica: String, artista: String, nota: Int, texto: String) {
        AppDatabase(appContext).use { db ->
            val valores = ContentValues().apply {
                put("musica", musica)
                put("artista", artista)
                put("nota", nota)
                put("texto", texto)
            }
            val alterado = db.writableDatabase.update(
                "reviews", valores,
                "id = ? AND usuario_id = ?", arrayOf(id.toString(), usuarioId.toString())
            )
            require(alterado == 1) { "Review não encontrada. Atualize a lista" }
        }
    }

    fun excluir(id: Long, usuarioId: Long) {
        AppDatabase(appContext).use { db ->
            val excluido = db.writableDatabase.delete(
                "reviews",
                "id = ? AND usuario_id = ?", arrayOf(id.toString(), usuarioId.toString())
            )
            require(excluido == 1) { "Review não encontrada. Atualize a lista" }
        }
    }
}
