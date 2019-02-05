package com.haleysoftware.fabrickeeper

import android.app.LoaderManager
import android.content.ContentUris
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.haleysoftware.fabrickeeper.utils.FabricContract
import java.io.File

/**
 *
 */
class ImageActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    

    private val LOADER_ID = 33

    private lateinit var imageFabric: ImageView

    private var fabricUri: Uri? = null

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    /**
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        val bundleIntent = intent
        fabricUri = bundleIntent.data

        imageFabric = findViewById(R.id.iv_fabric_full)

        if (fabricUri != null) {
            loaderManager.initLoader(LOADER_ID, null, this)
        }
    }

    /**
     *
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     *
     */
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {

        val projection = arrayOf(FabricContract.FabricEntry.ROW_ID,
                FabricContract.FabricEntry.COLUMN_IMAGE)

        val selection = FabricContract.FabricEntry.ROW_ID + "=?"
        val fabricId = ContentUris.parseId(fabricUri)
        val selectionArgs = arrayOf(fabricId.toString())

        return CursorLoader(this, FabricContract.FabricEntry.CONTENT_URI,
                projection, selection, selectionArgs, null)
    }

    /**
     *
     */
    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
        if (data != null) {
            data.moveToFirst()

            val imageText = data.getString(data.getColumnIndex(FabricContract.FabricEntry.COLUMN_IMAGE))


            if (imageText.isEmpty()) {
                imageFabric.setImageResource(R.drawable.ic_broken_image_24dp)
            } else {
                imageFabric.setImageURI(Uri.fromFile(File(imageText)))
            }
        }
    }

    /**
     *
     */
    override fun onLoaderReset(loader: Loader<Cursor>?) {
        imageFabric.setImageResource(R.drawable.ic_broken_image_24dp)
    }
}
