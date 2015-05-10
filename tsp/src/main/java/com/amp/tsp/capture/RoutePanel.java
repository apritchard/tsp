package com.amp.tsp.capture;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;

import com.amp.tsp.mapping.Sector;

public class RoutePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	Map<String, Point> points;
	List<String> startingPoints;
	List<Sector> path;

	
	public RoutePanel(Map<String, Point> points, List<String> startingPoints, List<Sector> path){
		setBackground(new Color(0f, 0f, 0f, 0.1f));
		this.points = points;
		this.startingPoints = startingPoints;
		this.path = path;
	}
	
	public void paint(Graphics g){
		super.paint(g);
		
		Graphics2D g2d = (Graphics2D)g;
		int radius = 9;
		for(Entry<String,Point> set : points.entrySet()){
			Point p = set.getValue();
			int x = (int) (p.getX() - radius/2);
			int y = (int) (p.getY() - radius/2);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(Color.WHITE);
			g.fillOval(x, y, radius-1, radius-1);
			if(startingPoints.contains(set.getKey())){
				g2d.setColor(Color.GREEN);
			} else {
				g2d.setColor(Color.RED);
			}
			g.drawOval(x, y, radius, radius);
			
			int textX = x-radius;
			int textY = y-(radius*2);
			g2d.setColor(Color.WHITE);
			g2d.setFont(new Font("Helvetica", Font.PLAIN, 20));
			g2d.drawString(set.getKey(), textX, textY);
		}
		
		g2d.setStroke(new BasicStroke(4));
		g2d.setColor(Color.GRAY);
		Point first = null;
		Point second = null;
		for(Sector s : path){
			if(first == null){
				first = points.get(s.getName());
				continue;
			}
			second = points.get(s.getName());
			g2d.draw(new Line2D.Double(first, second));
			
			double theta = Math.atan2(first.getY() - second.getY(), first.getX() - second.getX());
			drawArrow(g2d, theta, second.getX(), second.getY());
			
			first = second;
		}

	}
	
	private void drawArrow(Graphics2D g2, double theta, double x0, double y0)
    {
		int barb=10;
		double phi = Math.PI/6; //30 degrees
        double x = x0 + barb * Math.cos(theta + phi);
        double y = y0 + barb * Math.sin(theta + phi);
        g2.draw(new Line2D.Double(x0, y0, x, y));
        x = x0 + barb * Math.cos(theta - phi);
        y = y0 + barb * Math.sin(theta - phi);
        g2.draw(new Line2D.Double(x0, y0, x, y));
    }
}
