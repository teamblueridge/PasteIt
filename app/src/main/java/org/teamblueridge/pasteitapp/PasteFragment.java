package org.teamblueridge.pasteitapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Loads the view for the Paste fragment
 */
public class PasteFragment extends Fragment {
    //private static final String TAG = "TeamBlueRidge";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_paste, container, false);
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.app_name));
        return rootView;
    }
}