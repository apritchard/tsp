package com.amp.tsp.app;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.miginfocom.swing.MigLayout;

public class AboutPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	private final String GITHUB_LINK = "https://github.com/apritchard/tsp";
	
	public AboutPanel(){
		setLayout(new MigLayout());
		
		StringBuilder sb = new StringBuilder();
		sb.append("I wrote this program fun fun and to learn about solutions to the traveling salesman problem.");
		sb.append(" It's a work in progress, and if you're interested in updating to the newest version, the");
		sb.append(" fastest way to do that is by visiting <a href='" + GITHUB_LINK + "'>github page</a>.");
		sb.append(" If you find a bug or have feedback, my contact information is available on my github profile.");
		
		JTextPane tpAbout = new JTextPane();
	    
	    tpAbout.setOpaque(false);
	    tpAbout.setEditable(false);
	    tpAbout.setContentType("text/html");
	    tpAbout.setText(sb.toString());
	    tpAbout.setBackground(UIManager.getColor("Label.background"));
	    tpAbout.setFont(UIManager.getFont("Label.font"));
	    tpAbout.setBorder(UIManager.getBorder("Label.border"));
	    tpAbout.setToolTipText(GITHUB_LINK);

	    tpAbout.addHyperlinkListener(new HyperlinkListener() {
			
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (Exception e2){
						JOptionPane.showMessageDialog(null, "Oops! Couldn't open your browser. Copied URL to clipboard.");
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(e.getURL().toString()), null);
					}
				}
				
			}
		});
	    
	    add(tpAbout, "w 400");
	}
	
}
