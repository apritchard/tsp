package com.amp.tsp.app;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import com.amp.tsp.capture.InstructionFrame;
import com.amp.tsp.capture.SelectionPicker;
import com.amp.tsp.capture.SolveAndDisplayPointListener;
import com.amp.tsp.mapping.Sector;
import com.amp.tsp.mapping.TspSolution;
import com.amp.tsp.parse.MapParser;
import com.amp.tsp.parse.YamlClickMap;
import com.amp.tsp.parse.YamlClickMap3d;
import com.amp.tsp.parse.YamlPoint;
import com.amp.tsp.parse.YamlPoint3d;
import com.amp.tsp.prefs.PrefName;

public class TspPanel extends JPanel implements SelectionListener {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TspPanel.class);
	private static final JFrame instructionFrame = new InstructionFrame();
	
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
				instructionFrame.setVisible(true);
			}
		});
		
		JButton btnLastMap = new JButton("Load last map");
		btnLastMap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getTopLevelAncestor().setVisible(false);
				SelectionPicker picker = new SelectionPicker(new SolveAndDisplayPointListener(sl));
				try{
					YamlClickMap3d ycm = MapParser.parseClickMap(Paths.get(PrefName.LAST_MAP_LOCATION.get()).toUri().toURL());
					picker.setStartingPoints(ycm.startingPoints);
					picker.setEndingPoints(ycm.endingPoints);
					picker.setWarpPoints(ycm.warpPoints);
					picker.setPoints3d(ycm.points);
				} catch (Exception ex){
					logger.error("Unable to load last map: " + ex);
					JOptionPane.showMessageDialog(null, "No previous map found! Either make a new map, change your last map location in preferences, or load a saved map.");
					getTopLevelAncestor().setVisible(true);
					picker.dispose();
					return;
				}
				picker.setVisible(true);
				instructionFrame.setVisible(true);				
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
					YamlClickMap3d ycm = MapParser.parseClickMap(Paths.get(chooser.getSelectedFile().getAbsolutePath()).toUri().toURL());
					picker.setStartingPoints(ycm.startingPoints);
					picker.setEndingPoints(ycm.endingPoints);
					picker.setWarpPoints(ycm.warpPoints);
					picker.setPoints3d(ycm.points);
				} catch (MalformedURLException mue){
					logger.error("Unable to load map.", mue);
				}
				picker.setVisible(true);
				instructionFrame.setVisible(true);			
			}			
		});

	    JSlider sldrComplexity = new JSlider(TspSolution.MIN_ACCURACY, TspSolution.MAX_ACCURACY, PrefName.ALGORITHM_ACCURACY.getInt());
	    Dictionary<Integer, JLabel> labels = new Hashtable<>();
	    labels.put(TspSolution.MIN_ACCURACY, new JLabel("Fast Calculation"));
	    labels.put(TspSolution.MAX_ACCURACY, new JLabel("Optimal Route"));
	    sldrComplexity.setLabelTable(labels);
	    sldrComplexity.setMajorTickSpacing(1);
	    sldrComplexity.setPaintLabels(true);
	    sldrComplexity.setPaintTicks(true);
	    
		sldrComplexity.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				if(!source.getValueIsAdjusting()){
					PrefName.ALGORITHM_ACCURACY.putInt(source.getValue());
				}
			}
		});
		
	    add(btnNewMap, "width 33%");
		add(btnLastMap, "width 33%");
		add(btnLoadMap, "width 33%, wrap");
		add(sldrComplexity, "spanx, grow, wrap");

	}

	@Override
	public void finished(final List<Sector> path, final BufferedImage screenShot, final int distance, 
			final Map<String, YamlPoint3d> points, final List<String> startPoints, final List<String> endPoints, final List<String> warpPoints) {
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
