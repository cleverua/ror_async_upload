package com.cleverua.uploader;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by: Alex Kulakovsky
 * Date: 5/7/13
 * Time: 11:37 AM
 * Email: akulakovsky@cleverua.com
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
