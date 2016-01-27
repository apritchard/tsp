package com.amp.tsp.capture;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

import com.amp.tsp.prefs.PrefName;

public class BlankFrame extends JFrame{
	private static final long serialVersionUID = 1L;

	public BlankFrame(){
		setUndecorated(true);
		int screen = PrefName.SCREEN_NUMBER.getInt();
		GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		GraphicsDevice gd = null;
		if(screen >= 0 && screen < gds.length){
			gd = gds[screen];
		} else {
			gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		}
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		setLocation(gd.getDefaultConfiguration().getBounds().x, 0);
		setSize(width, height);
		setBackground(new Color(0f, 0f, 0f, 0f));
	}
}
