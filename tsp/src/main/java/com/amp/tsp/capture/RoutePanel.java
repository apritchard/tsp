package com.amp.tsp.capture;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import com.amp.tsp.mapping.Sector;
import com.amp.tsp.parse.YamlPoint3d;

public class RoutePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Map<String, YamlPoint3d> points3d;
	private List<String> startingPoints;
	private List<String> endingPoints;
	private List<String> warpPoints;
	private List<Sector> path;
	
	public RoutePanel(Map<String, YamlPoint3d> points, List<String> startingPoints, List<String> endingPoints, List<String> warpPoints, List<Sector> path){
		setBackground(new Color(0f, 0f, 0f, 0.1f));
//		this.points = points;
		this.points3d = points;
		this.startingPoints = startingPoints;
		this.endingPoints = endingPoints;
		this.warpPoints = warpPoints;
		this.path = path;
	}
	
	public void paint(Graphics g){
		super.paint(g);
//		DrawUtils.drawMap(g, points, startingPoints, endingPoints, warpPoints);
		DrawUtils.drawMap3d(g, points3d, startingPoints, endingPoints, warpPoints);
		
		Graphics2D g2d = (Graphics2D)g;
		
		g2d.setStroke(new BasicStroke(4));
		YamlPoint3d first = null;
		YamlPoint3d second = null;
		for(Sector s : path){
			if(first == null){
				first = points3d.get(s.getName());
				continue;
			}
			second = points3d.get(s.getName());
			if(warpPoints.contains(s.getName())){
				g2d.setColor(Color.YELLOW);
			} else {
				g2d.setColor(Color.GRAY);
			}
			g2d.draw(new Line2D.Double(first.x, first.y - first.z, second.x, second.y - second.z));
			
			double theta = Math.atan2((first.y - first.z) - (second.y - second.z), first.x - second.x);
			drawArrow(g2d, theta, second.x, (second.y - second.z) );
			
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
