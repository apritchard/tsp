package com.amp.tsp.capture;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.amp.tsp.app.SelectionListener;
import com.amp.tsp.mapping.Sector;
import com.amp.tsp.parse.MapParser;
import com.amp.tsp.parse.YamlClickMap;
import com.amp.tsp.parse.YamlPoint;


public class SelectionPicker extends BlankFrame {
	private static final Logger logger = Logger.getLogger(SelectionPicker.class.getName());
	private static final long serialVersionUID = 1L;
	
	private PointListener pointListener;
	
	private Map<String, Point> points = new HashMap<>();
	private List<String> startingPoints = new ArrayList<>();
	private List<String> endingPoints = new ArrayList<>();
	
	public static void main(String[] args) throws MalformedURLException {
		SelectionPicker sp = new SelectionPicker(new SolveAndDisplayPointListener(
				new SelectionListener(){public void finished(List<Sector> path, BufferedImage screenShot, int distance, 
						Map<String, Point> points, List<String> seeds, List<String> endPoints) 
				{/*do nothing*/}}
		));
		
		if(args.length >0){
			YamlClickMap ycm = MapParser.parseClickMap(Paths.get(args[0]).toUri().toURL());
			sp.setStartingPoints(ycm.startingPoints);
			sp.setEndingPoints(ycm.endingPoints);
		
			for(Entry<String, YamlPoint> point : ycm.points.entrySet()){
				Point p = new Point(point.getValue().x, point.getValue().y);
				sp.getPoints().put(point.getKey(), p);
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
		for(Entry<String, Point> point : getPoints().entrySet()){
			if(point.getValue().distance(p) < deleteThreshold){
				getPoints().remove(point.getKey());
				getStartingPoints().remove(point.getKey());
				getEndingPoints().remove(point.getKey());
				return true;
			}
		}
		return false;
	}
	
	public List<String> getStartingPoints() {
		return startingPoints;
	}

	public void setStartingPoints(List<String> startingPoints) {
		this.startingPoints = startingPoints == null ? new ArrayList<String>() : startingPoints;
	}

	public List<String> getEndingPoints() {
		return endingPoints;
	}

	public void setEndingPoints(List<String> endingPoints) {
		this.endingPoints = endingPoints == null ? new ArrayList<String>() : endingPoints;
	}

	public Map<String, Point> getPoints() {
		return points;
	}

	public void setPoints(Map<String, Point> points) {
		this.points = points;
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
			String textName;
			for(Entry<String,Point> set : getPoints().entrySet()){
				Point p = set.getValue();
				int x = (int) (p.getX() - radius/2);
				int y = (int) (p.getY() - radius/2);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.WHITE);
				g.fillOval(x, y, radius-1, radius-1);
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
				g.drawOval(x, y, radius, radius);
				
				int textX = x-radius;
				int textY = y-(radius*2);
				g2d.setColor(Color.WHITE);
				g2d.setFont(new Font("Helvetica", Font.PLAIN, 20));
				g2d.drawString(textName, textX, textY);
			}

		}
	}
	
	class PointClickAdapter extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e){
			if(SwingUtilities.isRightMouseButton(e)){
				pointListener.notifySelection(getPoints(), getStartingPoints(), getEndingPoints());
				setVisible(false);
				dispose();
			} else {
				if(deleteIfExisting(e.getPoint())){
					repaint();
					return;
				}
				String s = JOptionPane.showInputDialog(null, "Point Name:", "Point Name", JOptionPane.PLAIN_MESSAGE);
				if(s == null){
					return;
				}
				getPoints().put(s, e.getPoint());
				if(e.isShiftDown()){
					getStartingPoints().add(s);
					getEndingPoints().remove(s);
				} else if (e.isControlDown()) {
					getEndingPoints().add(s);
					getStartingPoints().remove(s);
				} else {
					getEndingPoints().remove(s);
					getStartingPoints().remove(s);
				}
				repaint();
			} 
		}
		
	}
	
}
