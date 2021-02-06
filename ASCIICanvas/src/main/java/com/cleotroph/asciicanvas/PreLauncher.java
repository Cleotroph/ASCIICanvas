package com.cleotroph.asciicanvas;

import javax.swing.*;
import java.awt.*;

public class PreLauncher {
    int width, height, size;
    int cx, cy;
    int[] widths = {1920, 1280, 2560};
    int[] heights = {1080, 720, 1440};
    int[] sizes = {20, 14, 28};
    boolean fullScreen;
    private int resMode = 0;
    private volatile boolean open = true;
    public PreLauncher(Frame frame, Canvas component, String name){
        cx = 96;
        cy = 54;
        JFrame prelauncher = new JFrame(name);
        prelauncher.getContentPane().setLayout(null);
        prelauncher.setSize(300, 300);
        prelauncher.setLocation((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2) - 150,
                (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2) - 150);

        JButton resButton = new JButton("Resolution mode: " + widths[resMode] + " : " + heights[resMode]);
        resButton.addActionListener(actionEvent -> {
            resMode++;
            resMode %= widths.length;
            resButton.setText("Resolution mode: " + widths[resMode] + " : " + heights[resMode]);
        });
        resButton.setBounds(10, 10, 265, 110);

        JButton fullscreenButton = new JButton("Fullscreen");
        fullscreenButton.addActionListener(actionEvent -> {
            fullScreen = !fullScreen;
            fullscreenButton.setText(fullScreen ? "Fullscreen" : "Windowed");
        });
        fullscreenButton.setBounds(10, 130, 130, 110);

        JButton startButton = new JButton("Start game");
        startButton.addActionListener(actionEvent -> {
            open = false;
        });
        startButton.setBounds(150, 130, 125, 110);

        prelauncher.add(resButton);
        prelauncher.add(startButton);
        prelauncher.add(fullscreenButton);

        prelauncher.setVisible(true);
        while (open) { }
        prelauncher.dispose();
        width = widths[resMode];
        height = heights[resMode];
        size = sizes[resMode];

        frame = new Frame(name);
        frame.setBackground(new Color(0));
    }

    public void setCx(int cx) {
        this.cx = cx;
    }

    public void setCy(int cy) {
        this.cy = cy;
    }

    public void setSizes(int[] sizes) {
        this.sizes = sizes;
    }

    public void setHeights(int[] heights) {
        this.heights = heights;
    }

    public void setWidths(int[] widths) {
        this.widths = widths;
    }

}
