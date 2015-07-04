package com.example.palmizio;

import java.io.IOException;


import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private Camera mCamera;
	

	// costruttore di base
	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;
		mHolder = getHolder();
		mHolder.addCallback(this);
	}

	
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			// crea il surface e fa partire la camera
			if (mCamera == null) {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			}
		} catch (IOException e) {
			Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	// fa ripartire la preview dopo un'interruzione
	public void refreshCamera(Camera camera) {
		if (mHolder.getSurface() == null) {
			// preview surface non esiste
			return;
		}
		// ferma la preview prima di fare cambiamenti
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}
		// set preview size and make any resize, rotate or
		// reformatting changes here
		// start preview with new settings
		setCamera(camera);
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
		} catch (Exception e) {
			Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
		}
	}
	
    // Chiamato immediatamente dopo ogni cambiamento di struttura che viene fatto nel surface.
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		refreshCamera(mCamera);
	}

	public void setCamera(Camera camera) {
		//metodo per istanziare la camera
		mCamera = camera;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// mCamera.release();

	}
}