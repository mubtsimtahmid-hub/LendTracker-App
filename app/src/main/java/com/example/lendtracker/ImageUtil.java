package com.example.lendtracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Converts photos to/from Base64 text so they can be saved
 * directly inside Firebase Realtime Database (no paid Storage needed).
 */
public class ImageUtil {

    private static final int MAX_SIZE = 500; // max width/height in pixels

    /** Bitmap -> small JPEG -> Base64 string */
    public static String bitmapToBase64(Bitmap bitmap) {
        Bitmap small = scaleDown(bitmap);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        small.compress(Bitmap.CompressFormat.JPEG, 70, out);
        return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
    }

    /** Base64 string -> Bitmap (returns null if empty or broken) */
    public static Bitmap base64ToBitmap(String text) {
        if (text == null || text.isEmpty()) return null;
        try {
            byte[] bytes = Base64.decode(text, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Reads a gallery photo (Uri) as a Bitmap without using too much memory */
    public static Bitmap uriToBitmap(Context context, Uri uri) throws IOException {
        // 1. Read only the size of the image
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        InputStream in = context.getContentResolver().openInputStream(uri);
        if (in == null) throw new IOException("Cannot open image");
        BitmapFactory.decodeStream(in, null, bounds);
        in.close();

        // 2. Pick a sample size so the image is not huge
        int sample = 1;
        while (bounds.outWidth / sample > 1000 || bounds.outHeight / sample > 1000) {
            sample *= 2;
        }

        // 3. Read the real image
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        in = context.getContentResolver().openInputStream(uri);
        if (in == null) throw new IOException("Cannot open image");
        Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
        in.close();
        if (bitmap == null) throw new IOException("Not a valid image");
        return bitmap;
    }

    private static Bitmap scaleDown(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w <= MAX_SIZE && h <= MAX_SIZE) return bitmap;
        float ratio = Math.min((float) MAX_SIZE / w, (float) MAX_SIZE / h);
        return Bitmap.createScaledBitmap(bitmap, Math.round(w * ratio), Math.round(h * ratio), true);
    }
}
