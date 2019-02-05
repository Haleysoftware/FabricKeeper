package com.haleysoftware.fabrickeeper.utils

import android.app.Activity
import android.content.Context
import android.view.View

import com.google.android.gms.ads.AdListener
import com.haleysoftware.fabrickeeper.R

/**
 * Created by Michael Haley on 9/18/17.
 */

class FabricAds(private val adContext: Context) : AdListener() {

    override fun onAdFailedToLoad(i: Int) {
        val rootView = (adContext as Activity).window.decorView.findViewById<View>(android.R.id.content)
        val adSpace = rootView.findViewById<View>(R.id.adSpacer)
        val ads = rootView.findViewById<View>(R.id.adView)

        adSpace.visibility = View.GONE
        ads.visibility = View.GONE

        super.onAdFailedToLoad(i)
    }
}