package com.example.palmizio;

import java.sql.Timestamp;

public class HistoryEvent {
	private int id = -1; //valore di default senza senso !!!
	private Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
	private boolean is_new = false;
	private int id_photo = -1; //valore di default.se -1 deve essere settato il campo photoName
	private String photoName; //e' null se e' settato il campo id_photo
	
	
	public HistoryEvent (int idx, Timestamp ts, boolean new_picture, int id_pict){
		id = idx;
		timeStamp = ts;
		is_new = new_picture;
		id_photo = id_pict;
	}
	
//	//metodo di comodo rimasto per compatibilità codice
//	public HistoryEvent (int idx, Timestamp ts, boolean new_picture, int id_pict, String img){
//		id = idx;
//		timeStamp = ts;
//		is_new = new_picture;
//		id_photo = id_pict;
//		photoName=img;
//	}
	
	public HistoryEvent (int idx, Timestamp ts, boolean new_picture, String img){
		id = idx;
		timeStamp = ts;
		is_new = new_picture;
		photoName=img;
	}
	
	public int get_ID(){
		return id;
	}
	
	public Timestamp getTimeStamp(){
		return timeStamp;
	}
	
	public String getStringTimeStamp (){
		return getTimeStamp().toString();
	}
	
	public boolean getIsNew(){
		return is_new;
	}
	
	public int getIdPhoto(){
		return id_photo;
	}
	
	public String toString(){
		return id + " [" + timeStamp + "] ricercato [" + photoName + "]";
//		return id + " " + timeStamp + " " + (is_new ? "New" : "-") 
//				+ (id_photo == -1 ? "pictureName=" + photoName : " pictureID=" + id_photo);
	}
	

}
//i metodi set non hanno molto senso per adesso...gli oggetti sono solo utili concettualmente
//da implementare in futuro