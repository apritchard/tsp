package com.amp.tsp.capture;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.amp.tsp.app.SelectionListener;
import com.amp.tsp.mapping.MapWrapper;
import com.amp.tsp.mapping.Sector;
import com.amp.tsp.mapping.TspUtilities;
import com.amp.tsp.parse.MapParser;
import com.amp.tsp.prefs.PrefName;

public class SolveAndDisplayPointListener implements PointListener {
	private static final Logger logger = Logger.getLogger(SolveAndDisplayPointListener.class.getName());
	
	private SelectionListener selectionListener; 
	
	public SolveAndDisplayPointListener(SelectionListener selectionListener){
		this.selectionListener = selectionListener;
	}


	@Override
	public void notifySelection(final Map<String, Point> points, final List<String> startingPoints, final List<String> endingPoints) {
		Set<Sector> sectors = TspUtilities.pointsToSectors(points);
		MapParser.writeMapFile("mapText.yaml", sectors);
		MapParser.writeClickMap(PrefName.LAST_MAP_LOCATION.get(), points, startingPoints, endingPoints);
		MapWrapper mw;
		mw = new MapWrapper(sectors, TspUtilities.stringsToConstraints(startingPoints, endingPoints, sectors));
		final List<Sector> path = mw.calcTspMulti();
		final int distance = mw.getBoundForPath(path);
		logger.info("Best Path: " + path + " Distance: " + distance);
		final BlankFrame frame = new BlankFrame();
		frame.add(new RoutePanel(points, startingPoints, endingPoints, path));
		MouseAdapter adapter = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e){
				if(SwingUtilities.isRightMouseButton(e)){
					selectionListener.finished(path, captureScreen(points), distance, points, startingPoints, endingPoints);
					frame.setVisible(false);
					frame.dispose();
				} 
			}
		};
		frame.addMouseListener(adapter);
		frame.addMouseMotionListener(adapter);
		frame.setVisible(true);		
	}
	
	private BufferedImage captureScreen(Map<String, Point> points){
		Polygon poly = new Polygon();
		for(Point p : points.values()){
			poly.addPoint(p.x, p.y);
		}
		Rectangle r = poly.getBounds();
		int x = r.x - 40;
		int y = r.y - 40;
		int width = r.width + 80;
		int height = r.height + 80;
		try {
			Robot robot = new Robot();
			Rectangle screenRect = new Rectangle(x,y,width,height);
			BufferedImage img = robot.createScreenCapture(screenRect);
			return img;
		} catch (AWTException e){
			throw new RuntimeException(e);
		}
		
	}

}
