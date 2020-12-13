package xyz.heart.sms.shared.data.model

import android.database.Cursor
import xyz.heart.sms.encryption.EncryptionUtils


interface DatabaseTable {

    fun getCreateStatement(): String

    fun getTableName(): String

    fun getIndexStatements(): Array<String>

    fun fillFromCursor(cursor: Cursor)

    fun encrypt(utils: _root_ide_package_.xyz.heart.sms.encryption.EncryptionUtils)

    fun decrypt(utils: _root_ide_package_.xyz.heart.sms.encryption.EncryptionUtils)

}