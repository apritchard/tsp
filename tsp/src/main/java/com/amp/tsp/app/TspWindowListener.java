package com.amp.tsp.app;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import com.amp.tsp.prefs.PrefName;

public class TspWindowListener 	implements WindowListener{
	private JFrame frame;
	
	public TspWindowListener(JFrame frame){
		this.frame = frame;
	}


	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		PrefName.APP_X.putInt(frame.getX());
		PrefName.APP_Y.putInt(frame.getY());
		PrefName.APP_WIDTH.putInt(frame.getWidth());
		PrefName.APP_HEIGHT.putInt(frame.getHeight());
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
