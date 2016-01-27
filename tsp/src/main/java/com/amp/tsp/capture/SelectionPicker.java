package com.amp.tsp.capture;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.amp.tsp.app.SelectionListener;
import com.amp.tsp.mapping.Sector;
import com.amp.tsp.mapping.TspUtilities;
import com.amp.tsp.parse.MapParser;
import com.amp.tsp.parse.YamlClickMap3d;
import com.amp.tsp.parse.YamlPoint;
import com.amp.tsp.parse.YamlPoint3d;
import com.amp.tsp.prefs.PrefName;


public class SelectionPicker extends BlankFrame {
	private static final Logger logger = Logger.getLogger(SelectionPicker.class);
	private static final long serialVersionUID = 1L;
	
	private PointListener pointListener;
	
	private Map<String, YamlPoint3d> points3d = new HashMap<>();
	private List<String> startingPoints = new ArrayList<>();
	private List<String> endingPoints = new ArrayList<>();
	private List<String> warpPoints = new ArrayList<>();
	
	private YamlPoint3d clickCurrent;
	
	public static void main(String[] args) throws MalformedURLException {
		SelectionPicker sp = new SelectionPicker(new SolveAndDisplayPointListener(
				new SelectionListener(){public void finished(List<Sector> path, BufferedImage screenShot, int distance, 
						Map<String, YamlPoint3d> points, List<String> seeds, List<String> endPoints, List<String> warpPoints) 
				{/*do nothing*/}}
		));
		
		if(args.length >0){
			YamlClickMap3d ycm = MapParser.parseClickMap(Paths.get(args[0]).toUri().toURL());
			sp.setStartingPoints(ycm.startingPoints);
			sp.setEndingPoints(ycm.endingPoints);
			sp.setWarpPoints(ycm.warpPoints);
			sp.setPoints3d(ycm.points);
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
		addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				pointListener.notifySelection(null, null, null, null);
				dispose();
			}
			public void keyReleased(KeyEvent e) {}
			public void keyPressed(KeyEvent e) {}
		});
	}
	
	private boolean deleteIfExisting(YamlPoint3d p){
		int deleteThreshold = 6;
		for(Entry<String, YamlPoint3d> point : getPoints3d().entrySet()){
			if(TspUtilities.distance3d(point.getValue(), p) < deleteThreshold){
				getPoints3d().remove(point.getKey());
				getStartingPoints().remove(point.getKey());
				getEndingPoints().remove(point.getKey());
				return true;
			}
		}
		return false;
	}
	
	private void switchMonitors(){
		int screen = PrefName.SCREEN_NUMBER.getInt();
		GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		screen = (screen + 1) % gds.length;
		GraphicsDevice gd = gds[screen];
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		setLocation(gd.getDefaultConfiguration().getBounds().x, 0);
		setSize(width, height);
		PrefName.SCREEN_NUMBER.putInt(screen);
		repaint();
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

	public List<String> getWarpPoints() {
		return warpPoints;
	}

	public void setWarpPoints(List<String> warpPoints) {
		this.warpPoints = warpPoints == null ? new ArrayList<String>() : warpPoints;
	}
	
	public Map<String, YamlPoint3d> getPoints3d() {
		return points3d;
	}
	
	public void setPoints3d(Map<String, YamlPoint3d> points3d){
		this.points3d = points3d;
	}

	class DrawPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		
		public DrawPanel(){
			setBackground(new Color(0f, 0f, 0f, 0.1f));
		}
		
		public void paint(Graphics g){
			super.paint(g);
//			DrawUtils.drawMap(g, points, startingPoints, endingPoints, warpPoints, clickDown, clickCurrent);
			DrawUtils.drawMap3d(g, points3d, startingPoints, endingPoints, warpPoints, clickCurrent);
		}
	}
	
	class PointClickAdapter extends MouseAdapter {
		
		@Override
		public void mousePressed(MouseEvent e) {
			clickCurrent = new YamlPoint3d();
			clickCurrent.x = e.getPoint().x;
			clickCurrent.y = e.getPoint().y;
		};
		
		@Override
		public void mouseDragged(MouseEvent e) {
			clickCurrent.z = e.getPoint().y > clickCurrent.y ? 0 : clickCurrent.y - e.getPoint().y; 
			repaint();
		}
		
		@Override
		public void mouseReleased(MouseEvent e){
			if(SwingUtilities.isRightMouseButton(e)){
				logger.info("Points selected: " + getPoints3d() + " Starting: " + getStartingPoints() + " Ending: " + getEndingPoints() + " Warp: " + getWarpPoints());
				pointListener.notifySelection(getPoints3d(), getStartingPoints(), getEndingPoints(), getWarpPoints());
				setVisible(false);
				dispose();
			} else {
				if(e.isAltDown() && e.isShiftDown() && e.isControlDown()) {
					switchMonitors();
					return;
				}

				clickCurrent.z = e.getPoint().y > clickCurrent.y ? 0 : clickCurrent.y - e.getPoint().y;
				if(deleteIfExisting(clickCurrent)){
					repaint();
					return;
				}
				String s = JOptionPane.showInputDialog(null, "Point Name:", "Point Name", JOptionPane.PLAIN_MESSAGE);
				if(s == null){
					return;
				}
				getPoints3d().put(s, clickCurrent);
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
				
				if(e.isAltDown()){
					getWarpPoints().add(s);
				} else {
					getWarpPoints().remove(s);
				}
				clickCurrent = null;				
				repaint();
			} 
		}
		
	}
	
}
