package org.teamblueridge.pasteit;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PasteFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_paste, container, false);
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.app_name));
        return rootView;
    }
}