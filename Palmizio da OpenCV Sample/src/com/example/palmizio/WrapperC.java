package com.example.palmizio;

public class WrapperC {
	
	//dichiariamo il costruttore privat per non poter instanziare un oggetto 
	private WrapperC() {}
	
	
	//dichiaro il metodo nativo:
	//public native void FindFeatures(long matAddrGr, long matAddrRgba);
	private static native void FindRect(long src_p, long dst_r);
	
	private static native String FindFreak(long src_p);
	
	private static native void Train();
	
	private static native void AddImg(String path);
	
	public static void JAddImg(String path){
		AddImg(path);
	
	}
	
	public static void JFindRect (long src_p, long dst_r){
		FindRect(src_p, dst_r);
	}
    
	static{
		System.loadLibrary("mixed_sample");
	}
	
	
	
	public static String JFindFreak (long src_p){
		String out = FindFreak(src_p);
		return out;
	}
    
	static{
		System.loadLibrary("mixed_sample");
	}
	
	
	
	public static void JTrain (){
		Train();
	}
    
	static{
		System.loadLibrary("mixed_sample");
	}
	
	
	
	
	
	
	

}
