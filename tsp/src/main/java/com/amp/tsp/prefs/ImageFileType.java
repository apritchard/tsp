package com.amp.tsp.prefs;


public enum ImageFileType {
	BMP("bmp"), GIF("gif"), JPEG("jpg"), PNG("png"), TIFF("tif");
	
	String ext;
	
	private ImageFileType(String ext){
		this.ext = ext;
	}
	
	public String getExtension(){
		return ext;
	}
}
