package com.cleverua.uploader;

import android.*;
import android.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * Created by: Alex Kulakovsky
 * Date: 5/7/13
 * Time: 11:39 AM
 * Email: akulakovsky@cleverua.com
 */
public class SettingsActivity extends Activity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}