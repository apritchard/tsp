package com.amp.tsp.prefs;

import java.util.logging.Logger;
import java.util.prefs.Preferences;

public enum PrefName {
	IMAGE_FILE_TYPE("image-file-type", PrefType.ENUM_SINGLE, ImageFileType.TIFF.toString(), true),
	LAST_SAVE_PATH("last-save-path", PrefType.DIRECTORY, "", true),
	LAST_MAP_LOCATION("last-map-location", PrefType.FILE, "clickMap.yaml", true),
	LAST_SAVED_LOCATION("last-saved-location", PrefType.FILE, "clickMap.yaml", false),
	APP_X("app-x", PrefType.INTEGER, 0, false),
	APP_Y("app-y", PrefType.INTEGER, 0, false),
	APP_WIDTH("app-width", PrefType.INTEGER, 640, false),
	APP_HEIGHT("app-height", PrefType.INTEGER, 480, false);
	
	private static final Preferences prefs = Preferences.userNodeForPackage(PreferencesPanel.class);	
	private static final Logger logger = Logger.getLogger(PrefName.class.getName());
	
	private String pathName; 
	private PrefType type;
	private Object defaultValue;
	private boolean editable;
	
	PrefName(String pathName, PrefType type, Object defaultValue, boolean editable){
		this.pathName = pathName;
		this.type = type;
		this.defaultValue = defaultValue;
		this.editable = editable;
	}
	
	public String path(){
		return pathName;
	}
	
	public PrefType getType(){
		return type;
	}
	
	public boolean isEditable(){
		return editable;
	}
	
	public String defaultString(){
		return defaultValue.toString();
	}
	
	public int defaultInt(){
		try{
			int value = Integer.parseInt(defaultValue.toString());
			return value;
		} catch (NumberFormatException nfe){
			nfe.printStackTrace();
			logger.info("Int requested from non-int preference value (" + defaultValue + ") on " + toString());
			return 0;
		}
	}
	
	public boolean defaultBoolean(){
		if(defaultValue.toString().equalsIgnoreCase("true")){
			return true;
		} else if (defaultValue.toString().equalsIgnoreCase("false")){
			return false;
		} else {
			logger.info("Boolean requested from non-boolean preference value (" + defaultValue +") on " + toString());
			return false;
		}
	}
	
	public String get(){
		return prefs.get(pathName, defaultString());
	}
	
	public int getInt(){
		return prefs.getInt(pathName, defaultInt());
	}
	
	public boolean getBoolean(){
		return prefs.getBoolean(pathName, defaultBoolean());
	}
	
	public void put(String value){
		prefs.put(pathName, value);
	}
	
	public void putInt(int value){
		prefs.putInt(pathName, value);
	}
	
	public void putBoolean(boolean value){
		prefs.putBoolean(pathName, value);
	}
}
