package com.haleysoftware.fabrickeeper

import android.app.LoaderManager.LoaderCallbacks
import android.content.*
import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.haleysoftware.fabrickeeper.utils.FabricAdapter
import com.haleysoftware.fabrickeeper.utils.FabricAds
import com.haleysoftware.fabrickeeper.utils.FabricContract


/**
 * 
 */

/**
 * Main Launch Activity
 *
 * This is used to display the list of fabric items in the SQLite Database
 *
 * User can search with the actionbar, add items with the FAB, view the Details by clicking on a
 * list item
 */
class ListActivity : AppCompatActivity(), LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener {

    //TODO: Need to add more Firebase Analytics
    //TODO: Add Firebase Storage
    //TODO: Look into Firebase Authentication
    //TODO: Look into Firebase Crash Reporting

    private var searchWord = ""
    //The custom fabricAdapter to work with the cursor data and the listview
    private lateinit var fabricAdapter: FabricAdapter
    //Link to the list view to fill with content
    private lateinit var fabricList: ListView

    //TODO: Need to add a gridview and the power to switch between views

    //Shown when the database is empty
    private lateinit var emptyList: TextView
    //Shown when the database query is going on
    private lateinit var loadList: ProgressBar

    private lateinit var adsView: AdView

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val loaderId = 22

    /**
     * Setups the FAB, list item click, Admob, and loadmanager for database items
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        setContentView(R.layout.activity_list)
        supportActionBar?.setTitle(R.string.name_list)

        val floatAction = findViewById<FloatingActionButton>(R.id.fab_add)
        floatAction.setOnClickListener {
            val activityIntent = Intent(this, EditActivity::class.java)
            startActivity(activityIntent)
        }

        fabricList = findViewById(R.id.lv_fabric_list)
        emptyList = findViewById(R.id.tv_empty_list)
        loadList = findViewById(R.id.pb_loading_list)

        fabricList.setOnItemClickListener { _, _, _, id ->
            val activityIntent = Intent(this, DetailActivity::class.java)
            activityIntent.data = ContentUris.withAppendedId(FabricContract.FabricEntry.CONTENT_URI, id)
            startActivity(activityIntent)
        }

        createAds(context = this)

        fabricAdapter = FabricAdapter(this, null, 0)
        fabricList.adapter = fabricAdapter
        loaderManager.initLoader(loaderId, null, this)
    }

    /**
     * Pauses the ad if there is one.
     */
    override fun onPause() {
        super.onPause()
        adsView.pause()
    }

    /**
     * Restarts the loader, used mostly when activity is returned to.
     * Resumes the ad if there is one.
     */
    override fun onResume() {
        super.onResume()
        adsView.resume()
        loaderManager.restartLoader(loaderId, null, this)
    }

    /**
     * Destroys the ad if there is one.
     */
    override fun onDestroy() {
        super.onDestroy()
        adsView.destroy()
    }

    /**
     * Sets up the ad view and displays and ad.
     */
    private fun createAds(context: Context) {
        val adString : String = resources.getString(R.string.banner_ad_unit_id)
        MobileAds.initialize(context, adString)

        adsView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder()
                .addTestDevice("017DFE675121B084DB5B940BFC1C41CC")
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build()
        adsView.adListener = FabricAds(context)
        adsView.loadAd(adRequest)
    }

    /**
     * Let's get a menu created
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        val search = menu!!.findItem(R.id.action_search).actionView as SearchView
        search.setOnQueryTextListener(this)
        return true
    }



    //TODO: Need to add a menu item to remove ads


    /**
     * Don't know if this is needed
     * Was needed for the live search function
     * Needs an search(enter) button press to search
     */
    override fun onQueryTextSubmit(word: String?): Boolean {
        return false
    }

    /**
     * This does a search when the search text field changes
     */
    override fun onQueryTextChange(word: String?): Boolean {
        if (word != null) {
            searchWord = word
        }
        loaderManager.restartLoader(loaderId, null, this)
        return true
    }

    /**
     * Creates the cursor loader for the content provider.
     */
    override fun onCreateLoader(id: Int, bundle: Bundle?): Loader<Cursor> {
        fabricList.visibility = View.INVISIBLE
        emptyList.visibility = View.INVISIBLE
        loadList.visibility = View.VISIBLE

        val projection = arrayOf(
                FabricContract.FabricEntry.ROW_ID,
                FabricContract.FabricEntry.COLUMN_DESCRIPTION,
                FabricContract.FabricEntry.COLUMN_KEYWORDS,
                //FabricContract.FabricEntry.COLUMN_LOCATION,
                //FabricContract.FabricEntry.COLUMN_PROJECTS,
                //FabricContract.FabricEntry.COLUMN_YARDS,
                //FabricContract.FabricEntry.COLUMN_WIDTH,
                FabricContract.FabricEntry.COLUMN_DESIGN_NAME,
                //FabricContract.FabricEntry.COLUMN_DESIGNER,
                FabricContract.FabricEntry.COLUMN_PURCHASED,
                FabricContract.FabricEntry.COLUMN_IMAGE)

        //TODO Might add an option to change the sort order. If requested
        val sortOrder = FabricContract.FabricEntry.COLUMN_DESCRIPTION + " COLLATE NOCASE ASC"

        if (searchWord.isNotEmpty()) {
            val selection = FabricContract.FabricEntry.COLUMN_DESCRIPTION + " LIKE ? OR " +
                    FabricContract.FabricEntry.COLUMN_KEYWORDS + " LIKE ? OR " +
                    FabricContract.FabricEntry.COLUMN_DESIGN_NAME + " LIKE ? OR " +
                    FabricContract.FabricEntry.COLUMN_PURCHASED + " LIKE ?"
            val selectionArgs = arrayOf("%$searchWord%", "%$searchWord%", "%$searchWord%", "%$searchWord%")

            return CursorLoader(this, FabricContract.FabricEntry.CONTENT_URI,
                    projection, selection, selectionArgs, sortOrder)
        }

        return CursorLoader(this, FabricContract.FabricEntry.CONTENT_URI,
                projection, null, null, sortOrder)
    }

    /**
     * Finished loading data from the content provider!
     * Send that cursor to the adapter.
     */
    override fun onLoadFinished(loader: Loader<Cursor>?, cursor: Cursor?) {
        loadList.visibility = View.INVISIBLE
        if (cursor?.count != 0 && cursor != null) {
            fabricList.visibility = View.VISIBLE
            emptyList.visibility = View.INVISIBLE
        } else {

            fabricList.visibility = View.INVISIBLE
            emptyList.visibility = View.VISIBLE
        }
        fabricAdapter.swapCursor(cursor)
    }

    /**
     * Time to reset! Swap that cursor
     */
    override fun onLoaderReset(loader: Loader<Cursor>?) {
        fabricAdapter.swapCursor(null)
    }
}
