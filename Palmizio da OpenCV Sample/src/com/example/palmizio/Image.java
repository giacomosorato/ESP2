package com.example.palmizio;

public class Image {
	private int id = -1;
	private String guess_name = null;
	private String local_path = null;

	public Image(int idx, String name, String path) {
		id = idx;
		guess_name = name;
		local_path = path;
	}

	public int getID() {
		return id;
	}
	
	public String getGuessName() {
		return guess_name;
	}
	
	public String getLocalPath(){
		return local_path;
	}
	
	public String toString() {
		return (id + " - " + guess_name + " [" + local_path + "]");		
	}
	
//i metodi set non hanno molto senso per adesso...gli oggetti sono solo utili concettualmente
//da implementare in futuro
	
/*	public boolean setID(int idx) {
		id = idx;
		return true;
	}
	
	public boolean setGuessName() {
		return true;
	}
	
	public boolean setLocalPath(){
		return true;
	}*/

}
