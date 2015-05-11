package com.amp.tsp.capture;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.amp.tsp.mapping.MapWrapper;
import com.amp.tsp.mapping.Sector;
import com.amp.tsp.mapping.TspUtilities;
import com.amp.tsp.parse.MapParser;
import com.amp.tsp.parse.YamlClickMap;
import com.amp.tsp.parse.YamlPoint;


public class SelectionPicker extends BlankFrame {
	private static final Logger logger = Logger.getLogger(SelectionPicker.class.getName());
	private static final long serialVersionUID = 1L;
	
	private PointListener pointListener;
	
	private Map<String, Point> points = new HashMap<>();
	private List<String> startingPoints = new ArrayList<>();
	
	public static void main(String[] args) throws MalformedURLException {
		SelectionPicker sp = new SelectionPicker(new PointListener() {
			
			@Override
			public void notifySelection(Map<String, Point> points, List<String> startingPoints) {
				Set<Sector> sectors = TspUtilities.pointsToSectors(points);
				MapParser.writeMapFile("mapText.yaml", sectors);
				MapParser.writeClickMap("clickMap.yaml", points, startingPoints);
				MapWrapper mw;
				if(startingPoints.isEmpty()){
					mw = new MapWrapper(sectors);
				} else {
					mw = new MapWrapper(sectors, TspUtilities.stringsToSeeds(startingPoints, sectors), true);
				}
				List<Sector> path = mw.calcTspMulti();
				logger.info("Best Path: " + path + " Distance: " + mw.getBoundForPath(path));
				final BlankFrame frame = new BlankFrame();
				frame.add(new RoutePanel(points, startingPoints, path));
				MouseAdapter adapter = new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e){
						if(SwingUtilities.isRightMouseButton(e)){
							frame.setVisible(false);
							frame.dispose();
						} 
					}
				};
				frame.addMouseListener(adapter);
				frame.addMouseMotionListener(adapter);
				frame.setVisible(true);
			}
		});
		
		if(args.length >0){
			YamlClickMap ycm = MapParser.parseClickMap(Paths.get(args[0]).toUri().toURL());
			sp.startingPoints = ycm.startingPoints;
		
			for(Entry<String, YamlPoint> point : ycm.points.entrySet()){
				Point p = new Point(point.getValue().x, point.getValue().y);
				sp.points.put(point.getKey(), p);
			}
		}
		
		sp.setVisible(true);
	}
	
	public SelectionPicker(final PointListener pointListener){
		super();

		DrawPanel drawPanel = new DrawPanel();
		add(drawPanel);
		
		this.pointListener = pointListener;
		PointClickAdapter adapter = new PointClickAdapter();
		addMouseListener(adapter);
		addMouseMotionListener(adapter);
	}
	
	private boolean deleteIfExisting(Point p){
		int deleteThreshold = 6;
		for(Entry<String, Point> point : points.entrySet()){
			if(point.getValue().distance(p) < deleteThreshold){
				points.remove(point.getKey());
				startingPoints.remove(point.getKey());
				return true;
			}
		}
		return false;
	}
	
	class DrawPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		
		public DrawPanel(){
			setBackground(new Color(0f, 0f, 0f, 0.1f));
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

		}
	}
	
	class PointClickAdapter extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e){
			if(SwingUtilities.isRightMouseButton(e)){
				pointListener.notifySelection(points, startingPoints);
				setVisible(false);
				dispose();
			} else {
				if(deleteIfExisting(e.getPoint())){
					repaint();
					return;
				}
				String s = JOptionPane.showInputDialog(null, "Point Name:", "Point Name", JOptionPane.PLAIN_MESSAGE).toString();
				points.put(s, e.getPoint());
				if(e.isShiftDown()){
					startingPoints.add(s);
				}				
				repaint();
			} 
		}
		
	}
	
	
}
