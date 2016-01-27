package com.amp.tsp.capture;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import com.amp.tsp.app.TspWindowListener;
import com.amp.tsp.prefs.PrefName;

import net.miginfocom.swing.MigLayout;

public class InstructionFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	public InstructionFrame(){
		int appX = PrefName.INSTRUCTION_X.getInt();
		int appY = PrefName.INSTRUCTION_Y.getInt();
		int appWidth = PrefName.INSTRUCTION_WIDTH.getInt();
		int appHeight = PrefName.INSTRUCTION_HEIGHT.getInt();
		
		setSize(appWidth, appHeight);
		setLocation(appX, appY);
		setTitle("Instructions");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		addWindowListener(new TspWindowListener(this, PrefName.INSTRUCTION_X, PrefName.INSTRUCTION_Y, PrefName.INSTRUCTION_WIDTH, PrefName.INSTRUCTION_HEIGHT));
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout());
		String instructions = "Click on the greyed out screen to place nodes." + 
				"\nLeft click\tplace node on the map"
				+ "\nShift-click\tNode required at beginning of path (fast)"
				+ "\nCtrl-click\tNode required at end of path (slow)"
				+ "\nAlt-click\tNode may be warped to (edit cost in preferences)"
				+ "\nRight-click\tSolve graph (press again to save solved graph)"
				+ "\nEscape\tCancel map input"
				+ "\nCtrl-Alt-Shift-click\n\tSwitch map screen"				
				
				+ "\n\nNodes may be either beginning or ending nodes and also a warp node (use alt-shift or alt-ctrl), but"
				+ " cannot be both beginning and ending nodes at the same time. Graphs containing more than 25 nodes may"
				+ " not finish in a reasonable amount of time. Ending nodes decrease performance.";
		
		
		JTextArea taInstructions = new JTextArea();
	    taInstructions.setText(instructions);
	    taInstructions.setWrapStyleWord(true);
	    taInstructions.setLineWrap(true);
	    taInstructions.setOpaque(false);
	    taInstructions.setEditable(false);
	    taInstructions.setBackground(UIManager.getColor("Label.background"));
	    taInstructions.setFont(UIManager.getFont("Label.font"));
	    taInstructions.setBorder(UIManager.getBorder("Label.border"));
	    panel.add(taInstructions, "push, grow");
	    getContentPane().add(panel);
	}
}
