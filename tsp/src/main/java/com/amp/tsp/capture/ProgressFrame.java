package com.amp.tsp.capture;

import javax.swing.*;
import java.awt.*;

/**
 * Created by sprco on 2/3/2017.
 */
public class ProgressFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JProgressBar progressBar;
    public ProgressFrame(int max){
        progressBar = new JProgressBar(0,max);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(progressBar);
        getContentPane().add(panel);
        setUndecorated(true);
        GraphicsDevice gd = DrawUtils.getGraphicsDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();
        setLocation(gd.getDefaultConfiguration().getBounds().x + (width/4), (height/2)-50);
        setSize(width / 2, 100);
        setVisible(true);
    }

    public void setProgress(int progress){
        progressBar.setValue(progress);
        System.out.println("progress = " + progress);
        repaint();
    }
}
