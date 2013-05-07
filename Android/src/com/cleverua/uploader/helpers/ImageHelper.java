package com.cleverua.uploader.helpers;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by: Alex Kulakovsky
 * Date: 5/7/13
 * Time: 11:24 AM
 * Email: akulakovsky@cleverua.com
 */
public class ImageHelper {

    public static final int IMAGE_GALLERY_REQUEST = 1001;

    public static void setPic(ImageButton mImageView, String mCurrentPhotoPath) {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = mImageView.getLayoutParams().width;
        int targetH = mImageView.getLayoutParams().height;

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.max(photoW / targetW, photoH / targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        if (bitmap == null){
            return;
        }

		/* Associate the Bitmap to the ImageView */
        mImageView.setImageBitmap(scaleCenterCrop(bitmap, targetH, targetH));
    }

    public static Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }

    public static String getFilePathFromUri(Context context, Uri uri) {
        // a file could be picked using some FileManager, so check this case
        // first
        if (uri.getScheme().equals("file")) {
            String path = uri.getPath();
            Log.d("ImageHelper:", "getFilePathFromUri: path = " + path);
            File f = new File(path);
            String fileName = f.getName().toLowerCase();
            // allow only images
            for (String ext : new String[] { ".jpg", ".jpeg" }) {
                if (fileName.endsWith(ext)) {
                    return path;
                }
            }
            return null;
        }

        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };

            cursor = context.getContentResolver().query(uri, proj, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(columnIndex);
            }

            return null;

        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

}
