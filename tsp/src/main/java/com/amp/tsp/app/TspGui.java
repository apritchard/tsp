package com.amp.tsp.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.amp.tsp.prefs.PrefName;
import com.amp.tsp.prefs.PreferencesPanel;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class TspGui extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TspGui.class);
	
	private final static PreferencesPanel preferencesPanel = new PreferencesPanel();
	private final static AboutPanel aboutPanel = new AboutPanel();
	private final static TspPanel tspPanel = new TspPanel();
	
	private static final String VERSION_URL = "http://logicker.net/tour/files/version.txt";
	private static final String FILE_URL = "http://logicker.net/tour/files/TravelingStarshipSolver.jar";
	private static final String JAR_NAME = "TravelingStarshipSolver.jar";
	private static final String TEMP_JAR = "TempJar.jar";
	private static final String RENAME_JAR = "OldTravelingStarshipSolver.jar";
	public static final String CURRENT_VERSION = "1.1";
	
	private final TspGui theGui;
	
	public TspGui(){
		int appX = PrefName.APP_X.getInt();
		int appY = PrefName.APP_Y.getInt();
		int appWidth = PrefName.APP_WIDTH.getInt();
		int appHeight = PrefName.APP_HEIGHT.getInt();
		
		setSize(appWidth, appHeight);
		setLocation(appX, appY);
		setTitle("Traveling Starship Solver");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new TspWindowListener(this, PrefName.APP_X, PrefName.APP_Y, PrefName.APP_WIDTH, PrefName.APP_HEIGHT));
		
		setJMenuBar(buildMenuBar());
		getContentPane().add(tspPanel);
		
		theGui = this;
	}

	public static void main(String[] args) {
		if(args.length == 0){
			TspGui gui = new TspGui();
			gui.setVisible(true);
			return;
		}
		
		switch(args[0]){
		case "deleteAndDownload":
			deleteAndDownload();
			break;
		case "cleanUp":
			cleanUp();
			break;
		}
	}

	private JMenuBar buildMenuBar() {
		final JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);
		
		JMenuItem preferencesMenuItem = new JMenuItem("Preferences", KeyEvent.VK_P);
		preferencesMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
		preferencesMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame();
				frame.setTitle("Preferences");
				frame.setLocation(PrefName.APP_X.getInt() + 20, PrefName.APP_Y.getInt() + 20);
				frame.getContentPane().add(preferencesPanel);
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.pack();
				frame.setVisible(true);
			}
		});

		
		JMenuItem aboutMenuItem = new JMenuItem("About", KeyEvent.VK_A);
		aboutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
		aboutMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame();
				frame.setTitle("About");
				frame.setLocation(PrefName.APP_X.getInt() + 20, PrefName.APP_Y.getInt() + 20);
				frame.getContentPane().add(aboutPanel);
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.pack();
				frame.setVisible(true);
			}
		});
		
		JMenuItem updateMenuItem = new JMenuItem("Update", KeyEvent.VK_U);
		updateMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.ALT_MASK));
		updateMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					URL website = new URL(VERSION_URL);
					BufferedReader reader = new BufferedReader(new InputStreamReader(website.openStream(), Charsets.UTF_8));
					String newVersion = reader.readLine(); 
					logger.info("Version: " + newVersion);
					if(newVersion.compareTo(CURRENT_VERSION) > 0){
						updateApp();
					} else {
						JOptionPane.showMessageDialog(theGui, "Already up to date!");
					}
					
				} catch (IOException mue){
					JOptionPane.showMessageDialog(theGui, "Unable to contact server. Update cancelled.");
				}
				
			}
		});
		
		JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
		exitMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				theGui.dispose();
			}
		});
		
		fileMenu.add(preferencesMenuItem);		
		fileMenu.add(aboutMenuItem);
		fileMenu.add(updateMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);
		return menuBar;
	}
	
	
	private static void updateApp(){
		//copy this jar
		if(!copyFile(JAR_NAME, TEMP_JAR)){
			JOptionPane.showMessageDialog(null, "Unable to copy this jar, cannot update.");
			return;
		}
		//tell the new jar to run with update
		try {
			Process p = new ProcessBuilder("java", "-jar", TEMP_JAR, "deleteAndDownload").start();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Unable to start new process, cannot update.");
			return;
		}
		System.exit(0);
	}
	
	private static void deleteAndDownload(){
		//rename old jar
		if(!renameFile(JAR_NAME, RENAME_JAR)){
			JOptionPane.showMessageDialog(null, "Unable to rename old jar, cannot update.");
			return;
		}
		//download new jar
		try{
			downloadFile(FILE_URL, JAR_NAME);
		} catch (IOException ioe){
			JOptionPane.showMessageDialog(null, "Error downloading new update, reverting");
			renameFile(RENAME_JAR, JAR_NAME);
		}
		//tell new jar to run with cleanup
		try {
			Process p = new ProcessBuilder("java", "-jar", JAR_NAME, "cleanUp").start();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Unable to start new process, cannot update.");
			return;
		}	
		System.exit(0);
	}
	
	private static void cleanUp(){
		//delete temp jar and rename jar
		deleteFile(TEMP_JAR);
		deleteFile(RENAME_JAR);
		//start gui
		TspGui gui = new TspGui();
		gui.setVisible(true);
	}
	
	private static void downloadFile(String location, String newFile) throws IOException{
		URL url = new URL(location);
		File destination = new File(newFile);
		final Downloader downloader = new Downloader(url, destination);
		
		final JFrame frame = new JFrame();
		final JPanel panel = new JPanel();
		final JProgressBar bar = new JProgressBar();
		bar.setValue(0);
		bar.setMaximum(100);
		bar.setStringPainted(true);
		bar.setBorder(BorderFactory.createTitledBorder("Updating"));
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				downloader.stop();
			}
		});
		panel.add(bar);
		panel.add(btnCancel);
		frame.getContentPane().add(panel);
		frame.setLocation(PrefName.APP_X.getInt() + 20, PrefName.APP_Y.getInt() + 20);
		frame.pack();
		frame.setVisible(true);
		
		new Thread(downloader).start();
		
		while(downloader.progress() < 100 && !downloader.isFinished()){
			final int progress = downloader.progress();
			System.out.println(progress);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					bar.setValue(progress);
				}
			});			
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame.setVisible(false);
				frame.dispose();
			}
		});
//		ReadableByteChannel rbc = Channels.newChannel(url.openStream());
//		FileOutputStream fos = new FileOutputStream(newFile);
//		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
//		fos.close();
	}
	
	private static boolean renameFile(String oldName, String newName){
		File file = new File(oldName);
		File newFile = new File(newName);
		if(newFile.exists()) {
			return false;
		}
		try{
			Files.move(file, newFile);
			return true;
		} catch (IOException ioe){
			return false;
		}
	}
	
	private static boolean copyFile(String oldName, String newName){
		File file = new File(oldName);
		File newFile = new File(newName);
		if(newFile.exists()) {
			return false;
		}
		try{
			Files.copy(file, newFile);
			return true;
		} catch (IOException ioe){
			return false;
		}
	}
	
	private static void deleteFile(String fileName){
		File file = new File(fileName);
		file.delete();
	}
	
}
