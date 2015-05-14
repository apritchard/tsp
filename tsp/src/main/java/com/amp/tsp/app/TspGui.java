package com.amp.tsp.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.amp.tsp.prefs.PrefName;
import com.amp.tsp.prefs.PreferencesPanel;

public class TspGui extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private final static Logger logger = Logger.getLogger(TspGui.class.getName());
	private final static PreferencesPanel preferencesPanel = new PreferencesPanel();
	private final static AboutPanel aboutPanel = new AboutPanel();
	private final static TspPanel tspPanel = new TspPanel();
	
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
		addWindowListener(new TspWindowListener(this));
		
		setJMenuBar(buildMenuBar());
		getContentPane().add(tspPanel);
		
		theGui = this;
	}

	public static void main(String[] args) {
		TspGui gui = new TspGui();
		gui.setVisible(true);
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
		
		JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
		exitMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				theGui.dispose();
			}
		});
		
		fileMenu.add(preferencesMenuItem);		
		fileMenu.add(aboutMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);
		return menuBar;
	}
	
	
}
