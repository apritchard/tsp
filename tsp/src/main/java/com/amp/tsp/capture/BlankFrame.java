package com.amp.tsp.capture;

import java.awt.Color;
import java.awt.GraphicsDevice;

import javax.swing.JFrame;

public class BlankFrame extends JFrame{
	private static final long serialVersionUID = 1L;

	public BlankFrame(){
		setUndecorated(true);
		GraphicsDevice gd = DrawUtils.getGraphicsDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		setLocation(gd.getDefaultConfiguration().getBounds().x, 0);
		setSize(width, height);
		setBackground(new Color(0f, 0f, 0f, 0f));
	}
}
