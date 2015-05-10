package com.amp.tsp.capture;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

public class BlankFrame extends JFrame{
	private static final long serialVersionUID = 1L;

	public BlankFrame(){
		setUndecorated(true);
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		setSize(width, height);
		setBackground(new Color(0f, 0f, 0f, 0f));
	}
}
