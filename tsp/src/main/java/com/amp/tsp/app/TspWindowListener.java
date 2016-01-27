package com.amp.tsp.app;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import com.amp.tsp.prefs.PrefName;

public class TspWindowListener 	implements WindowListener{
	private JFrame frame;
	private PrefName appX, appY, appWidth, appHeight;
	
	public TspWindowListener(JFrame frame, PrefName appX, PrefName appY, PrefName appWidth, PrefName appHeight){
		this.frame = frame;
		this.appX = appX;
		this.appY = appY;
		this.appWidth = appWidth;
		this.appHeight = appHeight;
	}


	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		appX.putInt(frame.getX());
		appY.putInt(frame.getY());
		appWidth.putInt(frame.getWidth());
		appHeight.putInt(frame.getHeight());
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}
