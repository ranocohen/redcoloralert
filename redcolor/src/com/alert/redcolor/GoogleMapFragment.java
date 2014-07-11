package com.alert.redcolor;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;

public class GoogleMapFragment extends SupportMapFragment {

    private static final String SUPPORT_MAP_BUNDLE_KEY = "MapOptions";

    public static interface OnGoogleMapFragmentListener {
        void onMapReady(GoogleMap map);
    }

    public static GoogleMapFragment newInstance() {
        return new GoogleMapFragment();
    }

    public static GoogleMapFragment newInstance(GoogleMapOptions options) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(SUPPORT_MAP_BUNDLE_KEY, options);

        GoogleMapFragment fragment = new GoogleMapFragment();
        fragment.setArguments(arguments);
        

        //fragment.getMap().getUiSettings().setMyLocationButtonEnabled(true);
        fragment.getMap().getUiSettings().setCompassEnabled(true);
        fragment.getMap().getUiSettings().setZoomControlsEnabled(false);
                
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnGoogleMapFragmentListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().getClass().getName() + " must implement OnGoogleMapFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (mCallback != null) {
            mCallback.onMapReady(getMap());
        }
        return view;
    }

    private OnGoogleMapFragmentListener mCallback;
}