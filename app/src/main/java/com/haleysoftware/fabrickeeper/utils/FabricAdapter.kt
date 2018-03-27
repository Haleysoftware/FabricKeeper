package com.haleysoftware.fabrickeeper.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import com.haleysoftware.fabrickeeper.R
import com.haleysoftware.fabrickeeper.utils.FabricContract.FabricEntry
import java.io.File


/**
 * Created by Michael Haley on 9/10/17.
 */

class FabricAdapter : CursorAdapter {

//    var memoryCache : LruCache<String, Bitmap>? = null

    constructor(context: Context, cursor: Cursor?, autoRequery: Boolean) : super(context, cursor, autoRequery) {
        //Need to look up what autoRequery does and if I want to use it.
    }

    constructor(context: Context, cursor: Cursor?, flags: Int) : super(context, cursor, flags) {
        //Just passing on to the super.

        //TODO: Need to look into storing the bitmaps into memory to make them load faster
//        val maxMemory = Runtime.getRuntime().maxMemory().toInt() / 1024
//        val cacheSize = maxMemory / 8

//        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
//            override fun sizeOf(key: String, bitmap: Bitmap): Int {
//                // The cache size will be measured in kilobytes rather than
//                // number of items.
//                return bitmap.byteCount / 1024
//            }
//        }
    }

//    private fun addBitmapToMemoryCache(key: String, bitmap: Bitmap) {
//        if (getBitmapFromMemCache(key) == null) {
//            memoryCache?.put(key, bitmap)
//
//        }
//    }
//
//    private fun getBitmapFromMemCache(key: String): Bitmap? {
//        return memoryCache?.get(key)
//    }


    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val description = view.findViewById<TextView>(R.id.tv_fabric_description)
        val keywords = view.findViewById<TextView>(R.id.tv_fabric_keywords)
        val purchase = view.findViewById<TextView>(R.id.tv_fabric_purchase)
        val design = view.findViewById<TextView>(R.id.tv_fabric_design)
        val image = view.findViewById<ImageView>(R.id.iv_fabric_thumb)

        val desText = cursor.getString(cursor.getColumnIndex(FabricEntry.COLUMN_DESCRIPTION))
        val keyText = cursor.getString(cursor.getColumnIndex(FabricEntry.COLUMN_KEYWORDS))
        val buyText = cursor.getString(cursor.getColumnIndex(FabricEntry.COLUMN_PURCHASED))
        val designText = cursor.getString(cursor.getColumnIndex(FabricEntry.COLUMN_DESIGN_NAME))
        val imageText = cursor.getString(cursor.getColumnIndex(FabricEntry.COLUMN_IMAGE))

        description.text = desText
        keywords.text = keyText
        purchase.text = buyText
        design.text = designText

        if (imageText.isNotEmpty()) {
            image.setImageURI(Uri.fromFile(File(imageText)))
            image.visibility = View.VISIBLE
        } else {
            image.setImageResource(R.drawable.ic_broken_image_24dp)
            image.visibility = View.INVISIBLE
        }
    }
}
