package com.haleysoftware.fabrickeeper.utils

import android.net.Uri

/**
 * Created by Michael Haley on 9/3/17.
 */


object FabricContract {

    val AUTHORITY = "com.haleysoftware.fabrickeeper"

    private val BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY)

    val PATH_FABRIC = "fabric"


    object FabricEntry {

        val CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FABRIC).build()


        val DATABASE_NAME = "fabricstore.db"

        val TABLE_NAME = "fabric"


        val ROW_ID = "_id"

        val COLUMN_DESCRIPTION = "description"

        val COLUMN_KEYWORDS = "keywords"

        val COLUMN_LOCATION = "location"

        val COLUMN_PROJECTS = "projects"

        val COLUMN_YARDS = "yards"

        val COLUMN_WIDTH = "width"

        val COLUMN_DESIGN_NAME = "name"

        val COLUMN_DESIGNER = "designer"

        val COLUMN_PURCHASED = "purchased"

        val COLUMN_IMAGE = "image"

    }
}
