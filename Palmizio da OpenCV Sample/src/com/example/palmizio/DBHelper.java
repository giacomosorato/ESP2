package com.example.palmizio;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SQLiteOpenHelper {

	public static final int DATABASE_VER = 1;
	public static final String DATABASE_NAME = "Palmizio.db";

	public static final String PICTURES_TABLE_NAME = "PICTURES";
	public static final String PICTURES_COLUMN_ID = "idP";
	public static final String PICTURES_COLUMN_NAME = "name";
	//public static final String PICTURES_COLUMN_AUTHOR = "autor"; //non si riesce a capire con decente affidabilita' se parliamo del nome o del cognome
	public static final String PICTURES_COLUMN_PATH = "path";

	public static final String HISTORY_TABLE_NAME = "HISTORY";
	//intero autoincrementato 
	public static final String HISTORY_COLUMN_ID = "idH";

	// campo con la timestamp
	public static final String HISTORY_COLUMN_TIMESTAMP = "timestamp";
	
	// flag che ci identifica l'evento in cui facciamo foto a immagine nuova
	public static final String HISTORY_COLUMN_FLAG_NEW = "flag_new";

	// rappressenta la chiave esterna = chiave primaria tabella pictures !! e'
	// diversa da id photo
	public static final String HISTORY_COLUMN_ID_PHOTO = "id_photo";
	
	private HashMap hp;

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VER);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		///ATTENZIONE: prima devo controllare che il DB non esista già.
		//creo le tabelle solo se non esiste.

		// creo la tabella che conterra' le informazioni sulle immaggini memorizzate
		db.execSQL("CREATE TABLE " + PICTURES_TABLE_NAME + "(" + 
				PICTURES_COLUMN_ID+ " integer primary key,"+
				PICTURES_COLUMN_NAME + " text,"+
				//PICTURES_COLUMN_AUTHOR + " text,"+
				PICTURES_COLUMN_PATH + " text);");

		db.execSQL("CREATE TABLE " + HISTORY_TABLE_NAME + "(" + 
				HISTORY_COLUMN_ID + " integer primary key,"+
				HISTORY_COLUMN_TIMESTAMP + " text,"+
				HISTORY_COLUMN_FLAG_NEW + " integer check ("+HISTORY_COLUMN_FLAG_NEW+">= 0 AND " + HISTORY_COLUMN_FLAG_NEW + " <= 1)," +
				HISTORY_COLUMN_ID_PHOTO + " integer, FOREIGN KEY (" + HISTORY_COLUMN_ID_PHOTO + ") REFERENCES " + PICTURES_TABLE_NAME + "(" + PICTURES_COLUMN_ID + "));");	
	
	
		//init();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//l'evento di upgrade del DB non e' contemplato per ora. 
		//il DB per il momento restera' nella forma attuale
		
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + PICTURES_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE_NAME);
		onCreate(db);

	}

	/*
	 * metodo che aggiunge un record nella tabella delle imagini note
	 * */
	public boolean addPicture(String best_guess, String picture_path) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(PICTURES_COLUMN_NAME, best_guess);
		contentValues.put(PICTURES_COLUMN_PATH, picture_path);
		
		long ris = -1;
		try {
			ris = db.insert(PICTURES_TABLE_NAME, null, contentValues);
		} catch (Exception e) {
			// TODO assegnare azioni per eccezzione
			db.close();
			return false;
		}
		db.close();
		return (ris == -1 ? false : true);
	}
	
	/*
	 * metodo che aggiunge un record nella tabella delle imagini note
	 * */
	public boolean addHistoryEvent(Timestamp ts, boolean isNew, int idPhoto) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(HISTORY_COLUMN_TIMESTAMP, ts.toString());
		contentValues.put(HISTORY_COLUMN_FLAG_NEW, isNew);
		contentValues.put(HISTORY_COLUMN_ID_PHOTO, idPhoto);
		
		long ris = -1;
		try{
			ris = db.insert(HISTORY_TABLE_NAME, null, contentValues);
		}catch(Exception e){
			//TODO assegnare  azioni per eccezzione
			return false;
		}
		return (ris == -1 ? false : true);
	}

	

	public Image getPictureData(int idx) throws Exception{ //TODO definire il tipo d'eccezzione per questo errore
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.rawQuery("select * from "+ PICTURES_TABLE_NAME +" where "+PICTURES_COLUMN_ID+"=" + idx + ";", null);
		
		if(res.getCount() != 1){// non ho elementi in uscita dalla query : id errato oppure BUG nella logica
			throw new Exception("Il risultato della query e' nullo oppure sono stare ritrovate molteplici record.");
		}


		res.moveToFirst();

		return new Image(res.getInt(res.getColumnIndex(PICTURES_COLUMN_ID)),
				res.getString(res.getColumnIndex(PICTURES_COLUMN_NAME)),
				res.getString(res.getColumnIndex(PICTURES_COLUMN_PATH)));
	}


	// ritorna la query di selezione eventi dello storico
	public HistoryEvent getHistoryEventData(int id) throws Exception{ //TODO definire il tipo d'eccezzione per questo errore
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.rawQuery("select * from "+ HISTORY_TABLE_NAME +" where "+HISTORY_COLUMN_ID+"=" + id + ";", null);
		
		if(res.getCount() != 1){// non ho elementi in uscita dalla query : id errato oppure BUG nella logica
			throw new Exception("Il risultato della query e' nullo oppure sono stare ritrovate molteplici record.");
		}		
		
		res.moveToFirst();

		return new HistoryEvent(res.getInt(res.getColumnIndex(HISTORY_COLUMN_ID)),
				Timestamp.valueOf(res.getString(res.getColumnIndex(HISTORY_COLUMN_TIMESTAMP))),
				(1 == res.getInt(res.getColumnIndex(HISTORY_COLUMN_FLAG_NEW))),
				res.getInt(res.getColumnIndex(HISTORY_COLUMN_ID_PHOTO)));
	}
	
	public int numberOfPictures() {
		SQLiteDatabase db = this.getReadableDatabase();
		int numRows = (int) DatabaseUtils.queryNumEntries(db, PICTURES_TABLE_NAME);
		return numRows;
	}
	public int numberOfHistoryEvents() {
		SQLiteDatabase db = this.getReadableDatabase();
		int numRows = (int) DatabaseUtils.queryNumEntries(db, HISTORY_TABLE_NAME);
		return numRows;
	}
		
/*	codice di esempio

	public Integer deleteContact(Integer id) {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete("contacts", "id = ? ", new String[] { Integer.toString(id) });
	}
*/
	public ArrayList<HistoryEvent> getAllHistory() {
		ArrayList<HistoryEvent> array_list = new ArrayList<HistoryEvent>();


		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.rawQuery("select * from " + HISTORY_TABLE_NAME + " left outer  join " + PICTURES_TABLE_NAME
				+ " on " + HISTORY_TABLE_NAME + "." + HISTORY_COLUMN_ID_PHOTO + "=" + PICTURES_TABLE_NAME + "."
				+ PICTURES_COLUMN_ID+";", null);
		res.moveToFirst();

		while (res.isAfterLast() == false) {
			array_list.add(new HistoryEvent(res.getInt(res.getColumnIndex(HISTORY_COLUMN_ID)),
					Timestamp.valueOf(res.getString(res.getColumnIndex(HISTORY_COLUMN_TIMESTAMP))),
					(1 == res.getInt(res.getColumnIndex(HISTORY_COLUMN_FLAG_NEW))),
					res.getString(res.getColumnIndex(PICTURES_COLUMN_NAME))));
			res.moveToNext();
		}
		return array_list;
	}
	
	/*metodo utile per testing*/
	public ArrayList<String> getAllHistoryStrings() {
		ArrayList<String> array_list = new ArrayList<String>();
		
		ArrayList<HistoryEvent> evList = getAllHistory();
		
		for (HistoryEvent he : evList) {
			array_list.add(he.toString());
		}
		
		return array_list;
		
	}
	
// tag inizio
	
	public ArrayList<Image> getAllImages() {
		ArrayList<Image> array_list = new ArrayList<Image>();


		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.rawQuery("select * from " + PICTURES_TABLE_NAME, null);
		res.moveToFirst();

		while (res.isAfterLast() == false) {
			array_list.add(
					new Image(
							res.getInt(res.getColumnIndex(PICTURES_COLUMN_ID)),
							res.getString(res.getColumnIndex(PICTURES_COLUMN_NAME)),
							res.getString(res.getColumnIndex(PICTURES_COLUMN_PATH)))
					);
			res.moveToNext();
		}
		return array_list;
	}
	
	/*metodo utile per testing*/
	public ArrayList<String> getAllImagesStrings() {
		ArrayList<String> array_list = new ArrayList<String>();
		
		ArrayList<Image> imList = getAllImages();
		
		for (Image img : imList) {
			array_list.add(img.toString());
		}
		
		return array_list;
		
	}	
	
// tag fine

	
	
	public boolean init(){
		
		boolean okkei = false;
		
		okkei= (
				addPicture("Urlo di Munch","/sdcard/Pictures/PalmizioImg/urlo.jpg") &&
				addPicture("Gioconda Da Vinci","/sdcard/Pictures/PalmizioImg/gioconda.jpg") &&
				addPicture("Danza Matisse","/sdcard/Pictures/PalmizioImg/danza.jpg") &&
				addPicture("Notte Van Gogh","/sdcard/Pictures/PalmizioImg/notte.jpg") &&
				addPicture("Guernica Picasso","/sdcard/Pictures/PalmizioImg/picasso.jpg") &&
				addPicture("bacio Heinz","/sdcard/Pictures/PalmizioImg/bacio.jpg") &&
				addPicture("American Gothic","/sdcard/Pictures/PalmizioImg/coppia.jpg") &&
				addPicture("Salvador Dalì","/sdcard/Pictures/PalmizioImg/orologi.jpg") &&
				addPicture("Puntinismo","/sdcard/Pictures/PalmizioImg/puntini.jpg") &&
				addPicture("Venere Botticelli","/sdcard/Pictures/PalmizioImg/venere.jpg") &&
				addPicture("Campo di grano Van Gogh","/sdcard/Pictures/PalmizioImg/campo.jpg") &&
				addPicture("Renè Magritte","/sdcard/Pictures/PalmizioImg/mela.jpg") &&
				addPicture("Deposizione Mantegna","/sdcard/Pictures/PalmizioImg/cristo.jpg") &&
				addPicture("Trionfo di Galatea","/sdcard/Pictures/PalmizioImg/angeli.jpg") &&
				addPicture("Primavera","/sdcard/Pictures/PalmizioImg/primavera.jpg") &&
				addPicture("Iris Van Gogh","/sdcard/Pictures/PalmizioImg/iris.jpg") &&
				addPicture("Venere (2)","/sdcard/Pictures/PalmizioImg/venere2.jpg") &&
				addPicture("Il quarto stato","/sdcard/Pictures/PalmizioImg/stato.jpg") &&
				addPicture("La libertà che guida il Popolo","/sdcard/Pictures/PalmizioImg/francia.jpg") &&
				addPicture("La madre","/sdcard/Pictures/PalmizioImg/bean.jpg") &&
				addPicture("Platone e Aristotele","/sdcard/Pictures/PalmizioImg/filosofi.jpg") &&
				addPicture("Bacco","/sdcard/Pictures/PalmizioImg/bacco.jpg")
				);


		return okkei;
		
		
	}
}
