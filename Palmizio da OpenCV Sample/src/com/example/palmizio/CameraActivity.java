package com.example.palmizio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.example.palmizio.LabActivity;
import com.example.palmizio.WrapperC;
//import com.nummist.secondsight.R;
//import com.nummist.secondsight.LabActivity;
//import com.nummist.secondsight.CameraActivity;
//import com.nummist.secondsight.R;



public final class CameraActivity extends ActionBarActivity implements CvCameraViewListener2 {
	
	// A matrix that is used when saving photos.
    private Mat mBgr;
    
    // The image sizes supported by the active camera.
    private List<Size> mSupportedImageSizes;
    
    // The index of the active image size.
    private int mImageSizeIndex;
	
	// A tag for log output.
    private static final String TAG = CameraActivity.class.getSimpleName();
	
	// The camera view.
    private CameraBridgeViewBase mCameraView;
    
    //guardia che diventa true quando schiaccio il pulsante foto
    private boolean mIsPhotoPending;
	
	// The OpenCV loader callback.
    private BaseLoaderCallback mLoaderCallback =
            new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(final int status) {
            switch (status) {
            case LoaderCallbackInterface.SUCCESS:
                Log.d(TAG, "OpenCV loaded successfully");
                mCameraView.enableView();
                //mCameraView.enableFpsMeter();
                mBgr = new Mat();
                break;
            default:
                super.onManagerConnected(status);
                break;
            }
        }
    };
    
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        final Camera camera;
        camera = Camera.open();
        final Parameters parameters = camera.getParameters();
        //molto importante:
        camera.release();
        
        mSupportedImageSizes = parameters.getSupportedPreviewSizes();
        final Size size = mSupportedImageSizes.get(mImageSizeIndex);
        
        mCameraView = new JavaCameraView(this, 0);
        mCameraView.setMaxFrameSize(size.width, size.height);
        mCameraView.setCvCameraViewListener(this);
        setContentView(mCameraView);
        
    }
    
    @Override
    public void onPause() {
        if (mCameraView != null) {
            mCameraView.disableView();
        }
        super.onPause();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
    }
	
    @Override
    public void onDestroy() {
        if (mCameraView != null) {
            mCameraView.disableView();
        }
        super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_camera, menu);

        return true;
    }
    
    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
    	int itemId = item.getItemId();
		if (itemId == R.id.menu_take_photo) {
			// Next frame, take the photo.
            mIsPhotoPending = true;
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
    	
    	
    	
    }
    
    @Override
    public void onCameraViewStarted(final int width,final int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }
    
    @Override
    public Mat onCameraFrame(final CvCameraViewFrame inputFrame) {
        final Mat rgba = inputFrame.rgba();
        
        if (mIsPhotoPending) {
            mIsPhotoPending = false;
            //show(rgba);
            takePhoto(rgba);
        }
        
        return rgba;
    }
    
    private void show(final Mat rgba) {
    	
    	long addr = rgba.getNativeObjAddr();
    	// Open the photo in LabActivity.
        final Intent intent = new Intent(this, LabActivity.class);
        intent.putExtra( "myImg", addr );
        
        //passo una string per debug:
        String strLong = Long.toString(addr);
        intent.putExtra( "string", strLong );
/*        
        //passo bitmap per debug:
        Bitmap bm = Bitmap.createBitmap(rgba.cols(), rgba.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, bm);
        intent.putExtra("BitmapImage", bm);
*/        
        startActivity(intent);
    	
    }
         
    private void takePhoto(final Mat rgba) {
	
    	//System.loadLibrary("mixed_sample");
    	
		//matrice 2 (esperimento, conterra'� la versione gray)
		Mat mOutput = new Mat();
		
		//invoco il metodo nativo:
		WrapperC.JFindRect(rgba.getNativeObjAddr(), mOutput.getNativeObjAddr());
		//JFindRect(rgba.getNativeObjAddr(), mOutput.getNativeObjAddr());
	
        // Determine the path and metadata for the photo.

        //final String photoPath = albumPath + File.separator + currentTimeMillis + LabActivity.PHOTO_FILE_EXTENSION;
        final String photoPath = getExternalFilesDir(null).toString() + File.separator + "TempFile.jpg";
        
        // Try to create the photo.
        Imgproc.cvtColor(mOutput, mBgr, Imgproc.COLOR_RGBA2BGR, 3);
        if (!Highgui.imwrite(photoPath, mBgr)) {
            Log.e(TAG, "Failed to save photo to " + photoPath);
            onTakePhotoFailed();
        }
        Log.d(TAG, "Photo saved successfully to " + photoPath);
        
        
        // Open the photo in LabActivity.
        final Intent intent = new Intent(this, LabActivity.class);
        
        intent.putExtra("path",photoPath);
        startActivity(intent);
    }
    
    private void onTakePhotoFailed() {
        //mIsMenuLocked = false;
        
        // Show an error message.
        final String errorMessage =
                getString(R.string.photo_error_message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CameraActivity.this, errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
	//dichiaro il metodo nativo:
	//public native void FindFeatures(long matAddrGr, long matAddrRgba);
	public native void FindRect(long src_p, long dst_r);
    

	
}