package com.haleysoftware.fabrickeeper

import android.app.AlertDialog
import android.app.LoaderManager
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.haleysoftware.fabrickeeper.utils.FabricAds
import com.haleysoftware.fabrickeeper.utils.FabricContract.FabricEntry
import java.io.File


/**
 * This class shows the detail screen of the fabric that was clicked in the listview
 * From here, the user can go to the edit screen.
 */
class DetailActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    private val LOADER_ID = 22

    private var adsView: AdView? = null

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    private var fabricUri: Uri? = null

    private var textDescription: TextView? = null
    private var textKeywords: TextView? = null
    private var textLocation: TextView? = null
    private var textProjects: TextView? = null
    private var textYards: TextView? = null
    private var textWidth: TextView? = null
    private var textName: TextView? = null
    private var textDesigner: TextView? = null
    private var textPurchased: TextView? = null
    private var imageFabric: ImageView? = null

    private var imagePath: String = ""

    /**
     * Needs data from the intent to work
     * Starts the adView
     * Gets links to the text views
     * Starts the cursorLoader to get some data
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        val bundleIntent = intent
        fabricUri = bundleIntent.data

        adsView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adsView?.adListener = FabricAds(adContext = this)
        adsView?.loadAd(adRequest)

        textDescription = findViewById(R.id.tv_description)
        textKeywords = findViewById(R.id.tv_keywords)
        textLocation = findViewById(R.id.tv_location)
        textProjects = findViewById(R.id.tv_projects)
        textYards = findViewById(R.id.tv_yards)
        textWidth = findViewById(R.id.tv_fabric_width)
        textName = findViewById(R.id.tv_fabric_name)
        textDesigner = findViewById(R.id.tv_designer)
        textPurchased = findViewById(R.id.tv_purchased)
        imageFabric = findViewById(R.id.iv_fabric_main)



        if (fabricUri != null) {
            loaderManager.initLoader(LOADER_ID, null, this)
        }
    }

    /**
     * Let's create a menu
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    /**
     * Handles the menu item clicks
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        //use when and return true. Keep the return super for default
        when (item?.itemId) {
            R.id.action_edit -> {
                val activityIntent = Intent(this, EditActivity::class.java)
                activityIntent.data = fabricUri
                startActivity(activityIntent)
            }
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     *
     */
    private fun showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.delete_dialog_msg)
        builder.setPositiveButton(R.string.delete, DialogInterface.OnClickListener { dialog, id ->
            // User clicked the "Delete" button, so delete the pet.
            if (deleteFabric()) {
                finish()
            }
        })
        builder.setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
            // User clicked the "Cancel" button, so dismiss the dialog
            // and continue editing the pet.
            dialog?.dismiss()
        })

        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()
    }

    /**
     *
     */
    private fun deleteFabric(): Boolean {
        val numDeleted = contentResolver.delete(fabricUri, null, null)
        if (numDeleted > 0) {
            deleteImage(imagePath)
            return true
        }
        return false
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
                val descriptionText = data.getString(data.getColumnIndex(FabricEntry.COLUMN_DESCRIPTION))
                val keywordsText = data.getString(data.getColumnIndex(FabricEntry.COLUMN_KEYWORDS))
                val locationText = data.getString(data.getColumnIndex(FabricEntry.COLUMN_LOCATION))
                val projectsText = data.getString(data.getColumnIndex(FabricEntry.COLUMN_PROJECTS))
                val yardsInt = data.getDouble(data.getColumnIndex(FabricEntry.COLUMN_YARDS))
                val widthInt = data.getDouble(data.getColumnIndex(FabricEntry.COLUMN_WIDTH))
                val dNameText = data.getString(data.getColumnIndex(FabricEntry.COLUMN_DESIGN_NAME))
                val designerText = data.getString(data.getColumnIndex(FabricEntry.COLUMN_DESIGNER))
                val purchasedText = data.getString(data.getColumnIndex(FabricEntry.COLUMN_PURCHASED))
                imagePath = data.getString(data.getColumnIndex(FabricEntry.COLUMN_IMAGE))

                textDescription?.text = descriptionText
                textKeywords?.text = keywordsText
                textLocation?.text = locationText
                textProjects?.text = projectsText
                textYards?.text = yardsInt.toString()
                textWidth?.text = widthInt.toString()
                textName?.text = dNameText
                textDesigner?.text = designerText
                textPurchased?.text = purchasedText

                if (imagePath.isEmpty()) {
                    imageFabric?.setImageResource(R.drawable.ic_broken_image_24dp)
                } else {
                    imageFabric?.setImageURI(Uri.fromFile(File(imagePath)))
                    imageFabric?.setOnClickListener {
                        val activityIntent = Intent(this, ImageActivity::class.java)
                        activityIntent.data = fabricUri
                        startActivity(activityIntent)
                    }
                }
            }
        }
    }

    /**
     *
     */
    override fun onLoaderReset(loader: Loader<Cursor>?) {
        textDescription?.text = ""
        textKeywords?.text = ""
        textLocation?.text = ""
        textProjects?.text = ""
        textYards?.text = ""
        textWidth?.text = ""
        textName?.text = ""
        textDesigner?.text = ""
        textPurchased?.text = ""
        imageFabric?.setImageResource(R.drawable.ic_broken_image_24dp)
    }
}
