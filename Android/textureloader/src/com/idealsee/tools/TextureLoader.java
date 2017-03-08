package com.idealsee.tools;

import com.idealsee.tools.ImageFileLoader.IImageFileLoadFinish;
import com.unity3d.player.UnityPlayer;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public class TextureLoader {
	
	private static final String TAG = "TextureLoader";
	private static Bitmap updateBitmap;
	private static int[] textureIDList;
	
	private native static void nativeImageLoadFinish(boolean isLoadSuccess);
	private native static void nativeUpdateTextureFinish();
	private native static void nativeInitTexFinish(int[] initTexIdList);
	
	public static void initTexture(int texCount, int imageSize) {
		int[] textures = new int[texCount];
		GLES20.glGenTextures(texCount, textures, 0);
		
		for (int i = 0; i < texCount; i++)
		{
			Log.v(TAG, "glGenTextures id : " + textures[i]);

			// ...and bind it to our array
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
			Log.v(TAG, "glGetError " + GLES20.glGetError());

			// Create Nearest Filtered Texture
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

			// Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
					GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
					GLES20.GL_REPEAT);

			// Use the Android GLUtils to specify a two-dimensional texture image
			// from our bitmap
			
			if (updateBitmap != null) {
				GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, updateBitmap, 0);
				Log.v(TAG, "glGetError " + GLES20.glGetError());
			} else {
				Bitmap initBitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
				GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, initBitmap, 0);
				Log.v(TAG, "glGetError " + GLES20.glGetError());
				initBitmap.recycle();
			}
		}
		if (updateBitmap != null) {
			updateBitmap.recycle();
			updateBitmap = null;
		}

		nativeInitTexFinish(textures);
	}
	
	public static void loadImageFile(String imageFilePath, int imageSize) {
		Log.v(TAG, "Start load Image");
		
		UnityPlayer.currentActivity.runOnUiThread(new ImageFileLoader(imageFilePath, new IImageFileLoadFinish() {

			@Override
			public void OnLoadCompleted(Bitmap mImage) {
				// TODO Auto-generated method stub
				updateBitmap = mImage;
				nativeImageLoadFinish(mImage != null);
			}
			
		}, imageSize, imageSize));
		
//Start a new thread is not effect in Unity
//		new Thread(new ImageFileLoader(imageFilePath, new IImageFileLoadFinish() {
//
//			@Override
//			public void OnLoadCompleted(Bitmap mImage) {
//				// TODO Auto-generated method stub
//				updateBitmap = mImage;
//				Log.v(TAG, "OnLoadCompleted " +mImage.getHeight());
//				nativeImageLoadFinish(mImage != null);
//			}
//			
//		}, imageSize, imageSize)).run();
	}
	
	public static void updateTexture(int textureID) {
		Log.v(TAG, "updateTexture id : " + textureID);
		if (updateBitmap == null) {
			Log.e(TAG, "update image is null!");
			return;
		}
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
		Log.v(TAG, "glGetError " + GLES20.glGetError());
		GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, updateBitmap);
		Log.v(TAG, "glGetError " + GLES20.glGetError());
		nativeUpdateTextureFinish();
		
		updateBitmap.recycle();
		updateBitmap = null;
	}
	
}
