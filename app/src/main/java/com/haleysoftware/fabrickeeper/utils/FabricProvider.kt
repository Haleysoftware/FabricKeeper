package com.haleysoftware.fabrickeeper.utils

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.haleysoftware.fabrickeeper.utils.FabricContract.FabricEntry

/**
 * Created by Michael Haley on 9/3/17.
 */

class FabricProvider : ContentProvider() {

    //This is the holder for the Database helper class. It lets you access the Database.
    private var dBHelper: FabricDBHelper? = null

    //The constant for Uri paths that want all rows in the Database.
    private val FABRIC = 100

    //The constant for Uri paths that only want one row in the Database. The # will be the row.
    private val FABRIC_WITH_ID = 101

    private val URI_MATCHER = buildUriHelper()

    //Helper Function to build a Uri matcher that will help identify what Uri was used
    private fun buildUriHelper(): UriMatcher {
        //Build a blank UriMatcher to start off with
        var uriMacher = UriMatcher(UriMatcher.NO_MATCH)
        //Add the Uri for all rows
        uriMacher.addURI(FabricContract.AUTHORITY, FabricContract.PATH_FABRIC, FABRIC)
        //Add the Uri for a single row
        uriMacher.addURI(FabricContract.AUTHORITY, FabricContract.PATH_FABRIC + "/#",
                FABRIC_WITH_ID)

        return uriMacher
    }

    override fun onCreate(): Boolean {
        dBHelper = FabricDBHelper(context)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {

        val dB = dBHelper!!.readableDatabase

        val match = URI_MATCHER.match(uri)

        val cursor: Cursor

        when (match) {
            FABRIC -> {
                cursor = dB!!.query(FabricEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder)
            }
            FABRIC_WITH_ID -> {
                val id = uri.pathSegments[1]

                val mSelection = "_id=?"
                val mSelectionArgs = arrayOf(id)

                cursor = dB!!.query(FabricEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder)
            }
        //The Uri is not supported
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }
        //Notify that there was a change
        cursor?.setNotificationUri(context.contentResolver, uri)
        //Return the cursor
        return cursor
    }

    override fun getType(uri: Uri): String? {
        val match = URI_MATCHER.match(uri)

        when (match) {
            FABRIC -> {
                return "vnd.android.cursor.dir" + "/" + FabricContract.AUTHORITY + "/" +
                        FabricContract.PATH_FABRIC
            }
            FABRIC_WITH_ID -> {
                return "vnd.android.cursor.item" + "/" + FabricContract.AUTHORITY + "/" +
                        FabricContract.PATH_FABRIC
            }
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        //May need to check that empty values are not inserted
        if (values!!.containsKey(FabricEntry.COLUMN_DESIGN_NAME)) {
            //TODO need to check that there is a name
        }
        //Get safe access to a writable Database
        val dB = dBHelper!!.writableDatabase
        //Get a match for the Uri
        val match = URI_MATCHER.match(uri)
        //Use a Switch to do the right task depending on the Uri
        var returnUri: Uri?
        when (match) {
            FABRIC -> {
                var id = dB?.insert(FabricEntry.TABLE_NAME, null,
                        values)
                //Check if the new row was added or not
                if (id != null && id > 0) {
                    //The new row was added to the Database!
                    //Create the Uri for the new row
                    returnUri = ContentUris.withAppendedId(FabricEntry.CONTENT_URI, id)
                } else {
                    //It failed to add the new row
                    throw android.database.SQLException("Failed to insert row into " + uri)
                }
            }
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }
        context.contentResolver.notifyChange(uri, null)
        return returnUri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val dB = dBHelper!!.writableDatabase

        val match = URI_MATCHER.match(uri)

        val numDeleted: Int

        when (match) {
            FABRIC -> {
                numDeleted = dB.delete(FabricEntry.TABLE_NAME, selection,
                        selectionArgs)
            }
            FABRIC_WITH_ID -> {
                val id = uri.pathSegments[1]

                val mSelection = "_id=?"
                val mSelectionArgs = arrayOf(id)

                numDeleted = dB.delete(FabricEntry.TABLE_NAME, mSelection,
                        mSelectionArgs)
            }
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }

        if (numDeleted != 0) {
            context.contentResolver.notifyChange(uri, null)
        }
        return numDeleted
    }

    override fun update(uri: Uri, contentValues: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {

//        if (selection == null || selectionArgs == null) {
//            return 0
//        }

        val dB = dBHelper!!.writableDatabase

        val match = URI_MATCHER.match(uri)

        val numUpdated: Int

        when (match) {
            FABRIC -> {
                numUpdated = dB.update(FabricEntry.TABLE_NAME, contentValues, selection,
                        selectionArgs)
            }
            FABRIC_WITH_ID -> {
                val id = uri.pathSegments[1]

                val mSelection = "_id=?"
                val mSelectionArgs = arrayOf(id)

                numUpdated = dB.update(FabricEntry.TABLE_NAME, contentValues,
                        mSelection, mSelectionArgs)
            }
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }

        if (numUpdated != 0) {
            context.contentResolver.notifyChange(uri, null)
        }

        return numUpdated
    }
}
