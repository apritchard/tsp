package com.amp.tsp.capture;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.amp.tsp.parse.YamlPoint3d;

public class DrawUtils {
	private static final int radius = 9;
	
	public static void drawMap3d(Graphics g, Map<String, YamlPoint3d> points,
			List<String> startingPoints, List<String> endingPoints, List<String> warpPoints) {
		drawMap3d(g, points, startingPoints, endingPoints, warpPoints, null);
	}
	
	public static void drawMap3d(Graphics g, Map<String, YamlPoint3d> points,
			List<String> startingPoints, List<String> endingPoints, List<String> warpPoints,
			YamlPoint3d currentPoint){
		Graphics2D g2d = (Graphics2D)g;
		
		for(Entry<String, YamlPoint3d> set : points.entrySet()){
			YamlPoint3d p = set.getValue();
			Color interior = Color.WHITE;
			Color exterior = Color.RED;
			String textName;			
			
			if(warpPoints.contains(set.getKey())){
				interior = Color.YELLOW;
			} 

			if(startingPoints.contains(set.getKey())){
				exterior = Color.GREEN;
				textName = "" + (startingPoints.indexOf(set.getKey()) +1) + ". "  + set.getKey();
			} else if(endingPoints.contains(set.getKey())){
				exterior = Color.BLUE;
				textName = "" + (endingPoints.indexOf(set.getKey()) +1) + ". "  + set.getKey();
			} else {
				textName = set.getKey();
			}
			drawPoint3d(g2d, p, interior, exterior, textName);
		}
		
		if(currentPoint != null){
			drawPoint3d(g2d, currentPoint, Color.CYAN, Color.GRAY, "" + currentPoint.z);
		}		
	}
	
	private static void drawPoint3d(Graphics2D g2d, YamlPoint3d p, Color interior, Color exterior, String textName) {
		Point bottom = new Point(p.x, p.y);
		Point top = new Point(p.x, p.y - p.z);
		g2d.setColor(Color.GRAY);
		g2d.setStroke(new BasicStroke(2));
		g2d.drawLine(bottom.x, bottom.y, top.x, top.y);
		drawPoint(g2d, top, interior, exterior, textName);
	}
	
	private static void drawPoint(Graphics2D g2d, Point p, Color interior, Color exterior, String textName){
		int x = (int) (p.getX() - radius/2);
		int y = (int) (p.getY() - radius/2);
		g2d.setStroke(new BasicStroke(2));
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(interior);
		g2d.fillOval(x, y, radius-1, radius-1);	
		g2d.setColor(exterior);
		g2d.drawOval(x, y, radius, radius);
		if(textName != null && !textName.isEmpty()) {
			int textX = x-radius;
			int textY = y-(radius*2);
			g2d.setColor(Color.WHITE);
			g2d.setFont(new Font("Helvetica", Font.PLAIN, 20));
			g2d.drawString(textName, textX, textY);
		}
	}
	
}
