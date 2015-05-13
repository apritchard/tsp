package com.amp.tsp.capture;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DrawUtils {
	public static void drawMap(Graphics g, Map<String, Point> points, List<String> startingPoints, List<String> endingPoints, List<String> warpPoints){
		Graphics2D g2d = (Graphics2D)g;
		int radius = 9;
		String textName;
		for(Entry<String,Point> set : points.entrySet()){
			Point p = set.getValue();
			int x = (int) (p.getX() - radius/2);
			int y = (int) (p.getY() - radius/2);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			if(warpPoints.contains(set.getKey())){
				g2d.setColor(Color.YELLOW);
			} else {
				g2d.setColor(Color.WHITE);
			}
			g2d.fillOval(x, y, radius-1, radius-1);
			if(startingPoints.contains(set.getKey())){
				g2d.setColor(Color.GREEN);
				textName = "" + (startingPoints.indexOf(set.getKey()) +1) + ". "  + set.getKey();
			} else if(endingPoints.contains(set.getKey())){
				g2d.setColor(Color.BLUE);
				textName = "" + (endingPoints.indexOf(set.getKey()) +1) + ". "  + set.getKey();
			} else {
				g2d.setColor(Color.RED);
				textName = set.getKey();
			}

			g2d.drawOval(x, y, radius, radius);
			
			int textX = x-radius;
			int textY = y-(radius*2);
			g2d.setColor(Color.WHITE);
			g2d.setFont(new Font("Helvetica", Font.PLAIN, 20));
			g2d.drawString(textName, textX, textY);
		}		
	}
}
