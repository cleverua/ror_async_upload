package com.cleverua.uploader;

import android.app.Application;

/**
 * Created by: Alex Kulakovsky
 * Date: 5/7/13
 * Time: 1:56 PM
 * Email: akulakovsky@cleverua.com
 */
public class MyApplication extends Application {

    private int lastS3Id = -1;
    private int lastFsId = -1;

    public int getLastS3Id() {
        return lastS3Id;
    }

    public void setLastS3Id(int lastS3Id) {
        this.lastS3Id = lastS3Id;
    }

    public int getLastFsId() {
        return lastFsId;
    }

    public void setLastFsId(int lastFsId) {
        this.lastFsId = lastFsId;
    }
}
