package com.example.palmizio;

import java.io.File;
import java.util.List;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.example.palmizio.CameraPreview;
import com.example.palmizio.R;
import com.example.palmizio.LabActivity;
import com.example.palmizio.WrapperC;

@SuppressWarnings("deprecation")
public final class CameraActivity2 extends ActionBarActivity implements
		CvCameraViewListener2 {

	// A tag for log output.
	private static final String TAG = CameraActivity2.class.getSimpleName();

	// variabili di oggetti
	private Camera mCamera;
	private CameraPreview mPreview;
	private PictureCallback mPicture;

	// variabili di layout
	private Button capture;
	private Button buS;
	private Button buDB;
	private Context myContext;
	private LinearLayout cameraPreview;

	private boolean cameraFront = false;

	// database
	private DBHelper db;

	// The OpenCV loader callback.
	/**
	 * Per il caricamento delle librerie opencv dal dispositivo.
	 */
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(final int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
				Log.d(TAG, "OpenCV loaded successfully");
				break;
			default:
				super.onManagerConnected(status);
				break;
			}
		}
	};

	/**
	 * onCreate - inizializza gli elementi di cattura foto inizializza il
	 * database nel caso sia vuoto.
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate start");
		setContentView(R.layout.camera_layout);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		myContext = this;
		initialize();

		db = new DBHelper(this);
		if (db.numberOfPictures() == 0)
			db.init();
	}

	/**
	 * metodo per l'inizializzazione della preview e del bottone di cattura.
	 */
	public void initialize() {
		cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);
		mPreview = new CameraPreview(myContext, mCamera);
		cameraPreview.addView(mPreview);

		capture = (Button) findViewById(R.id.button_capture);
		capture.setOnClickListener(captrureListener);

		buS = (Button) findViewById(R.id.button1000);
		buS.setOnClickListener(captrureListener2);

		buDB = (Button) findViewById(R.id.button2000);
		buDB.setOnClickListener(captrureListener3);
	}

	// definizione dei captureListener - prezzione pulsante -> scatta foto
	/**
	 * Metodo in ascolto sulla premuta di un pulsante. Pulsante premuto: scatta
	 * la foto.
	 */
	OnClickListener captrureListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			// disabilito tutti i bottoni
			// questo per evitare di andare in conflitto con il flusso di
			// codice per la cattura della foto
			capture.setEnabled(false);
			capture.setClickable(false);
			buS.setEnabled(false);
			buS.setClickable(false);
			buDB.setEnabled(false);
			buDB.setClickable(false);

			// prendi la foto
			mCamera.takePicture(null, null, mPicture);

		}
	};

	/**
	 * Metodo in ascolto sulla premuta di un pulsante. Pulsante premuto: intent
	 * per richiamare ViewDBActivity.
	 */
	OnClickListener captrureListener2 = new OnClickListener() {
		@Override
		public void onClick(View v) {

			// disabilito tutti i bottoni
			// questo per evitare di andare in conflitto con il flusso di
			// codice per la cattura della foto
			capture.setEnabled(false);
			capture.setClickable(false);
			buS.setEnabled(false);
			buS.setClickable(false);
			buDB.setEnabled(false);
			buDB.setClickable(false);

			// intent per richiamare l'activity ViewDBActivity
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), ViewDBActivity.class);
			startActivity(intent);

			// riabilito tutti i bottoni
			capture.setEnabled(true);
			capture.setClickable(true);
			buS.setEnabled(true);
			buS.setClickable(true);
			buDB.setEnabled(true);
			buDB.setClickable(true);

		}
	};

	/**
	 * Metodo in ascolto sulla premuta di un pulsante. Pulsante premuto: intent
	 * per richiamare HistoryActivity.
	 */
	OnClickListener captrureListener3 = new OnClickListener() {
		@Override
		public void onClick(View v) {

			// disabilito tutti i bottoni
			// questo per evitare di andare in conflitto con il flusso di
			// codice per la cattura della foto
			capture.setEnabled(false);
			capture.setClickable(false);
			buS.setEnabled(false);
			buS.setClickable(false);
			buDB.setEnabled(false);
			buDB.setClickable(false);

			// intent per richiamare l'activity HistoryActivity
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), HistoryActivity.class);
			startActivity(intent);

			// riabilito tutti i bottoni
			capture.setEnabled(true);
			capture.setClickable(true);
			buS.setEnabled(true);
			buS.setClickable(true);
			buDB.setEnabled(true);
			buDB.setClickable(true);

		}
	};

	/**
	 * onResume - setta i parametri della camera, la apre e lancia la callback
	 * per catturare la foto.
	 */
	@Override
	public void onResume() {
		super.onResume();

		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this,
				mLoaderCallback);

		if (!hasCamera(myContext)) {
			Toast toast = Toast.makeText(myContext,
					"Sorry, your phone does not have a camera!",
					Toast.LENGTH_LONG);
			toast.show();
			finish();
		}
		if (mCamera == null) {
			// apertura della camera
			mCamera = Camera.open(findBackFacingCamera());

			// parametri della camera
			final Camera.Parameters parameters = mCamera.getParameters();
			parameters
					.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			parameters.setPictureFormat(ImageFormat.JPEG);
			parameters.setJpegQuality(100);

			// Per determinare la dimensione della foto salvata
			// usa il meglio che può.
			List<Size> sizes = parameters.getSupportedPictureSizes();
			Camera.Size size = sizes.get(0);
			for (int i = 0; i < sizes.size(); i++) {
				if (sizes.get(i).width > size.width)
					size = sizes.get(i);
			}
			parameters.setPictureSize(size.width, size.height);

			// Per determinare la dimensione della preview
			// usa il meglio che può.
			Camera.Size bestSize = null;
			List<Camera.Size> sizeList = mCamera.getParameters()
					.getSupportedPreviewSizes();
			bestSize = sizeList.get(0);
			for (int i = 1; i < sizeList.size(); i++) {
				if ((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)) {
					bestSize = sizeList.get(i);
				}
			}
			parameters.setPreviewSize(bestSize.width, bestSize.height);

			// per settare tutti i parametri impostati
			mCamera.setParameters(parameters);

			// callback per catturare la foto
			mPicture = getPictureCallback();

			// camera refresh per continuare a vedere la preview
			mPreview.refreshCamera(mCamera);
		}
	}

	/**
	 * Controlla se il device ha la camera
	 * 
	 * @param context
	 * @return boolean
	 */
	private boolean hasCamera(Context context) {
		// check if the device has camera
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Ricerca la fotocamera posteriore
	 * 
	 * @return cameraId, l'id della fotocamera trovata
	 */
	private int findBackFacingCamera() {
		int cameraId = -1;
		// Search for the back facing camera
		// get the number of cameras
		int numberOfCameras = Camera.getNumberOfCameras();
		// for every camera check
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
				cameraId = i;
				cameraFront = false;
				break;
			}
		}
		return cameraId;
	}

	/**
	 * Elaborazione della foto scattata (sistemazione colori RGB,
	 * ridimensionamento, estrazione quadro) e salvataggio dell'immagine
	 * risultante. Chiamata tramite intent di LabActivity.
	 * 
	 * @return Picture
	 */
	private PictureCallback getPictureCallback() {
		final PictureCallback picture = new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				// converto il parametro d'ingresso da byte a bitmap
				// BitmapFactory.Options options = new BitmapFactory.Options();
				// options.inJustDecodeBounds = true;
				Bitmap bmp = BitmapFactory
						.decodeByteArray(data, 0, data.length);

				// converto da bitmap a Mat
				Mat mInput = new Mat();
				Bitmap bmp1 = bmp.copy(Bitmap.Config.ARGB_8888, true);
				Utils.bitmapToMat(bmp1, mInput);

				// ridimensiono l'immagine per migliorare le prestazioni:
				org.opencv.core.Size size = new org.opencv.core.Size(
						mInput.width() / 4, mInput.height() / 4);
				Imgproc.resize(mInput, mInput, size);

				Imgproc.cvtColor(mInput, mInput, Imgproc.COLOR_RGBA2BGR, 3);

				// mat di output
				Mat mOutput = new Mat();

				// chiamo metodo nativo findRect
				WrapperC.JFindRect(mInput.getNativeObjAddr(),
						mOutput.getNativeObjAddr());

				// genero la path dove salvare la foto
				final String photoPath = getExternalFilesDir(null).toString()
						+ File.separator + "TempFile.jpg";

				Toast toast = null;

				// provo a salvare la foto
				if (!Highgui.imwrite(photoPath, mOutput)) {
					toast = Toast.makeText(myContext, "Failed ti take picture",
							Toast.LENGTH_LONG);
					Log.e(TAG, "Failed to save photo to " + photoPath);
					onTakePhotoFailed();
				} else {
					toast = Toast.makeText(myContext, "Picture taked",
							Toast.LENGTH_LONG);
					Log.d(TAG, "Photo saved successfully to " + photoPath);
				}

				// visualizzazione del toast
				toast.show();

				// Apertura della foto in LabActivity
				final Intent intent = new Intent(myContext, LabActivity.class);

				// passagio della path della foto
				intent.putExtra("path", getExternalFilesDir(null).toString()
						+ File.separator + "TempFile.jpg");
				startActivity(intent);

				return;
			}
		};

		// riabilito tutti i bottoni dopo che la foto è stata presa
		capture.setEnabled(true);
		capture.setClickable(true);
		buS.setEnabled(true);
		buS.setClickable(true);
		buDB.setEnabled(true);
		buDB.setClickable(true);

		return picture;
	}

	/**
	 * Rilascio della camera
	 */
	private void releaseCamera() {
		// stop and release camera
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	/**
	 * Metodo chiamato quando si fallisce la cattura di una foto, ritorna un
	 * messaggio di errore in un toast.
	 */
	private void onTakePhotoFailed() {
		// mIsMenuLocked = false;

		// Show an error message.
		final String errorMessage = getString(R.string.photo_error_message);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(CameraActivity2.this, errorMessage,
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * onPause - Chiusura del db e rilascio della camera.
	 */
	@Override
	public void onPause() {
		super.onPause();

		// release database
		if (db != null)
			db.close();

		// when on Pause, release camera in order to be used from other
		// applications
		releaseCamera();
	}

	/**
	 * Dichiarazione del metodo nativo utilizzato - FindRect
	 * 
	 * @param src_p
	 *            , indirizzo di memoria della matrice contenente l'immagine
	 *            sorgente
	 * @param dst_r
	 *            , indirizzo di memoria della matrice contenente l'immagine
	 *            destinazione
	 */
	public native void FindRect(long src_p, long dst_r);

	/**
	 * Non implementato.
	 */
	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
	}

	/**
	 * Non implementato.
	 */
	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
	}

	/**
	 * Non implementato.
	 */
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// TODO Auto-generated method stub
		return null;
	}

}