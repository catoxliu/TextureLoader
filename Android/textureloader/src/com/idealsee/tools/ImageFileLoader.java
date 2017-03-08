package com.idealsee.tools;


import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

public class ImageFileLoader implements Runnable {
	
	private final String TAG = "ImageFileLoader";
	private float targetHeight, targetWidth;
	private String ImageFilePath = "";

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Log.v(TAG, "Thread load Image run");
		Bitmap normalImage = null;
		try {
			File t = new File(ImageFilePath);
			if (t.exists()) {
				Bitmap rawImage = BitmapFactory.decodeFile(ImageFilePath);
				normalImage = NormalizeBitmap(rawImage);
			} else {
				Log.w(TAG, "Image file " + ImageFilePath + " not exist!");
			}
		} catch (Exception e) {
			Log.e(TAG, "Load Bitmap Error : " + e);
		}
		
		Log.v(TAG, "Thread load image finish! ");
		
		if (mImageFileLoadFinish != null) {
			mImageFileLoadFinish.OnLoadCompleted(normalImage);
		}
	}
	
	public interface IImageFileLoadFinish {
		void OnLoadCompleted(Bitmap mImage);
	}

	private IImageFileLoadFinish mImageFileLoadFinish;
	
	public ImageFileLoader(String filePath, IImageFileLoadFinish callback, float width, float height)
	{
		ImageFilePath = filePath;
		mImageFileLoadFinish = callback;
		targetWidth = width;
		targetHeight = height;
	}
	
	private Bitmap NormalizeBitmap(Bitmap raw) {
		Matrix scaleMatrix = new Matrix();
		float rawHeight = raw.getHeight();
		float rawWidth = raw.getWidth();
		Bitmap target;
		if (rawHeight != targetHeight || rawWidth != targetWidth) {
			scaleMatrix.postScale(targetWidth / rawWidth, -targetHeight / rawHeight);
			target = Bitmap.createBitmap(raw, 0, 0, (int)rawWidth, (int)rawHeight, scaleMatrix, false);
		} else {
			target = raw;
		}
		return target;
	}

}
