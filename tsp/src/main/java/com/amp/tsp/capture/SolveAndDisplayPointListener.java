package com.amp.tsp.capture;

import java.awt.AWTException;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RunnableFuture;

import javax.swing.*;

import org.apache.log4j.Logger;

import com.amp.tsp.app.SelectionListener;
import com.amp.tsp.mapping.Constraint;
import com.amp.tsp.mapping.Sector;
import com.amp.tsp.mapping.TspSolution;
import com.amp.tsp.mapping.TspSolver;
import com.amp.tsp.mapping.TspUtilities;
import com.amp.tsp.parse.MapParser;
import com.amp.tsp.parse.YamlPoint3d;
import com.amp.tsp.prefs.PrefName;

public class SolveAndDisplayPointListener implements PointListener {
	private static final Logger logger = Logger.getLogger(SolveAndDisplayPointListener.class);
	
	private SelectionListener selectionListener;
	private ProgressFrame progressFrame;

	private Map<String, YamlPoint3d> points;
	private List<String> startingPoints;
	private List<String> endingPoints;
	private List<String> warpPoints;
	private TspSolver solver;

	public SolveAndDisplayPointListener(SelectionListener selectionListener){
		this.selectionListener = selectionListener;
	}


	@Override
	public void notifySelection(final Map<String, YamlPoint3d> points, final List<String> startingPoints, final List<String> endingPoints, final List<String> warpPoints) {
		if(points == null || points.isEmpty()){
			selectionListener.finished(null, null, 0, null, null, null, null);
			return;
		}

		this.points = points;
		this.startingPoints = startingPoints;
		this.endingPoints = endingPoints;
		this.warpPoints = warpPoints;

		Set<Sector> sectors = TspUtilities.pointsToSectors(points, warpPoints);
		MapParser.writeMapFile("mapText.yaml", sectors);
		MapParser.writeClickMap(PrefName.LAST_MAP_LOCATION.get(), points, startingPoints, endingPoints, warpPoints);
		
		List<Constraint> constraints = TspUtilities.stringsToConstraints(startingPoints, endingPoints, sectors);
		this.solver = TspSolution.forSectors(sectors).usingConstraints(constraints).accuracy(PrefName.ALGORITHM_ACCURACY.getInt());

		progressFrame = new ProgressFrame(points.size());
		progressFrame.toFront();
		progressFrame.repaint();
		solver.setProgressFrame(progressFrame);

		TspWorker tspWorker = new TspWorker();
		try {
			tspWorker.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void displayResults(List<Sector> path){
		final int distance = solver.getBoundForPath(path);

		logger.info("Best Path: " + path + " Distance: " + distance);
		final BlankFrame frame = new BlankFrame();
		frame.add(new RoutePanel(points, startingPoints, endingPoints, warpPoints, path));
		MouseAdapter adapter = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e){
				if(SwingUtilities.isRightMouseButton(e)){
					selectionListener.finished(path, captureScreen(points), distance, points, startingPoints, endingPoints, warpPoints);
					frame.setVisible(false);
					frame.dispose();
				}
			}
		};
		progressFrame.setVisible(false);
		progressFrame.dispose();

		frame.addMouseListener(adapter);
		frame.addMouseMotionListener(adapter);
		frame.setVisible(true);
	}
	
	private BufferedImage captureScreen(Map<String, YamlPoint3d> points){
		Polygon poly = new Polygon();
		for(YamlPoint3d p : points.values()){
			poly.addPoint(p.x, p.y);
			poly.addPoint(p.x, p.y - p.z);
		}
		Rectangle r = poly.getBounds();
		int x = r.x - 40;
		int y = r.y - 40;
		int width = r.width + 80;
		int height = r.height + 80;
		try {
			//have to do this kludge because Java8 robot no longer correctly behaves on consuming graphics device
			//must manually offset
			GraphicsDevice gd = DrawUtils.getGraphicsDevice();
			GraphicsConfiguration gc = gd.getConfigurations()[0];
			Rectangle screen = gc.getBounds();
			Robot robot = new Robot();
			Rectangle screenRect = new Rectangle(screen.x + x, screen.y + y,width,height);
			BufferedImage img = robot.createScreenCapture(screenRect);
			return img;
		} catch (AWTException e){
			throw new RuntimeException(e);
		}
		
	}

	class TspWorker extends SwingWorker<List<Sector>, Integer> {

		@Override
		protected List<Sector> doInBackground() throws Exception {
			List<Sector> sectors = solver.solve();
			displayResults(sectors);
			return sectors;
		}
	}

}
