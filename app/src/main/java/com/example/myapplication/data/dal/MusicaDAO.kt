package com.example.myapplication.data.dal

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.myapplication.data.Musica
import com.example.myapplication.data.local.AppDatabase

internal class MusicaDAO(context: Context) {
    private val appContext = context.applicationContext

    private val colunas = arrayOf("id", "usuario_id", "titulo", "artista", "album", "genero")

    private fun lerMusica(c: Cursor) = Musica(
        c.getLong(0),
        c.getLong(1),
        c.getString(2),
        c.getString(3),
        c.getString(4),
        c.getString(5)
    )

    fun cadastrar(usuarioId: Long, titulo: String, artista: String, album: String, genero: String) {
        AppDatabase(appContext).use { db ->
            val dados = ContentValues().apply {
                put("usuario_id", usuarioId)
                put("titulo", titulo)
                put("artista", artista)
                put("album", album)
                put("genero", genero)
            }
            db.writableDatabase.insertOrThrow("musicas", null, dados)
        }
    }

    fun listar(usuarioId: Long): List<Musica> = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "musicas", colunas,
            "usuario_id = ?", arrayOf(usuarioId.toString()),
            null, null, "titulo COLLATE NOCASE, id"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(lerMusica(cursor))
            }
        }
    }

    fun buscarPorId(id: Long, usuarioId: Long): Musica? = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "musicas", colunas,
            "id = ? AND usuario_id = ?", arrayOf(id.toString(), usuarioId.toString()),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) lerMusica(cursor) else null
        }
    }

    // Usado na tela de busca (API) para não salvar a mesma música duas vezes.
    fun existe(usuarioId: Long, titulo: String, artista: String): Boolean =
        AppDatabase(appContext).use { db ->
            db.readableDatabase.query(
                "musicas", arrayOf("id"),
                "usuario_id = ? AND titulo = ? COLLATE NOCASE AND artista = ? COLLATE NOCASE",
                arrayOf(usuarioId.toString(), titulo, artista),
                null, null, null, "1"
            ).use { cursor -> cursor.moveToFirst() }
        }

    fun alterar(
        id: Long, usuarioId: Long, titulo: String,
        artista: String, album: String, genero: String
    ) {
        AppDatabase(appContext).use { db ->
            val valores = ContentValues().apply {
                put("titulo", titulo)
                put("artista", artista)
                put("album", album)
                put("genero", genero)
            }
            val alterado = db.writableDatabase.update(
                "musicas", valores,
                "id = ? AND usuario_id = ?", arrayOf(id.toString(), usuarioId.toString())
            )
            require(alterado == 1) { "Música não encontrada. Atualize a lista" }
        }
    }

    fun excluir(id: Long, usuarioId: Long) {
        AppDatabase(appContext).use { db ->
            val excluido = db.writableDatabase.delete(
                "musicas",
                "id = ? AND usuario_id = ?", arrayOf(id.toString(), usuarioId.toString())
            )
            require(excluido == 1) { "Música não encontrada. Atualize a lista" }
        }
    }
}
