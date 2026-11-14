package com.example.myapplication.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File

internal class AppDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    File(context.noBackupFilesDir, "musica_app.db").absolutePath,
    null,
    2
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE usuarios(
                id INTEGER PRIMARY KEY,
                nome TEXT NOT NULL CHECK(length(nome) BETWEEN 3 AND 100),
                email TEXT NOT NULL UNIQUE CHECK(length(email) BETWEEN 3 AND 254),
                senha BLOB NOT NULL,
                salt BLOB NOT NULL,
                foto TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE reviews(
                id INTEGER PRIMARY KEY,
                usuario_id INTEGER NOT NULL,
                musica TEXT NOT NULL CHECK(length(musica) BETWEEN 1 AND 100),
                artista TEXT NOT NULL CHECK(length(artista) BETWEEN 1 AND 100),
                nota INTEGER NOT NULL CHECK(nota BETWEEN 1 AND 5),
                texto TEXT NOT NULL CHECK(length(texto) <= 500)
            )
            """.trimIndent()
        )
        criarTabelasDaBiblioteca(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Versão 1 -> 2: foto de perfil (recurso nativo) + tabelas de álbuns e músicas.
        // Usa ALTER TABLE / CREATE TABLE para NÃO apagar os usuários e reviews já cadastrados.
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE usuarios ADD COLUMN foto TEXT")
            criarTabelasDaBiblioteca(db)
        }
    }

    // CRUD 3 (albuns) e CRUD 4 (musicas): tabelas da biblioteca musical de cada usuário.
    private fun criarTabelasDaBiblioteca(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS albuns(
                id INTEGER PRIMARY KEY,
                usuario_id INTEGER NOT NULL,
                titulo TEXT NOT NULL CHECK(length(titulo) BETWEEN 1 AND 100),
                artista TEXT NOT NULL CHECK(length(artista) BETWEEN 1 AND 100),
                ano INTEGER NOT NULL CHECK(ano BETWEEN 1900 AND 2100),
                genero TEXT NOT NULL CHECK(length(genero) <= 50)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS musicas(
                id INTEGER PRIMARY KEY,
                usuario_id INTEGER NOT NULL,
                titulo TEXT NOT NULL CHECK(length(titulo) BETWEEN 1 AND 100),
                artista TEXT NOT NULL CHECK(length(artista) BETWEEN 1 AND 100),
                album TEXT NOT NULL CHECK(length(album) <= 100),
                genero TEXT NOT NULL CHECK(length(genero) <= 50)
            )
            """.trimIndent()
        )
    }
}
