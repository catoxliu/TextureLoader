package com.idealsee.tools;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import com.idealsee.tools.ImageFileLoader.IImageFileLoadFinish;
import com.unity3d.player.UnityPlayer;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLES31;
import android.opengl.GLES31Ext;
import android.util.Log;

public class TextureLoader {
	
	private static final String TAG = "TextureLoader";
	private static Bitmap updateBitmap;
	private static int[] textureIDList;
	
	private native static void nativeImageLoadFinish(boolean isLoadSuccess);
	private native static void nativeUpdateTextureFinish(int textureID);
	private native static void nativeInitTexFinish(int[] initTexIdList);
	
	private static boolean isGlError(String msg) {
		int result = GLES30.glGetError();
		if (result != 0) {
			Log.e(TAG, "GL ERROR CODE ["+result+"] :" + msg);
			return true;
		}
		return false;
	}

	
	public static void initTexture(int texCount, int imageSize) {
		int[] textures = new int[texCount];
		GLES30.glGenTextures(texCount, textures, 0);
		if (isGlError("glGenTextures")) {
			nativeInitTexFinish(null);
			return;
		}
		
		int[] maxTextureBufferSize = new int[1];
		GLES31.glGetIntegerv(GLES31Ext.GL_MAX_TEXTURE_BUFFER_SIZE_EXT, maxTextureBufferSize, 0);
		isGlError("glGenTextures");
		Log.v(TAG, "GL_MAX_TEXTURE_BUFFER_SIZE_EXT : " + maxTextureBufferSize[0]);
		
		int[] buffers = new int[texCount * 2];
		GLES30.glGenBuffers(texCount * 2, buffers, 0);
		if (isGlError("glGenBuffers")) {
			nativeInitTexFinish(null);
			return;
		}
		
		int bufferSize = imageSize * imageSize * 4;// size for GL_RGBA8UI
		
		for (int i = 0; i < texCount; i++)
		{
			Log.v(TAG, "glGenTextures id : " + textures[i]);
			
			GLES30.glBindBuffer(GLES31Ext.GL_TEXTURE_BUFFER_EXT, buffers[i*2]);
			isGlError("glBindBuffer");
			
			GLES30.glBufferData(GLES31Ext.GL_TEXTURE_BUFFER_EXT, bufferSize, pixelBuffer, GLES30.GL_DYNAMIC_DRAW);
			isGlError("glBufferData");
			
			GLES30.glBindBuffer(GLES31Ext.GL_TEXTURE_BUFFER_EXT, buffers[i*2 + 1]);
			isGlError("glBindBuffer");
			
			GLES30.glBufferData(GLES31Ext.GL_TEXTURE_BUFFER_EXT, bufferSize, pixelBuffer, GLES30.GL_DYNAMIC_DRAW);
			isGlError("glBufferData");
			
			GLES30.glBindBuffer(GLES31Ext.GL_TEXTURE_BUFFER_EXT, buffers[i*2]);

			// ...and bind it to our array
			GLES30.glBindTexture(GLES31Ext.GL_TEXTURE_BUFFER_EXT, textures[i]);
			if (isGlError("glBindTexture")) continue;

			// Use the Android GLUtils to specify a two-dimensional texture image
			// from our bitmap
			
			if (updateBitmap != null) {
				Buffer byteBuffer = GLES30.glMapBufferRange(GLES31Ext.GL_TEXTURE_BUFFER_EXT, 0, bufferSize, GLES31.GL_MAP_WRITE_BIT);
				isGlError("glMapBufferRange");
				ByteBuffer bBuffer = (ByteBuffer)byteBuffer;
				bBuffer.order(ByteOrder.nativeOrder());
				bBuffer.position(0);
				updateBitmap.copyPixelsToBuffer(bBuffer);
				GLES30.glUnmapBuffer(GLES31Ext.GL_TEXTURE_BUFFER_EXT);
				isGlError("glUnmapBuffer");
			}
			
			GLES31Ext.glTexBufferEXT(GLES31Ext.GL_TEXTURE_BUFFER_EXT, GLES30.GL_RGBA8UI, buffers[i*2]);
			isGlError("glTexBufferEXT");
			GLES30.glBindBuffer(GLES31Ext.GL_TEXTURE_BUFFER_EXT, 0);
			
			TextureBuffer.put(textures[i], buffers[i*2], buffers[i*2 + 1], bufferSize);
		}
		if (updateBitmap != null) {
			updateBitmap.recycle();
			updateBitmap = null;
		}
		
		//GLES30.glBindTexture(GLES31Ext.GL_TEXTURE_BUFFER_EXT, 0);
		isGlError("glGenTextures finish");
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
			nativeUpdateTextureFinish(-1);
			return;
		}
		
		//GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GLES31Ext.GL_TEXTURE_BUFFER_EXT, textureID);
		if (isGlError("updateTexture glBindTexture")) {
			nativeUpdateTextureFinish(0);
			return;
		}
		
		boolean failUpdate = false;
		
		GLES30.glBindBuffer(GLES31Ext.GL_TEXTURE_BUFFER_EXT, TextureBuffer.getBackBuffer(textureID));
		failUpdate |= isGlError("glBindBuffer");
		Buffer byteBuffer = GLES30.glMapBufferRange(GLES31Ext.GL_TEXTURE_BUFFER_EXT, 0, TextureBuffer.getBufferSize(textureID), 
				GLES31.GL_MAP_WRITE_BIT | GLES31.GL_MAP_FLUSH_EXPLICIT_BIT);
		failUpdate |= isGlError("glMapBufferRange");
		
		ByteBuffer bBuffer = (ByteBuffer)byteBuffer;
		bBuffer.order(ByteOrder.nativeOrder());
		bBuffer.position(0);
		updateBitmap.copyPixelsToBuffer(bBuffer);
		
		GLES31.glFlushMappedBufferRange(GLES31Ext.GL_TEXTURE_BUFFER_EXT, 0, TextureBuffer.getBufferSize(textureID));
		failUpdate |= isGlError("glFlushMappedBufferRange");
		GLES30.glUnmapBuffer(GLES31Ext.GL_TEXTURE_BUFFER_EXT);
		failUpdate |= isGlError("glUnmapBuffer");
		
		
		GLES31Ext.glTexBufferEXT(GLES31Ext.GL_TEXTURE_BUFFER_EXT, GLES30.GL_RGBA8UI, TextureBuffer.getBackBuffer(textureID));
		failUpdate |= isGlError("glTexBufferEXT");
		
		GLES30.glBindBuffer(GLES31Ext.GL_TEXTURE_BUFFER_EXT, 0);
		failUpdate |= isGlError("updateTexture finish");
		if (failUpdate) {
			nativeUpdateTextureFinish(0);
		}else {
			nativeUpdateTextureFinish(textureID);
		}
		
		TextureBuffer.switchBuffer(textureID);
		
		//GLES30.glBindTexture(GLES31Ext.GL_TEXTURE_BUFFER_EXT, 0);
		
		updateBitmap.recycle();
		updateBitmap = null;
		byteBuffer = null;
	}
	
}

class TextureBuffer {
	
	private static HashMap<Integer, TextureBuffer> textureBufferMap = new HashMap<Integer, TextureBuffer>();
	
	public static int getActiveBuffer(int textureID) {
		if (textureBufferMap.containsKey(textureID)) {
			return textureBufferMap.get(textureID).getActive();
		}
		return 0;
	}
	
	public static int getBackBuffer(int textureID) {
		if (textureBufferMap.containsKey(textureID)) {
			return textureBufferMap.get(textureID).getBack();
		}
		return 0;
	}
	
	public static void switchBuffer(int textureID) {
		if (textureBufferMap.containsKey(textureID)) {
			textureBufferMap.get(textureID).switchBuffer();
		}
	}
	
	public static int getBufferSize(int textureID) {
		if (textureBufferMap.containsKey(textureID)) {
			return textureBufferMap.get(textureID).getSize();
		}
		return 0;
	}
	
	public static void put(int textureID, int activeBuffer, int backBuffer, int bufferSize) {
		if (textureBufferMap.containsKey(textureID)) {
			textureBufferMap.get(textureID).resetBuffers(activeBuffer, backBuffer, bufferSize);
		} else {
			TextureBuffer tb = new TextureBuffer(activeBuffer, backBuffer, bufferSize);
			textureBufferMap.put(textureID, tb);
		}
	}
	
	private int activeBuffer;
	private int backBuffer;
	private int bufferSize;
	
	private TextureBuffer(int buffer1, int buffer2, int size) {
		activeBuffer = buffer1;
		backBuffer = buffer2;
		bufferSize = size;
	}
	
	private void resetBuffers(int buffer1, int buffer2, int size) {
		activeBuffer = buffer1;
		backBuffer = buffer2;
		bufferSize = size;
	}
	
	private void switchBuffer() {
		int tmpBuffer = activeBuffer;
		activeBuffer = backBuffer;
		backBuffer = tmpBuffer;
	}
	
	private int getActive() {
		return activeBuffer;
	}
	
	private int getBack() {
		return backBuffer;
	}
	
	private int getSize() {
		return bufferSize;
	}
	
}
