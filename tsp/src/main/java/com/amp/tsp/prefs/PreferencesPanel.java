package com.amp.tsp.prefs;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.PlainDocument;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import com.amp.tsp.app.IntegerFilter;
import com.google.common.base.CaseFormat;

public class PreferencesPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(PreferencesPanel.class);

	private static final Preferences prefs = Preferences.userNodeForPackage(PreferencesPanel.class);
	
	private Map<JTextField, PrefName> textFields = new HashMap<>();
	private Map<JTextField, PrefName> integerFields = new HashMap<>();
	private Map<JCheckBox, PrefName> checkBoxes = new HashMap<>();
	private Map<JComboBox<String>, PrefName> comboBoxes = new HashMap<>();
	
	JLabel statusLabel = new JLabel("");
	
	private List<PreferenceListener> listeners = new ArrayList<>();

	public PreferencesPanel(){
		setLayout(new MigLayout());
		
		for(PrefName prefName : PrefName.values()){
			if(!prefName.isEditable()){
				continue;
			}
			switch(prefName.getType()){
			case BOOLEAN:
				addCheckbox(prefName);
				break;
			case DIRECTORY:
				addDirectory(prefName, true);
				break;
			case INTEGER:
				addInteger(prefName);
				break;
			case STRING:
				addString(prefName);
				break;
			case ENUM_SINGLE:
				addEnum(prefName);
				break;
			case FILE:
				addDirectory(prefName, false);
				break;
			default:
				logger.error("Unknown type for " + prefName + ": " + prefName.getType());
			}
		}
		
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<PrefName> changedPrefs = new ArrayList<>();
				for(Entry<JTextField, PrefName> entry : textFields.entrySet()){
					PrefName p = entry.getValue();
					String oldT = prefs.get(p.path(), p.defaultString());
					String newT = entry.getKey().getText();
					if(!oldT.equals(newT)){
						prefs.put(p.path(), newT);
						changedPrefs.add(p);
					}
				}
				
				for(Entry<JTextField, PrefName> entry : integerFields.entrySet()){
					PrefName p = entry.getValue();
					int oldI = prefs.getInt(p.path(), p.defaultInt());
					int newI = entry.getKey().getText().isEmpty() ? 
							0 : Integer.parseInt(entry.getKey().getText());
					if(!(oldI == newI)){
						prefs.putInt(p.path(), newI);
						changedPrefs.add(p);
					}
				}
				
				for(Entry<JCheckBox, PrefName> entry : checkBoxes.entrySet()){
					PrefName p = entry.getValue();
					boolean oldB = prefs.getBoolean(p.path(), p.defaultBoolean());
					boolean newB = entry.getKey().isSelected();
					if(!(oldB == newB)){
						prefs.putBoolean(p.path(), newB);
						changedPrefs.add(p);
					}
				}
				
				for(Entry<JComboBox<String>, PrefName> entry : comboBoxes.entrySet()){
					PrefName p = entry.getValue();
					String oldT = prefs.get(p.path(), p.defaultString());
					String newT = entry.getKey().getSelectedItem().toString();
					if(!oldT.equals(newT)){
						prefs.put(p.path(), newT);
						changedPrefs.add(p);
					}
				}
				
				statusLabel.setText("Preferences saved.");
				for(PreferenceListener listener : listeners){
					listener.notify(changedPrefs);
				}
			}
		});
		add(saveButton);
		add(statusLabel);
	}
	
	public void addListener(PreferenceListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(PreferenceListener listener){
		listeners.remove(listener);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addEnum(PrefName prefName) {
		String enumClassName = prefToJavaText(prefName.toString());
		JComboBox<String> enumBox = new JComboBox<String>();
		try {

			Class<Enum> enumClass = (Class<Enum>)Class.forName("com.amp.tsp.prefs." + enumClassName);
			Method m = enumClass.getDeclaredMethod("values");
			Enum[] result = (Enum[])m.invoke(enumClass);
			for(Enum e : result){
				enumBox.addItem(e.toString());
			}
			enumBox.setSelectedItem(prefs.get(prefName.path(), prefName.defaultString()));
			add(new JLabel(prefName.path()));
			add(enumBox, "w 60%, wrap");
			comboBoxes.put(enumBox, prefName);
		} catch (Exception e){
			logger.error("Invalid path / enum match for " + prefName + " in PrefName", e);
		}
		
	}
	
	private String prefToJavaText(String text){
		return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, text);
	}

	private void addString(PrefName prefName) {
		add(new JLabel(prefName.path()));
		JTextField textField = new JTextField();
		textField.setText(prefs.get(prefName.path(), prefName.defaultString()));
		add(textField, "width 60%, wrap");
		textFields.put(textField, prefName);
	}

	private void addCheckbox(PrefName prefName) {
		add(new JLabel(prefName.path()));
		JCheckBox ckbx = new JCheckBox();
		ckbx.setSelected(prefs.getBoolean(prefName.path(), prefName.defaultBoolean()));
		add(ckbx, "wrap");
		checkBoxes.put(ckbx, prefName);
	}
	
	private void addInteger(PrefName prefName){
		add(new JLabel(prefName.path()));
		JTextField textField = new JTextField();
		((PlainDocument)textField.getDocument()).setDocumentFilter(new IntegerFilter(0, Integer.MAX_VALUE));
		textField.setText("" + prefs.getInt(prefName.path(),  prefName.defaultInt()));
		add(textField, "width 60%, wrap");
		integerFields.put(textField, prefName);
	}
	
	private void addDirectory(final PrefName prefName, final boolean directoryOnly){
		add(new JLabel(prefName.path()));
		
		final JTextField textField = new JTextField();
		final Component chooserParent = this;
		
		textField.setText(prefs.get(prefName.path(), prefName.defaultString()));
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooseDirectory(textField, prefName, chooserParent, directoryOnly);
			}
		});		
		add(textField, "width 160");
		
		JButton browseButton = new JButton("Browse");
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooseDirectory(textField, prefName, chooserParent, directoryOnly);
			}
		});
		add(browseButton, "wrap, push");
		textFields.put(textField, prefName);
	}
	
	private void chooseDirectory(JTextField textField, PrefName prefName, Component chooserParent, boolean directoryOnly){
		JFileChooser chooser = new JFileChooser();
		String previousDirectory = textField.getText();
		if(!previousDirectory.isEmpty()){
			chooser.setCurrentDirectory(new File(previousDirectory));
		} else {
			chooser.setCurrentDirectory(new File("."));
		}
		if(directoryOnly){
			chooser.setDialogTitle("Select " + prefName.path() + " Directory");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);
		} else {
			chooser.setDialogTitle("Select " + prefName.path() + " File");
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		
		if (chooser.showOpenDialog(chooserParent) == JFileChooser.APPROVE_OPTION){
			textField.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}
	
}
