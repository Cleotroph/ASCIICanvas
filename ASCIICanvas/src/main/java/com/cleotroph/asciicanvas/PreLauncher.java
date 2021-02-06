package com.cleotroph.asciicanvas;

import javax.swing.*;
import java.awt.*;

/**
 * Class to be initialized and passed in when launching in custom resolutions. Initializes with default settings. config
 * lengths must match.
 */
public class PreLauncher {
    int width, height, size;
    int cx, cy;
    private int[] widths = {1920, 1280, 2560};
    private int[] heights = {1080, 720, 1440};
    private int[] sizes = {20, 14, 28};
    boolean fullScreen = true;
    private int resMode = 0;
    private volatile boolean open = true;
    public PreLauncher(String name){
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
    }

    /**
     * Set character width of canvas
     * @param cx width
     */
    public void setCx(int cx) {
        this.cx = cx;
    }

    /**
     * Set character height of canvas
     * @param cy height
     */
    public void setCy(int cy) {
        this.cy = cy;
    }

    /**
     * Set character size options (correlate to resolution)
     * Should be equal to width / cx or rendering will not fill screen horizontally
     * @param sizes options of sizes
     */
    public void setSizes(int[] sizes) {
        this.sizes = sizes;
    }

    /**
     * set pixel height options.
     * @param heights options for heights
     */
    public void setHeights(int[] heights) {
        this.heights = heights;
    }

    /**
     * set pixel width options.
     * @param widths options for widths
     */
    public void setWidths(int[] widths) {
        this.widths = widths;
    }

}
