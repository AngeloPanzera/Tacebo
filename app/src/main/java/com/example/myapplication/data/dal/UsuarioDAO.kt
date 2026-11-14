package com.example.myapplication.data.dal

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.myapplication.data.Usuario
import com.example.myapplication.data.local.AppDatabase

internal class SenhaSalva(val hash: ByteArray, val salt: ByteArray, val usuarioId: Long)

internal class UsuarioDAO(context: Context) {
    private val appContext = context.applicationContext

    private val colunas = arrayOf("id", "nome", "email", "foto")

    private fun lerUsuario(c: Cursor) = Usuario(
        c.getLong(0),
        c.getString(1),
        c.getString(2),
        c.getString(3)
    )

    fun cadastrar(nome: String, email: String, senha: ByteArray, salt: ByteArray) {
        AppDatabase(appContext).use { db ->
            val dados = ContentValues().apply {
                put("nome", nome)
                put("email", email)
                put("senha", senha)
                put("salt", salt)
            }
            db.writableDatabase.insertOrThrow(
                "usuarios",
                null,
                dados
            )
        }
    }

    fun buscarSenha(email: String): SenhaSalva? {
        return AppDatabase(appContext).use { db ->
            db.readableDatabase.query(
                "usuarios",
                arrayOf("senha", "salt", "id"),
                "email = ?",
                arrayOf(email),
                null, null, null
            ).use { usuario ->
                if (usuario.moveToFirst()) SenhaSalva(
                    usuario.getBlob(0),
                    usuario.getBlob(1),
                    usuario.getLong(2)
                )
                else null
            }
        }
    }

    fun listar(): List<Usuario> = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "usuarios", colunas,
            null, null, null, null,
            "nome COLLATE NOCASE, id"
        ).use { usuario ->
            buildList {
                while (usuario.moveToNext()) add(lerUsuario(usuario))
            }
        }
    }

    fun buscarPorId(id: Long): Usuario? = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "usuarios", colunas,
            "id = ?", arrayOf(id.toString()),
            null, null, null
        ).use { usuario ->
            if (usuario.moveToFirst()) lerUsuario(usuario) else null
        }
    }

    fun alterar(
        id: Long, nome: String, hash: ByteArray? = null,
        salt: ByteArray? = null
    ) {
        AppDatabase(appContext).use { db ->
            val valores = ContentValues().apply {
                put("nome", nome)
                if (hash != null) put("senha", hash)
                if (salt != null) put("salt", salt)
            }
            val alterado = db.writableDatabase.update(
                "usuarios",
                valores, "id = ?", arrayOf(id.toString())
            )
            require(alterado == 1) { "Usuário não encontrado. Atualize a lista" }
        }
    }

    // Guarda (ou remove, se null) o caminho da foto de perfil do usuário.
    fun alterarFoto(id: Long, foto: String?) {
        AppDatabase(appContext).use { db ->
            val valores = ContentValues().apply {
                if (foto == null) putNull("foto") else put("foto", foto)
            }
            val alterado = db.writableDatabase.update(
                "usuarios",
                valores, "id = ?", arrayOf(id.toString())
            )
            require(alterado == 1) { "Usuário não encontrado. Atualize a lista" }
        }
    }

    fun excluir(id: Long, usuarioAtualId: Long) {
        require(usuarioAtualId > 0) { "Faça login para continuar" }
        require(id != usuarioAtualId) { "Você não pode excluir o seu usuário" }
        AppDatabase(appContext).use { db ->
            val banco = db.writableDatabase
            // Apaga antes os dados desse usuário, para não sobrar registro "órfão".
            banco.delete("reviews", "usuario_id = ?", arrayOf(id.toString()))
            banco.delete("albuns", "usuario_id = ?", arrayOf(id.toString()))
            banco.delete("musicas", "usuario_id = ?", arrayOf(id.toString()))
            val excluido = banco.delete(
                "usuarios", "id = ? AND id <> ?",
                arrayOf(id.toString(), usuarioAtualId.toString())
            )
            require(excluido == 1) { "Usuário não encontrado. Atualize a lista" }
        }
    }
}
