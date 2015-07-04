package com.example.palmizio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class GoogleResActivity extends ActionBarActivity{
	
	private static final String TAG = GoogleResActivity.class.getSimpleName();
	
	//classe privata che usa un thread proprio per uploadare l'img:
	private class Save extends AsyncTask<Void, Void, Void>  {

		@Override
		protected void onPreExecute() {
		super.onPreExecute();
		mProgressDialog = new ProgressDialog(GoogleResActivity.this);
		mProgressDialog.setTitle("sto estraendo le feature");
		mProgressDialog.setMessage("Loading...");
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.show();
		}
			
		protected Void doInBackground(Void... indirizzo) {
			//salvo le feature
			WrapperC.JAddImg(path);
			
			//salvo l'immagine 
			//File imgFile = new  File(path);
			//creo un nome anti collisione 
			Random generator = new Random();
			int n = 10000;
			n = generator.nextInt(n);
			String fname = "Image-" + n + ".jpg";
			String dir = "/sdcard/Pictures/PalmizioImg/";
			String percorso = dir + fname;
			File imgFile = new  File(percorso);
			
			try {
				FileOutputStream out = new FileOutputStream(imgFile);
				myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
				out.flush();
				out.close();
				} catch (Exception e) {
				e.printStackTrace();
				}
			
			DBHelper db = new DBHelper(getApplicationContext());
			boolean pippo= db.addPicture(bestGuess,percorso);
			db.close();
			Log.i(TAG,"insert into:"+pippo);
			
			return null;
		}
		
		protected void onPostExecute(Void results) {
	         
			//setPath(path);
			TextView txt = (TextView) findViewById(R.id.textGoogle);
			txt.setText("caricamento OK!");
			
			//dopo aver salvato, torno alla camera:
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(),CameraActivity2.class);
			
			mProgressDialog.dismiss();
			
			startActivity(intent);
			
	     }

	}
	
	private Button bu;
	private TextView tv;
	private ImageView im;
	private ProgressDialog mProgressDialog;
	private String path;
	private Bitmap myBitmap;
	private String bestGuess;
	

	@Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.google_layout);
        
        bu = (Button) findViewById(R.id.buttonGoogle);
        tv = (TextView) findViewById(R.id.textGoogle);
        im = (ImageView) findViewById(R.id.imageGoogle);
        
        final Intent intent = getIntent();
        
        path = intent.getStringExtra("path");
        bestGuess = intent.getStringExtra("guess");
        
        tv.setText(bestGuess);
        
        File imgFile = new  File(path);
        if(imgFile.exists())
        {
        	//leggo l'immagine:
        	myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        	im.setImageBitmap(myBitmap);	
        }
        
		bu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				new Save().execute();
			}
		});
        
        
	}
	
	
	

}
