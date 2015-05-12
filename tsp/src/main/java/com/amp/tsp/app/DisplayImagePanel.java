package com.amp.tsp.app;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

import com.amp.tsp.mapping.Sector;
import com.amp.tsp.parse.MapParser;
import com.amp.tsp.prefs.ImageFileType;
import com.amp.tsp.prefs.PrefName;

public class DisplayImagePanel extends JPanel{
	private static final long serialVersionUID = 1L;

	public DisplayImagePanel(final List<Sector> path, final BufferedImage image, int distance, 
			final Map<String, Point> points, final List<String> startPoints, final List<String> endPoints){
		setLayout(new MigLayout());
		JLabel lblImage = new JLabel(new ImageIcon(image));
		String pathDesc = "Optimal Path (" + distance + "px): " + path.toString();
		JTextArea taPath = new JTextArea();
	    taPath.setText(pathDesc);
	    taPath.setWrapStyleWord(true);
	    taPath.setLineWrap(true);
	    taPath.setOpaque(false);
	    taPath.setEditable(false);
	    taPath.setBackground(UIManager.getColor("Label.background"));
	    taPath.setFont(UIManager.getFont("Label.font"));
	    taPath.setBorder(UIManager.getBorder("Label.border"));

		final Component parent = this;
		
		JButton btnSave = new JButton("Save Image");
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File lastDir = new File(PrefName.LAST_SAVE_PATH.get());
				JFileChooser fileChooser = new JFileChooser(lastDir);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					ImageFileType imgType = ImageFileType.valueOf(PrefName.IMAGE_FILE_TYPE.get());
					if(!file.getName().endsWith(imgType.getExtension())){
						file = new File(file.getAbsolutePath() + "." + imgType.getExtension());
					}
					saveImageToFile(image, file, imgType.getExtension());
					PrefName.LAST_SAVE_PATH.put(file.getParent());
				}
				
			}
		});
		
		JButton btnSaveMap = new JButton("Save Map");
		btnSaveMap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File lastDir = new File(PrefName.LAST_SAVE_PATH.get());
				JFileChooser fileChooser = new JFileChooser(lastDir);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					String ext = "clm";
					if(!file.getName().endsWith(ext)){
						file = new File(file.getAbsolutePath() + "." + ext);
					}
					MapParser.writeClickMap(file.getAbsolutePath(), points, startPoints, endPoints);
					PrefName.LAST_SAVE_PATH.put(file.getParent());
				}
				
			}			
		});
		add(lblImage, "wrap");
		add(taPath, "wrap, grow, push");
		add(btnSave);
		add(btnSaveMap);
	}
	
	private static void saveImageToFile(BufferedImage bi, File file, String type) {
		try {
			ImageIO.write(bi, type, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
