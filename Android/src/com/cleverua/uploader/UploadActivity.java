package com.cleverua.uploader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.cleverua.uploader.helpers.ImageHelper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class UploadActivity extends Activity {

    private String takenImageUrl;
    private TextView tvFsStat;
    private TextView tvS3Stat;

    private UploadReceiver receiver = new UploadReceiver();

    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tvFsStat = (TextView) findViewById(R.id.fs_stat);
        tvS3Stat = (TextView) findViewById(R.id.s3_stat);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        registerReceiver(receiver, new IntentFilter(UploadService.BROADCAST_COMPLETE));
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");
        if ((requestCode == ImageHelper.IMAGE_GALLERY_REQUEST)
                && (resultCode == RESULT_OK)) {
            Log.d(TAG, "onActivityResult() - IMAGE_GALLERY_REQUEST");
            takenImageUrl = ImageHelper.getFilePathFromUri(getApplicationContext(), data.getData());
            if (takenImageUrl == null) {
                Log.d(TAG, "Failed to open image");
            } else {
                //imageType = IMAGE_TYPE_PHOTO;
                ImageButton imageButton = (ImageButton) findViewById(R.id.imageview);
                ImageHelper.setPic(imageButton, takenImageUrl);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected()");
        switch (item.getItemId()) {
            case R.id.clear:
                clearSelection();
                break;

            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.stat:
                getStatistics();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getStatistics(){
        int lastFsId = ((MyApplication) getApplication()).getLastFsId();
        int lastS3Id = ((MyApplication) getApplication()).getLastS3Id();

        if (lastFsId != -1) {
            new GetStatisticTask(lastFsId, tvFsStat).execute();
        }

        if (lastS3Id != -1) {
            new GetStatisticTask(lastS3Id, tvS3Stat).execute();
        }

        if (lastFsId == -1 && lastS3Id == -1) {
            Toast.makeText(this, "Please upload photo first!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onSelectImage(View view) {
        takeImageFromGallery();
    }

    public void onClickUpload(View view) {
        Log.d(TAG, "onClickUpload()");
        if (((Button) view).isEnabled()){
            if (takenImageUrl != null) {
                setUploadButtonEnabled(false);
                startUploadingService();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Caution!")
                        .setMessage("Please select image!")
                        .setPositiveButton("OK", null)
                        .create().show();
            }
        } else {
            Toast.makeText(this, "Already uploading...", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearSelection() {
        Log.d(TAG, "clearSelection()");
        takenImageUrl = null;
        ImageButton imageButton = (ImageButton) findViewById(R.id.imageview);
        imageButton.setImageBitmap(null);
        setUploadButtonEnabled(true);
        ((MyApplication) getApplication()).setLastFsId(-1);
        ((MyApplication) getApplication()).setLastS3Id(-1);
        tvFsStat.setVisibility(View.GONE);
        tvS3Stat.setVisibility(View.GONE);
    }

    private void takeImageFromGallery() {
        Log.d(TAG, "takeImageFromGallery()");
        final Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i, ImageHelper.IMAGE_GALLERY_REQUEST);
    }

    private void startUploadingService() {
        Log.d(TAG, "startUploadingService()");
            Intent i = new Intent(this, UploadService.class);
            i.putExtra("image", takenImageUrl);
            startService(i);
    }

    private class UploadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "UploadReceiver -> onReceive()");

            if (intent.getStringExtra("status").equals("ok")) {
                int id = intent.getIntExtra("id", -1);
                String msg = "";
                switch (id) {
                    case UploadService.ID_FS:
                        msg = "Successfully uploaded to FS!";
                        break;
                    case UploadService.ID_S3:
                        msg = "Successfully uploaded to S3!";
                        break;
                }
                Toast.makeText(UploadActivity.this, msg, Toast.LENGTH_SHORT).show();
            } else {
                int id = intent.getIntExtra("id", -1);
                String msg = "";
                switch (id) {
                    case UploadService.ID_FS:
                        msg = "Failed to upload to FS! :(";
                        break;
                    case UploadService.ID_S3:
                        msg = "Failed to upload to S3! :(";
                        break;
                }
                Toast.makeText(UploadActivity.this, msg, Toast.LENGTH_SHORT).show();
            }

            setUploadButtonEnabled(true);
        }
    }

    private void setUploadButtonEnabled(boolean isEnabled) {
        Button button = (Button) findViewById(R.id.upload_button);
        button.setEnabled(isEnabled);
    }

    private class GetStatisticTask extends AsyncTask<Void, Void, String> {

        private ProgressDialog dialog;
        private TextView textView;
        private int id;

        private GetStatisticTask(int id, TextView textView) {
            this.textView = textView;
            this.id = id;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(UploadActivity.this);
            dialog.setMessage("Getting statistics...");
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(UploadActivity.this);
            String baseUrl = preferences.getString("base_url", "");
            if (!TextUtils.isEmpty(baseUrl)){
                if (!baseUrl.endsWith("/")) {
                    baseUrl = baseUrl + "/";
                }
            }

            String statsActionUrl = preferences.getString("stats_action", "");

            if (!TextUtils.isEmpty(baseUrl) && !TextUtils.isEmpty(statsActionUrl)) {
                try {
                    HttpClient client = new DefaultHttpClient();
                    //HttpClient client = new DefaultHttpClient();

                    // Put method.
                    HttpGet method = new HttpGet(baseUrl + statsActionUrl + "?id=" + id);
                    HttpResponse response = client.execute(method);

                    // Result.
                    HttpEntity responseEntity = response.getEntity();
                    if (responseEntity != null){
                        String result = EntityUtils.toString(responseEntity);
                        JSONObject object = new JSONObject(result);

                        return object.optString("time");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(String str) {
            dialog.dismiss();
            textView.setText(str);
            textView.setVisibility(View.VISIBLE);
        }
    }

    private static final String TAG = UploadActivity.class.getSimpleName();
}
