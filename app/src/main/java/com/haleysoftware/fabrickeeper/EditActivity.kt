package com.haleysoftware.fabrickeeper

import android.app.LoaderManager
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.haleysoftware.fabrickeeper.utils.FabricAds
import com.haleysoftware.fabrickeeper.utils.FabricContract.FabricEntry
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 *
 */
class EditActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    private val loaderId = 22

    private val cameraIntentRequest = 3224

    private var adsView: AdView? = null

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    private var fabricUri: Uri? = null

    private var textDescription: EditText? = null
    private var textKeywords: EditText? = null
    private var textLocation: EditText? = null
    private var textProjects: EditText? = null
    private var textYards: EditText? = null
    private var textWidth: EditText? = null
    private var textName: EditText? = null
    private var textDesigner: EditText? = null
    private var textPurchased: EditText? = null
    private var imageFabric: ImageView? = null

    private var pathImage: String = ""
    private var tempBitmap: Bitmap? = null
    private var tempPath: String = ""

    /**
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        val bundleIntent = intent
        fabricUri = bundleIntent.data

        if (fabricUri == null) {
            supportActionBar?.setTitle(R.string.name_add)
        } else {
            supportActionBar?.setTitle(R.string.name_edit)
        }

        adsView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adsView?.adListener = FabricAds(this)
        adsView?.loadAd(adRequest)

        textDescription = findViewById(R.id.et_description)
        textKeywords = findViewById(R.id.et_keywords)
        textLocation = findViewById(R.id.et_location)
        textProjects = findViewById(R.id.et_projects)
        textYards = findViewById(R.id.et_yards)
        textWidth = findViewById(R.id.et_fabric_width)
        textName = findViewById(R.id.et_fabric_name)
        textDesigner = findViewById(R.id.et_designer)
        textPurchased = findViewById(R.id.et_purchased)

        imageFabric = findViewById(R.id.iv_fabric_main)
        imageFabric?.setOnClickListener {
            takePhoto()
        }

        if (fabricUri != null) {
            loaderManager.initLoader(loaderId, null, this)
        } else {
            imageFabric?.setImageResource(R.drawable.ic_photo_camera_24dp)
        }
    }

    /**
     *
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (fabricUri == null) {
            menuInflater.inflate(R.menu.menu_add, menu)
        } else {
            menuInflater.inflate(R.menu.menu_edit, menu)
        }
        return true
    }

    /**
     *
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
        //Used in Add and Edit Modes
            R.id.action_save -> {
                if (saveFabric()) {
                    finish()
                }
                return true
            }
        //Used in Add Mode
            R.id.action_clear -> {
                clearFabric()
                return true
            }
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
    private fun clearFabric() {
        textDescription?.text?.clear()
        textKeywords?.text?.clear()
        textLocation?.text?.clear()
        textProjects?.text?.clear()
        textYards?.text?.clear()
        textWidth?.text?.clear()
        textName?.text?.clear()
        textDesigner?.text?.clear()
        textPurchased?.text?.clear()

        imageFabric?.setImageResource(R.drawable.ic_photo_camera_24dp)
    }

    /**
     *
     */
    private fun saveFabric(): Boolean {
        val descriptionText = textDescription!!.text.toString()
        if (descriptionText.isEmpty()) {
            Toast.makeText(this, R.string.empty_description, Toast.LENGTH_SHORT).show()
            return false
        }

        val keywordsText = textKeywords!!.text.toString()
        if (keywordsText.isEmpty()) {
            Toast.makeText(this, R.string.empty_keywords, Toast.LENGTH_SHORT).show()
            return false
        }

        val yardsText = textYards!!.text.toString()
        val yardsInt: Double?
        if (yardsText.isEmpty()) {
            yardsInt = 0.0
        } else {
            yardsInt = yardsText.toDoubleOrNull()
            if (yardsInt == null) {
                Toast.makeText(this, R.string.null_yards, Toast.LENGTH_SHORT).show()
                return false
            }
        }

        val widthText = textWidth!!.text.toString()
        val widthInt: Double?
        if (widthText.isEmpty()) {
            widthInt = 0.0
        } else {
            widthInt = widthText.toDoubleOrNull()
            if (widthInt == null) {
                Toast.makeText(this, R.string.null_width, Toast.LENGTH_SHORT).show()
                return false
            }
        }

        if (tempBitmap != null) {
            tempPath = saveImage(tempBitmap!!)
        } else {
            tempPath = pathImage
        }

        val contentValues = ContentValues()
        contentValues.put(FabricEntry.COLUMN_DESCRIPTION, descriptionText)
        contentValues.put(FabricEntry.COLUMN_KEYWORDS, keywordsText)
        contentValues.put(FabricEntry.COLUMN_LOCATION, textLocation?.text.toString())
        contentValues.put(FabricEntry.COLUMN_PROJECTS, textProjects?.text.toString())
        contentValues.put(FabricEntry.COLUMN_YARDS, yardsInt)
        contentValues.put(FabricEntry.COLUMN_WIDTH, widthInt)
        contentValues.put(FabricEntry.COLUMN_DESIGN_NAME, textName?.text.toString())
        contentValues.put(FabricEntry.COLUMN_DESIGNER, textDesigner?.text.toString())
        contentValues.put(FabricEntry.COLUMN_PURCHASED, textPurchased?.text.toString())
        contentValues.put(FabricEntry.COLUMN_IMAGE, tempPath)

        if (fabricUri == null) {
            //Add
            val uri = contentResolver.insert(FabricEntry.CONTENT_URI, contentValues)
            val rowId = ContentUris.parseId(uri)

            if (rowId == -1L) {
                Toast.makeText(this, R.string.fail_to_add, Toast.LENGTH_SHORT).show()
                deleteImage(tempPath)
                return false
            }
            return true
        } else {
            //Edit
            Log.d("FabricTest", "Uri: " + fabricUri.toString())
            val updatedNum = contentResolver.update(fabricUri, contentValues, null, null)
            Log.d("FabricTest", "updated: " + updatedNum.toString())
            if (updatedNum == 0) {
                Toast.makeText(this, R.string.fail_to_update, Toast.LENGTH_SHORT).show()
                deleteImage(tempPath)
                return false
            }
            if (!pathImage.equals(other = tempPath, ignoreCase = true)) {
                deleteImage(imagePath = pathImage)
            }
            return true
        }

    }

    /**
     *
     */
    private fun takePhoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, cameraIntentRequest)
        }
    }

    /**
     *
     */
    private fun saveImage(bitmap: Bitmap): String {
        val fileName = "fabric_" + System.currentTimeMillis().toString() + ".png"
        val filePath = File(filesDir, fileName)

        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(filePath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return filePath.absolutePath
    }

    /**
     *
     */
    private fun deleteImage(imagePath: String) {
        if (imagePath.isNotEmpty()) {
            var file = File(imagePath)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    /**
     *
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == cameraIntentRequest) {
            when (resultCode) {
                RESULT_OK -> {
                    val extras = data!!.extras
                    tempBitmap = extras!!.get("data") as Bitmap

                    imageFabric?.setImageBitmap(tempBitmap)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     *
     */
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {

        val projection = arrayOf(
                //FabricEntry.ROW_ID,
                FabricEntry.COLUMN_DESCRIPTION,
                FabricEntry.COLUMN_KEYWORDS,
                FabricEntry.COLUMN_LOCATION,
                FabricEntry.COLUMN_PROJECTS,
                FabricEntry.COLUMN_YARDS,
                FabricEntry.COLUMN_WIDTH,
                FabricEntry.COLUMN_DESIGN_NAME,
                FabricEntry.COLUMN_DESIGNER,
                FabricEntry.COLUMN_PURCHASED,
                FabricEntry.COLUMN_IMAGE)

        val selection = FabricEntry.ROW_ID + "=?"
        val fabricId = ContentUris.parseId(fabricUri)
        val selectionArgs = arrayOf(fabricId.toString())

        return CursorLoader(this, FabricEntry.CONTENT_URI,
                projection, selection, selectionArgs, null)
    }

    /**
     *
     */
    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
        if (data != null) {
            if (data.moveToFirst()) {
                val descriptionText = data.getString(data.getColumnIndexOrThrow(FabricEntry.COLUMN_DESCRIPTION))
                val keywordsText = data.getString(data.getColumnIndex(FabricEntry.COLUMN_KEYWORDS))
                val locationText = data.getString(data.getColumnIndex(FabricEntry.COLUMN_LOCATION))
                val projectsText = data.getString(data.getColumnIndex(FabricEntry.COLUMN_PROJECTS))
                val yardsInt = data.getDouble(data.getColumnIndex(FabricEntry.COLUMN_YARDS))
                val widthInt = data.getDouble(data.getColumnIndex(FabricEntry.COLUMN_WIDTH))
                val dNameText = data.getString(data.getColumnIndex(FabricEntry.COLUMN_DESIGN_NAME))
                val designerText = data.getString(data.getColumnIndex(FabricEntry.COLUMN_DESIGNER))
                val purchasedText = data.getString(data.getColumnIndex(FabricEntry.COLUMN_PURCHASED))
                pathImage = data.getString(data.getColumnIndex(FabricEntry.COLUMN_IMAGE))

                textDescription?.setText(descriptionText)
                textKeywords?.setText(keywordsText)
                textLocation?.setText(locationText)
                textProjects?.setText(projectsText)
                textYards?.setText(yardsInt.toString())
                textWidth?.setText(widthInt.toString())
                textName?.setText(dNameText)
                textDesigner?.setText(designerText)
                textPurchased?.setText(purchasedText)


                if (pathImage.isEmpty()) {
                    imageFabric?.setImageResource(R.drawable.ic_photo_camera_24dp)
                } else {
                    imageFabric?.setImageURI(Uri.fromFile(File(pathImage)))
                }
            }
        }
    }

    /**
     *
     */
    override fun onLoaderReset(loader: Loader<Cursor>?) {
        textDescription?.text?.clear()
        textKeywords?.text?.clear()
        textLocation?.text?.clear()
        textProjects?.text?.clear()
        textYards?.text?.clear()
        textWidth?.text?.clear()
        textName?.text?.clear()
        textDesigner?.text?.clear()
        textPurchased?.text?.clear()

        imageFabric?.setImageResource(R.drawable.ic_broken_image_24dp)
    }
}
