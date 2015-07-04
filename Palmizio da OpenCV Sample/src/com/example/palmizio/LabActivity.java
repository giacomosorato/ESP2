package com.example.palmizio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.palmizio.R;
import com.example.palmizio.GoogleResActivity;
//jpeg.embedded@gmail.com
//•••••••• (fantozzi??)
public final class LabActivity extends ActionBarActivity {
	
	//classe privata che usa un thread proprio per uploadare l'img:
	private class UploadImg extends AsyncTask<String, Void, String>  {

		@Override
		protected void onPreExecute() {
		super.onPreExecute();
		mProgressDialog = new ProgressDialog(LabActivity.this);
		mProgressDialog.setTitle("caricamento foto attendere");
		mProgressDialog.setMessage("Loading...");
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.show();
		}
			
		protected String doInBackground(String... indirizzo) {
			
			String imgUrl = null;
			File toUpload = new File(indirizzo[0]);
			
			Map<String, String> config = new HashMap();
            config.put("cloud_name", "jpegembedded");
            config.put("api_key", "652944918467885");
            config.put("api_secret", "NGXgbYVSw6sqUu_tFaJIzBvrL8c");
            Cloudinary cloudinary = new Cloudinary(config);
            Map uploadResult = null;
            try {
            	uploadResult = cloudinary.uploader().upload(toUpload, ObjectUtils.emptyMap());
            } catch (IOException e) {
                e.printStackTrace();
            }
            imgUrl = (String) uploadResult.get("url");

    		
			return imgUrl;
		}
		
		protected void onPostExecute(String path) {
	         
			setPath(path);
			TextView txt = (TextView) findViewById(R.id.text);
			txt.setText(path);
			mProgressDialog.dismiss();
			
			new Guess().execute();

	     }

	}
	
	private class Guess extends AsyncTask<Void, Void, Void> {
		String best;
		String photoPath;
		Bitmap bm;
		//boolean guardia = true;

		@Override
		protected void onPreExecute() {
		super.onPreExecute();
		mProgressDialog = new ProgressDialog(LabActivity.this);
		mProgressDialog.setTitle("attendendo google");
		mProgressDialog.setMessage("Loading...");
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			
			String google = "https://www.google.com/searchbyimage?&image_url=";
			String url = google+imgUrl;
			
			Document doc = null;
			try {
				doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.76 Safari/537.36").get();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//trovo il nodo che racchiude la stringa del best guess (classe _gUb, trovata ispezionando a mano la pagina sorgente):
			
			Elements bestguess = doc.select("a._gUb");
			
			//if (bestguess == null)
				//{
					//Log.e(TAG, "guess non trovata");
					//best="guess non trovata";
					//guardia = false;

			//else			
			//{
				//ricavo la stringa
				best = bestguess.text();
				if (best.length() < 1) best = "no Guess found";
			//}
				
			
			
			////////////////////////////////////////////////////////////////////////////////////
			
			//if (guardia==true)
			//{
				//ora ricavo dal doc l'url della pagina contenente tutte le immagini simili:
				//il nome della classe è stato ottenuto sempre ispezionando a mano il doc sorgente
				Elements linkBestImgs = doc.select("div._Icb._kk._wI > a");
				String link = linkBestImgs.attr("href");
				String url2 = "https://www.google.it/"+link;
				
				Document doc2 = null;
				try {
					doc2 = Jsoup.connect(url2).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.76 Safari/537.36").get();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "ci metto troppo a raggiungere url2");
					e.printStackTrace();
				}
				
				//ispeziono il primo risultato e ricavo il link all'immagine:
				Element masthead = doc2.select("#rg_s > div:nth-child(1) > a").first();
				String mastheadLinkString = masthead.attr("href");
				
				//ora devo parsare la stringa dal carattere successivo al primo '=' fino alla prima occorrenza di '.jpg' inclusa
			    int indexname = mastheadLinkString.indexOf("=");
			    indexname++;
			    int indexname2 = mastheadLinkString.indexOf("&");
			    mastheadLinkString = mastheadLinkString.substring(indexname, indexname2);
			    //formatto la stringa: se il nome del file contiene spazi, questi vengono codificati
			    //con la sequenza "%252520". devo sostituire con %20 (trovato copiando il link dal browswr ad un file di testo):
			    String replacedStr = mastheadLinkString.replaceAll("%252520", "%20");
			    
			    // Determine the path and metadata for the photo.
	
		        //final String photoPath = albumPath + File.separator + currentTimeMillis + LabActivity.PHOTO_FILE_EXTENSION;
		        photoPath = getExternalFilesDir(null).toString() + File.separator + "GuessImg.jpg";
		        
		        
		        //infine salvo l'immagine:
			      
				URL indirizzo = null;
				
				//creazione dell'URL indirizzo
				try {
					indirizzo = new URL(replacedStr);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "eccezione nella creazione dell'URL");
					e.printStackTrace();
				}
				
				InputStream is = null;
				
				// apertura stream su indirizzo
				try {
					is = indirizzo.openStream();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "eccezione nell'apertura stream input");
					e.printStackTrace();
				}
				
				OutputStream os = null;
				
				// apertura stream output su os
				try {
					os = new FileOutputStream(photoPath);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "eccezione nell'apertura stream output");
					e.printStackTrace();
				}
	
				byte[] b = new byte[2048];
				int length;
	
				// leggi dall'output tutto ciò che c'è da leggere e scrivilo su b
				try {
					while ((length = is.read(b)) != -1) {
						os.write(b, 0, length);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "eccezione nella lettura dell'output");
					e.printStackTrace();
				}
	
				// chiudi tutti i stream
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "eccezione nella chiusura dello stream1");
					e.printStackTrace();
				}
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "eccezione nella chiusura dello stream2");
					e.printStackTrace();
				}
				
				bm = BitmapFactory.decodeFile(photoPath);
			//}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
		/*	
			// Set title into TextView
			TextView txttitle = (TextView) findViewById(R.id.text);
			txttitle.setText(best);

			try{
				ImageView im = (ImageView) findViewById(R.id.image);
				im.setImageBitmap(bm);
			} catch (Exception e){
				Log.d(TAG, "eccezione nell'inserimento dell'immagine dell'ImageView");
			}

			mProgressDialog.dismiss();
		*/
			
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(),GoogleResActivity.class);
			intent.putExtra("path",photoPath);	//passo il path della foto salvata
			intent.putExtra("guess",best);		//passo la stringa del best guess
			
			mProgressDialog.dismiss();
			
			startActivity(intent);
			
		}		
	}
	
	// Title AsyncTask
	private class Google extends AsyncTask<String, Void, Void> {
		String best;
		String photoPath;
		Bitmap bm;


		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(LabActivity.this);
			mProgressDialog.setTitle("Google Img Search");
			mProgressDialog.setMessage("Loading...");
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.show();
		}

		@Override
		protected Void doInBackground(String... indirizzo) {
			
			String imgUrl = null;
			File toUpload = new File(indirizzo[0]);
			
			Map<String, String> config = new HashMap();
            config.put("cloud_name", "jpegembedded");
            config.put("api_key", "652944918467885");
            config.put("api_secret", "NGXgbYVSw6sqUu_tFaJIzBvrL8c");
            Cloudinary cloudinary = new Cloudinary(config);
            Map uploadResult = null;
            try {
            	uploadResult = cloudinary.uploader().upload(toUpload, ObjectUtils.emptyMap());
            } catch (IOException e) {
                e.printStackTrace();
            }
            imgUrl = (String) uploadResult.get("url");
			
			String google = "https://www.google.com/searchbyimage?&image_url=";
			String url = google+imgUrl;
			
			Document doc = null;
			try {
				doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.76 Safari/537.36").get();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//trovo il nodo che racchiude la stringa del best guess (classe _gUb, trovata ispezionando a mano la pagina sorgente):
			
			Elements bestguess = doc.select("a._gUb");
	
			//ricavo la stringa	
			best = bestguess.text();	
			if (best.length() < 1) best = "no Guess found";

				//ora ricavo dal doc l'url della pagina contenente tutte le immagini simili:
				//il nome della classe è stato ottenuto sempre ispezionando a mano il doc sorgente
				Elements linkBestImgs = doc.select("div._Icb._kk._wI > a");
				String link = linkBestImgs.attr("href");
				String url2 = "https://www.google.it/"+link;
				
				Document doc2 = null;
				try {
					doc2 = Jsoup.connect(url2).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.76 Safari/537.36").get();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "ci metto troppo a raggiungere url2");
					e.printStackTrace();
				}
				
				//ispeziono il primo risultato e ricavo il link all'immagine:
				Element masthead = doc2.select("#rg_s > div:nth-child(1) > a").first();
				String mastheadLinkString = masthead.attr("href");
				
				//ora devo parsare la stringa dal carattere successivo al primo '=' fino alla prima occorrenza di '.jpg' inclusa
			    int indexname = mastheadLinkString.indexOf("=");
			    indexname++;
			    int indexname2 = mastheadLinkString.indexOf("&");
			    mastheadLinkString = mastheadLinkString.substring(indexname, indexname2);
			    //formatto la stringa: se il nome del file contiene spazi, questi vengono codificati
			    //con la sequenza "%252520". devo sostituire con %20 (trovato copiando il link dal browswr ad un file di testo):
			    String replacedStr = mastheadLinkString.replaceAll("%252520", "%20");
			    
			    // Determine the path and metadata for the photo.
	
		        //final String photoPath = albumPath + File.separator + currentTimeMillis + LabActivity.PHOTO_FILE_EXTENSION;
		        photoPath = getExternalFilesDir(null).toString() + File.separator + "GuessImg.jpg";
		        
		        
		        //infine salvo l'immagine:
			      
				URL indirizzo2 = null;
				
				//creazione dell'URL indirizzo
				try {
					indirizzo2 = new URL(replacedStr);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "eccezione nella creazione dell'URL");
					e.printStackTrace();
				}
				
				InputStream is = null;
				
				// apertura stream su indirizzo
				try {
					is = indirizzo2.openStream();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "eccezione nell'apertura stream input");
					e.printStackTrace();
				}
				
				OutputStream os = null;
				
				// apertura stream output su os
				try {
					os = new FileOutputStream(photoPath);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "eccezione nell'apertura stream output");
					e.printStackTrace();
				}
	
				byte[] b = new byte[2048];
				int length;
	
				// leggi dall'output tutto ciò che c'è da leggere e scrivilo su b
				try {
					while ((length = is.read(b)) != -1) {
						os.write(b, 0, length);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "eccezione nella lettura dell'output");
					e.printStackTrace();
				}
	
				// chiudi tutti i stream
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "eccezione nella chiusura dello stream1");
					e.printStackTrace();
				}
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "eccezione nella chiusura dello stream2");
					e.printStackTrace();
				}
				
				bm = BitmapFactory.decodeFile(photoPath);
			//}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
/*
			// Set title into TextView
			TextView txttitle = (TextView) findViewById(R.id.text);
			txttitle.setText(best);

			try
			{
				ImageView im = (ImageView) findViewById(R.id.image);
				im.setImageBitmap(bm);
			} catch (Exception e){
				Log.d(TAG, "eccezione nell'inserimento dell'immagine dell'ImageView");
			}
*/
			// Open the photo in LabActivity.
	        //final Intent intent = new Intent(this, GoogleResActivity.class);
			
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(),GoogleResActivity.class);
			intent.putExtra("path",photoPath);	//passo il path della foto salvata
			intent.putExtra("guess",best);		//passo la stringa del best guess
			
			mProgressDialog.dismiss();
			
			startActivity(intent);
			
			
		}
	}
	
	//per debug, da cancellare:
	//String url = "http://www.androidbegin.com";

	private long matAddr;
	
	private Mat tempImg;
	private Mat img;
	private Bitmap bm;
	
	private static final String TAG = LabActivity.class.getSimpleName();
	
	private String imgUrl;
	
	private Button bu;
//	private Button buS;
//	private Button buViewDB;
	
//	private Button bu1;
//	private Button bu2;
	private TextView tv;
	private ImageView im;
	private ImageView im2;
	
	private ProgressDialog mProgressDialog;
	private DBHelper db;
	public void setPath(String indirizzo){
		imgUrl = indirizzo;
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
        	Log.d(TAG, "verifico LoaderCallbackInterface.SUCCESS - status = " + status );
            if (status == LoaderCallbackInterface.SUCCESS ) {
                // now we can call opencv code !
            	
            	// Load native library after(!) OpenCV initialization
            	try {
                    //System.loadLibrary("mixed_sample");
                	Log.d(TAG, "classe sorg caricata");
                } catch (UnsatisfiedLinkError e) {
                	Log.d(TAG, "classe sorg non caricata");
                }
            	
            } else {
                super.onManagerConnected(status);
            }
        }
    };
   
	@Override
    protected void onPause() {
        super.onPause();
        if (db!=null)
        	db.close();
	}
	@Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        db = new DBHelper(this);
        if (db.numberOfPictures() == 0) db.init();
        
        
        setContentView(R.layout.lab_layout);
        
        bu = (Button) findViewById(R.id.button);
//        buS = (Button) findViewById(R.id.buttonStorico);
//        buViewDB = (Button) findViewById(R.id.buttonDB);

//        bu1 = (Button) findViewById(R.id.button1);
//        bu2 = (Button) findViewById(R.id.button2);
        tv = (TextView) findViewById(R.id.text);
        im = (ImageView) findViewById(R.id.image);
        im2 = (ImageView) findViewById(R.id.image2);

        
        /////OCCHIO!!!! solo per debug(lasciare commentato!):
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);
        /////////////////////

        final Intent intent = getIntent();
        
        final String indirizzo = intent.getStringExtra("path");
        
        File imgFile = new  File(indirizzo);
        if(imgFile.exists()){
        	//leggo l'immagine:
        	Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        	
        	///* ELABORAZIONE JNI
        	//creo la matrice Mat e converto la bitmap:
        	Mat ImageMat = new Mat ( myBitmap.getHeight(), myBitmap.getWidth(), CvType.CV_8U, new Scalar(4));
        	Utils.bitmapToMat(myBitmap, ImageMat);
        	
        	//matrice 2 (esperimento, conterrà la versione gray)
        	//Mat mGray = new Mat ( myBitmap.getHeight(), myBitmap.getWidth(), CvType.CV_8U, new Scalar(4));
        	//Mat mGray = new Mat();
        	
        	//converto ImageMat in gray:
        	//Imgproc.cvtColor(ImageMat, mGray, Imgproc.COLOR_RGB2GRAY,4);
        	
    	
        	//invoco il metodo nativo:
        	String match = WrapperC.JFindFreak(ImageMat.getNativeObjAddr());
        	//tv.setText(match);
        	
        	 
        	//estraggo l'indice del quadro trovato:
        	String strIdx = match.substring(match.lastIndexOf(">") + 1);
        	int idx = Integer.parseInt(strIdx);
        	
        	int positionXML = idx + 1; //nel db il contatore parte da 1, mentre l'indice di un quadro parte da 0 
        	
        	if(idx == -1){
        		
        		tv.setText("Quadro non riconosciuto");
        		db.addHistoryEvent(new Timestamp(System.currentTimeMillis()),false, positionXML);
        		db.close();	
        	}
        	else {
        		

        		Image dataFromDB = null;
        		try {
        			dataFromDB = db.getPictureData(positionXML);
        		} catch (Exception e) {
        			Log.d(TAG, "il try è fallito");
        			e.printStackTrace();
        		}
        		

        		//chiamo il metodo per avere il path e il nome corrispondente a idx:

        		String pathImg = dataFromDB.getLocalPath();
        		String nameImg = dataFromDB.getGuessName();
        		
        		db.addHistoryEvent(new Timestamp(System.currentTimeMillis()),false, positionXML);
        		
        		db.close();

        		tv.setText(nameImg);

        		File imgFile2 = new  File(pathImg);
        		Bitmap myBitmap2 = BitmapFactory.decodeFile(imgFile2.getAbsolutePath());
        		im2.setImageBitmap(myBitmap2);    	
			}
        	
        		Bitmap outBitmap= Bitmap.createBitmap(ImageMat.width(), ImageMat.height(), Config.ARGB_8888);
        		//riconverto ImageMat in una bitmap e la mostro:
        		Utils.matToBitmap(ImageMat, outBitmap);

        		//FINE ELABORAZIONE JNI*/ 

        		//im.setImageBitmap(outBitmap);
        		im.setImageBitmap(outBitmap);
        	
        	//final TextView tv = new TextView(this);
    		//tv.setText("press button");    		
    		//Button bu = new Button(this);
    		//bu.setText("premimi");
/*
			buViewDB.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					// chiamata all'Activiti Storico:
					final Intent intent = new Intent();
					intent.setClass(getApplicationContext(), ViewDBActivity.class);

					// intent.putExtra("path",photoPath);
					startActivity(intent);

				}
			});

			buS.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					// chiamata all'Activiti Storico:
					final Intent intent = new Intent();
					intent.setClass(getApplicationContext(), HistoryActivity.class);

					// intent.putExtra("path",photoPath);
					startActivity(intent);

				}
			});
*/
			bu.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					new UploadImg().execute(indirizzo);

				}
			});
    		
 /*   		
    		bu1.setOnClickListener(new View.OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				new Guess().execute();
    				//new Title().execute();
    			}
    		});

    		bu2.setOnClickListener(new View.OnClickListener() {
    			@Override
    			public void onClick(View v) {
 
    				//WrapperC.JTrain();
    				new Google().execute(indirizzo);
    			}
    		});
*/        	
        	//final ImageView imageView = new ImageView(this);
            //im.setImageBitmap(myBitmap);
            
            //LinearLayout myLayout = new LinearLayout(this);
            //myLayout.addView(imageView);
            //myLayout.addView(tv);
            //myLayout.addView(bu);

            //setContentView(myLayout);
            
            
            //GoogleGuess.guess(percorsoFile, destinazioneFile);
            
            
        }

       
       /* ROBA VECCHIA ENRICO
        matAddr = (long)intent.getLongExtra("myImg", 0);
        String indirizzo = intent.getStringExtra("string");
//        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("BitmapImage");
        
        String strLong = Long.toString(matAddr);
        
        
        //Cloning Mat in child is necessary
        //since parent activity could be killed any time
        //after onResume of child activity is completed:
        tempImg = new Mat(matAddr);
        img = tempImg.clone(); 
        
        // convert to bitmap:
        if(img.cols() > 0 && img.rows() > 0) {
        bm = Bitmap.createBitmap(img.cols(), img.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bm);
        }
        else {
        	Mat m = Mat.zeros(100,400, CvType.CV_8UC3);
            Core.putText(m, strLong, new Point(30,80), Core.FONT_HERSHEY_SCRIPT_SIMPLEX, 2.2, new Scalar(200,200,0),2);
            // convert to bitmap:
            bm = Bitmap.createBitmap(m.cols(), m.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(m, bm);
        }
       
        
        final ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bm);
        setContentView(imageView);  
        */       	
	}
	
	@Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_lab, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(final MenuItem item) {
		//return super.onOptionsItemSelected(item); 
		return true;//cioè non fa niente
    }
	
	//dichiaro il metodo nativo:
	//public native void FindFeatures(long matAddrGr, long matAddrRgba);
	//public native void FindRect(long matAddrGr, long matAddrRgba);
		
		
	
}
