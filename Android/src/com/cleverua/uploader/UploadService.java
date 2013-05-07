package com.cleverua.uploader;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Created by: Alex Kulakovsky
 * Date: 5/7/13
 * Time: 10:15 AM
 * Email: akulakovsky@cleverua.com
 */
public class UploadService extends IntentService {

    public static final String BROADCAST_COMPLETE = "com.cleverua.uploader.ACTION_COMPLETE";

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    public static final int ID_FS = 1101;
    public static final int ID_S3 = 1102;

    private String baseUrl;
    private String uploadAction;

    private boolean uploadFs;
    private boolean uploadS3;

    public UploadService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent()");
        final String imageUrl = intent.getStringExtra("image");

        getSettings();

        if (uploadFs) {
            showNotification(ID_FS);
            String status = uploadImage(imageUrl, baseUrl + uploadAction, ID_FS);
            notifyUI(status, ID_FS);
        }

        if (uploadS3) {
            showNotification(ID_S3);
            String status = uploadImage(imageUrl, baseUrl + uploadAction, ID_S3);
            notifyUI(status, ID_S3);
        }
    }

    private void getSettings() {
        Log.d(TAG, "getSettings()");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        baseUrl = preferences.getString("base_url", "");

        if (!TextUtils.isEmpty(baseUrl)){
            if (!baseUrl.endsWith("/")) {
                baseUrl = baseUrl + "/";
            }
        }

        uploadAction = preferences.getString("action", "");

        uploadFs = preferences.getBoolean("fs", true);
        uploadS3 = preferences.getBoolean("s3", true);

        Log.d(TAG, "Obtained preferences => " +
                "baseUrl=" + baseUrl +
                ", action=" + uploadAction +
                ", uploadFs=" + uploadFs +
                ", uploadS3=" + uploadS3);
    }

    private void showNotification(int notificationId){
        Log.d(TAG, "showNotification()");
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("CleverUploader")
                .setSmallIcon(R.drawable.ic_upload);

        switch (notificationId) {
            case ID_FS:
                mBuilder.setContentText("Uploading image to FS...");
                break;

            case ID_S3:
                mBuilder.setContentText("Uploading image to S3...");
                break;
        }

        mBuilder.setProgress(0, 0, true);
        mNotifyManager.notify(notificationId, mBuilder.build());
    }

    private void setNotificationStatus(int notificationId, String status) {
        Log.d(TAG, "setNotificationStatus()");
        mBuilder.setProgress(0, 0, false);

        String success = "";
        String fail = "";

        switch (notificationId) {
            case ID_S3:
                success = "Upload to S3 complete!";
                fail = "Failed to upload to S3 :(";
                break;

            case ID_FS:
                success = "Upload to FS complete!";
                fail = "Failed to upload to FS :(";
                break;
        }

        if (status.equals("ok")) {
            mBuilder.setContentText(success);
        } else {
            mBuilder.setContentText(fail);
        }
        mNotifyManager.notify(notificationId, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    private void notifyUI(String status, int id) {
        Intent i = new Intent(BROADCAST_COMPLETE);
        i.putExtra("status", status);
        i.putExtra("id", id);
        sendBroadcast(i);
        setNotificationStatus(id, status);
    }

    private String uploadImage(String imageUrl, String serverUrl, int id){
        Log.d(TAG, "uploadImage()");

        String status = "fail"; //we are not optimistic...

        // Multi-part content body.
        MultipartEntity mpEntity = new MultipartEntity();

        if (!TextUtils.isEmpty(imageUrl) && !TextUtils.isEmpty(serverUrl)){
            Log.d(TAG, "uploadImage() -> serverUrl = " + serverUrl);
            Log.d(TAG, "uploadImage() -> imageUrl = " + imageUrl);
            File file = new File(imageUrl);
            ContentBody photo = new FileBody(file, "image/jpeg");
            mpEntity.addPart("photo", photo);

            try {

                switch (id) {
                    case ID_FS:
                        mpEntity.addPart("upload_type", new StringBody("fs"));
                        break;
                    case ID_S3:
                        mpEntity.addPart("upload_type", new StringBody("s3"));
                }

                // Set up HTTP client.
                HttpParams httpParameters = new BasicHttpParams();
                // Set the timeout in milliseconds until a connection is established.
                // The default value is zero, that means the timeout is not used.
                int timeoutConnection = 3000;
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
                // Set the default socket timeout (SO_TIMEOUT)
                // in milliseconds which is the timeout for waiting for data.
                int timeoutSocket = 5000;
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

                HttpClient client = new DefaultHttpClient(httpParameters);
                //HttpClient client = new DefaultHttpClient();

                // Put method.
                HttpPost method = new HttpPost(serverUrl);
                method.setEntity(mpEntity);
                HttpResponse response = client.execute(method);

                // Result.
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null){
                    String result = EntityUtils.toString(responseEntity);
                    JSONObject object = new JSONObject(result);

                    if (object.optString("status").equals("ok")) {
                        status = "ok";
                        switch (id) {
                            case ID_FS:
                                ((MyApplication) getApplication()).setLastFsId(object.getInt("id"));
                                break;

                            case ID_S3:
                                ((MyApplication) getApplication()).setLastS3Id(object.getInt("id"));
                                break;
                        }
                    }

                    Log.d("TAG", responseEntity.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return status;
    }

    private static final String TAG = UploadService.class.getSimpleName();
}
