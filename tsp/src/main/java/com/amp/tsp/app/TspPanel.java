package com.amp.tsp.app;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

import com.amp.tsp.capture.SelectionPicker;
import com.amp.tsp.capture.SolveAndDisplayPointListener;
import com.amp.tsp.mapping.Sector;
import com.amp.tsp.parse.MapParser;
import com.amp.tsp.parse.YamlClickMap;
import com.amp.tsp.parse.YamlPoint;
import com.amp.tsp.prefs.PrefName;

public class TspPanel extends JPanel implements SelectionListener {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TspPanel.class.getName());
	
	public TspPanel(){
		super();
		setLayout(new MigLayout());
		
		final SelectionListener sl = this;
		
		JButton btnNewMap = new JButton("Enter new map");
		btnNewMap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getTopLevelAncestor().setVisible(false);
				SelectionPicker picker = new SelectionPicker(new SolveAndDisplayPointListener(sl));
				picker.setVisible(true);
			}
		});
		
		JButton btnLastMap = new JButton("Load last map");
		btnLastMap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getTopLevelAncestor().setVisible(false);
				SelectionPicker picker = new SelectionPicker(new SolveAndDisplayPointListener(sl));
				try{
					YamlClickMap ycm = MapParser.parseClickMap(Paths.get(PrefName.LAST_MAP_LOCATION.get()).toUri().toURL());
					picker.setStartingPoints(ycm.startingPoints);
					picker.setEndingPoints(ycm.endingPoints);
					picker.setWarpPoints(ycm.warpPoints);
				
					for(Entry<String, YamlPoint> point : ycm.points.entrySet()){
						Point p = new Point(point.getValue().x, point.getValue().y);
						picker.getPoints().put(point.getKey(), p);
					}
				} catch (Exception ex){
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null, "No previous map found! Either make a new map, change your last map location in preferences, or load a saved map.");
					getTopLevelAncestor().setVisible(true);
					picker.dispose();
					return;
				}
				picker.setVisible(true);
				
			}
		});
		
		JButton btnLoadMap = new JButton("Load another map");
		final Component chooserParent = this;
		btnLoadMap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				String previousDirectory = PrefName.LAST_SAVED_LOCATION.get();
				if(!previousDirectory.isEmpty()){
					chooser.setCurrentDirectory(new File(previousDirectory));
				} else {
					chooser.setCurrentDirectory(new File("."));
				}
				chooser.setDialogTitle("Select Click Map File");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if(chooser.showOpenDialog(chooserParent) != JFileChooser.APPROVE_OPTION) {
					return;
				}
				
				getTopLevelAncestor().setVisible(false);
				SelectionPicker picker = new SelectionPicker(new SolveAndDisplayPointListener(sl));
				try{
					YamlClickMap ycm = MapParser.parseClickMap(Paths.get(chooser.getSelectedFile().getAbsolutePath()).toUri().toURL());
					picker.setStartingPoints(ycm.startingPoints);
					picker.setEndingPoints(ycm.endingPoints);
					picker.setWarpPoints(ycm.warpPoints);
				
					for(Entry<String, YamlPoint> point : ycm.points.entrySet()){
						Point p = new Point(point.getValue().x, point.getValue().y);
						picker.getPoints().put(point.getKey(), p);
					}
				} catch (MalformedURLException mue){
					mue.printStackTrace();
				}
				picker.setVisible(true);
				
			}			
		});

		String instructions = "Press to create or load a map. Your primary monitor will tint, indicating recording is active. "
				+ "\nLeft click\tplace node on the map"
				+ "\nShift-click\tNode required at beginning of path"
				+ "\nCtrl-click\tNode required at end of path"
				+ "\nAlt-click\tNode may be warped to (cost in preferences)"
				+ "\nRight-click\tSolve graph (press again to save solved graph)"
				+ "\nEscape\tCancel map input"
				+ "\n\nNodes may be either beginning or ending and also a warp node, but cannot be both beginning"
				+ " and ending. Graphs containing more than 20 nodes may take some time to solve. Ending nodes decrease performance.";
		
		JTextArea taInstructions = new JTextArea();
	    taInstructions.setText(instructions);
	    taInstructions.setWrapStyleWord(true);
	    taInstructions.setLineWrap(true);
	    taInstructions.setOpaque(false);
	    taInstructions.setEditable(false);
	    taInstructions.setBackground(UIManager.getColor("Label.background"));
	    taInstructions.setFont(UIManager.getFont("Label.font"));
	    taInstructions.setBorder(UIManager.getBorder("Label.border"));
		
		add(btnNewMap, "wrap");
		add(btnLastMap, "wrap");
		add(btnLoadMap, "wrap");
		add(taInstructions, "wrap, growx, pushx");
	}

	@Override
	public void finished(final List<Sector> path, final BufferedImage screenShot, final int distance, 
			final Map<String, Point> points, final List<String> startPoints, final List<String> endPoints, final List<String> warpPoints) {
		getTopLevelAncestor().setVisible(true);
		
		if(path == null || path.isEmpty()){
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				JFrame frame = new JFrame();
				frame.getContentPane().add(new DisplayImagePanel(path, screenShot, distance, points, startPoints, endPoints, warpPoints));
				frame.pack();
				frame.setVisible(true);
			}
		});
	}
}
