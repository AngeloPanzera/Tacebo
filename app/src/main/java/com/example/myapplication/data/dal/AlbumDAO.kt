package com.example.myapplication.data.dal

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.myapplication.data.Album
import com.example.myapplication.data.local.AppDatabase

internal class AlbumDAO(context: Context) {
    private val appContext = context.applicationContext

    private val colunas = arrayOf("id", "usuario_id", "titulo", "artista", "ano", "genero")

    private fun lerAlbum(c: Cursor) = Album(
        c.getLong(0),
        c.getLong(1),
        c.getString(2),
        c.getString(3),
        c.getInt(4),
        c.getString(5)
    )

    fun cadastrar(usuarioId: Long, titulo: String, artista: String, ano: Int, genero: String) {
        AppDatabase(appContext).use { db ->
            val dados = ContentValues().apply {
                put("usuario_id", usuarioId)
                put("titulo", titulo)
                put("artista", artista)
                put("ano", ano)
                put("genero", genero)
            }
            db.writableDatabase.insertOrThrow("albuns", null, dados)
        }
    }

    fun listar(usuarioId: Long): List<Album> = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "albuns", colunas,
            "usuario_id = ?", arrayOf(usuarioId.toString()),
            null, null, "titulo COLLATE NOCASE, id"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(lerAlbum(cursor))
            }
        }
    }

    fun buscarPorId(id: Long, usuarioId: Long): Album? = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "albuns", colunas,
            "id = ? AND usuario_id = ?", arrayOf(id.toString(), usuarioId.toString()),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) lerAlbum(cursor) else null
        }
    }

    fun alterar(
        id: Long, usuarioId: Long, titulo: String,
        artista: String, ano: Int, genero: String
    ) {
        AppDatabase(appContext).use { db ->
            val valores = ContentValues().apply {
                put("titulo", titulo)
                put("artista", artista)
                put("ano", ano)
                put("genero", genero)
            }
            val alterado = db.writableDatabase.update(
                "albuns", valores,
                "id = ? AND usuario_id = ?", arrayOf(id.toString(), usuarioId.toString())
            )
            require(alterado == 1) { "Álbum não encontrado. Atualize a lista" }
        }
    }

    fun excluir(id: Long, usuarioId: Long) {
        AppDatabase(appContext).use { db ->
            val excluido = db.writableDatabase.delete(
                "albuns",
                "id = ? AND usuario_id = ?", arrayOf(id.toString(), usuarioId.toString())
            )
            require(excluido == 1) { "Álbum não encontrado. Atualize a lista" }
        }
    }
}
