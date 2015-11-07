package org.teamblueridge.pasteitapp

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Loads the view for the Paste fragment
 */
class PasteFragment : Fragment() {
    //private static final String TAG = "TeamBlueRidge";

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_paste, container, false)
        (activity as MainActivity).setActionBarTitle(getString(R.string.app_name))
        return rootView
    }
}