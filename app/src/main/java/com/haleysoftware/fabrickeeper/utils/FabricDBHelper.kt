package com.haleysoftware.fabrickeeper.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.haleysoftware.fabrickeeper.utils.FabricContract.FabricEntry

/**
 * Created by Michael Haley on 9/3/17.
 */


class FabricDBHelper internal constructor(context: Context) : SQLiteOpenHelper(context,
        FabricEntry.DATABASE_NAME, null, DATABASE_VERSION) {

    val CREATE_DATABASE = "CREATE TABLE if not exists ${FabricEntry.TABLE_NAME} (" +
            "${FabricEntry.ROW_ID} INTEGER PRIMARY KEY autoincrement, " +
            "${FabricEntry.COLUMN_DESCRIPTION} TEXT NOT NULL, " +
            "${FabricEntry.COLUMN_KEYWORDS} TEXT, " +
            "${FabricEntry.COLUMN_LOCATION} TEXT, " +
            "${FabricEntry.COLUMN_PROJECTS} TEXT, " +
            "${FabricEntry.COLUMN_YARDS} REAL, " +
            "${FabricEntry.COLUMN_WIDTH} REAL, " +
            "${FabricEntry.COLUMN_DESIGN_NAME} TEXT, " +
            "${FabricEntry.COLUMN_DESIGNER} TEXT, " +
            "${FabricEntry.COLUMN_PURCHASED} TEXT, " +
            "${FabricEntry.COLUMN_IMAGE} TEXT" +
            ")"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_DATABASE)
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {

    }

    companion object {
        /**
         * V1 - First
         * V2 - Changed yards and width from INTEGER to REAL
         */
        private val DATABASE_VERSION = 2

    }
}