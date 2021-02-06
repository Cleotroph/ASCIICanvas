package com.cleotroph.asciicanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;

/**
 * Class to be extended by user. Includes abstract methods for ticking, rendering, and key events.
 */
public abstract class ASCIICanvas implements KeyListener {
    // manager reference
    private ASCIICanvasManager cManager;

    // canvas dimensions
    public int height;
    public int width;

    // References to buffers, must be refreshed every swap
    private char[] chars;
    private int[] colors;

    // current draw state
    private int color;
    private char brush;

    // sequencing threads
    private RenderThread renderThread;
    private TickThread tickThread;

    /**
     * Initialize the game with a default prelauncher.
     * @param name Window name.
     */
    public ASCIICanvas(String name){
        final Frame frame = new Frame(name);

        PreLauncher preLauncher = new PreLauncher(name);

        cManager = new ASCIICanvasManager(preLauncher.width, preLauncher.height, preLauncher.cx, preLauncher.cy, preLauncher.size, this, frame);

        frame.setBackground(new Color(0));

        if(preLauncher.fullScreen){
            frame.setSize(preLauncher.width, preLauncher.height);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(true);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.add(cManager);
        }else{
            frame.add(cManager);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(true);
            frame.setSize(preLauncher.width, preLauncher.height);
            frame.setVisible(true);
        }


        cManager.addKeyListener(this);
        height = cManager.screenBuffer.getHeight();
        width = cManager.screenBuffer.getWidth();
        color = 0;
        brush = ' ';
        renderThread = new RenderThread(this);
        tickThread = new TickThread(this);
    }

    /**
     * Initialize the game with a custom prelauncher.
     * @param preLauncher Custom prelaucher.
     * @param name Window name.
     */
    public ASCIICanvas(PreLauncher preLauncher, String name){
        final Frame frame = new Frame(name);

        cManager = new ASCIICanvasManager(preLauncher.width, preLauncher.height, preLauncher.cx, preLauncher.cy, preLauncher.size, this, frame);

        frame.setBackground(new Color(0));

        if(preLauncher.fullScreen){
            frame.setSize(preLauncher.width, preLauncher.height);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(true);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.add(cManager);
        }else{
            frame.add(cManager);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(true);
            frame.setSize(preLauncher.width, preLauncher.height);
            frame.setVisible(true);
        }


        cManager.addKeyListener(this);
        height = cManager.screenBuffer.getHeight();
        width = cManager.screenBuffer.getWidth();
        color = 0;
        brush = ' ';
        renderThread = new RenderThread(this);
        tickThread = new TickThread(this);
    }

    /**
     * Initialize the component without a prelauncher.
     * @param x pixel width
     * @param y pixel height
     * @param cx character width
     * @param cy character height
     * @param size size of characters.
     * @param frame the frame to add the element to.
     */
    public ASCIICanvas(int x, int y, int cx, int cy, int size, Frame frame){
        cManager = new ASCIICanvasManager(x, y, cx, cy, size, this, frame);
        cManager.addKeyListener(this);
        height = cManager.screenBuffer.getHeight();
        width = cManager.screenBuffer.getWidth();
        color = 0;
        brush = ' ';
        renderThread = new RenderThread(this);
        tickThread = new TickThread(this);
    }

    /**
     * Called to start rendering + ticking
     */
    public void start(){
        load();
        cManager.start();
        renderThread.start();
        tickThread.start();
    }

    // Called every frame after the canvas is drawn to. Swaps the buffer and refreshes buffer references.
    private void onFrame(){
        cManager.screenBuffer.swapBuffer();
        chars = cManager.screenBuffer.getCharBuffer();
        colors = cManager.screenBuffer.getColorBuffer();
    }

    private void onRender(){
        onFrame();
        render();
    }

    private void onTick(){
        tick();
    }

    /**
     * Called to stop the program/element safely.
     */
    public void exit(){
        cManager.stop();
    }

    /**
     * Set the frame rate.
     * @param rate FPS
     */
    public void frameRate(int rate){
        renderThread.setTickSpeed(rate);
    }

    /**
     * Set the tick rate.
     * @param rate TPS
     */
    public void tickRate(int rate){
        tickThread.setTickSpeed(rate);
    }

    //--------------------------- Draw Functions ------------------------------

    /**
     * <h1>try to avoid using this.</h1>
     * syncs buffers for when clear is not being called every frame.
     * should be used if there is any movement between frames.
     * call only at the end of the draw loop.
     * warning, this is volatile! could cause screen tearing if called often.
     */
    public void syncBuffer(){
        cManager.screenBuffer.syncBuffer();
    }

    /**
     * Wipes the buffer entirely replacing with empty chars.
     * Normally this should be called on the start of every frame.
     */
    public void clear(){
        cManager.screenBuffer.clear();
        chars = cManager.screenBuffer.getCharBuffer();
    }

    /**
     * set brush color for draw operations.
     * @param color_in index of color.
     */
    public void setColor(int color_in){
        color = color_in;
    }

    /**
     * set the brush character for draw operations.
     * @param brush_in character to draw.
     */
    public void setBrush(char brush_in){
        brush = brush_in;
    }

    /**
     * Draws a vertical or horizontal line using the brush settings.
     * @param x x position of top/left.
     * @param y y position of top/left.
     * @param l length, extends towards positive x/y depending on orientation.
     * @param vertical orientation of the line, true for vertical, false for horizontal.
     */
    public void line(int x, int y, int l, boolean vertical){
        if(vertical){
            for(int iy = 0; iy < l;  iy++){
                int pos = x + (y + iy) * width;
                chars[pos] = brush;
                colors[pos] = color;
            }
        }else{
            for(int ix = 0; ix < l;  ix++){
                int pos = (x + ix) + y * width;
                chars[pos] = brush;
                colors[pos] = color;
            }
        }
    }

    /**
     * Fills a region with the brush settings.
     * @param x x position of top/left.
     * @param y y position of top/left.
     * @param w width of rect.
     * @param h height of rect.
     * @param filled whether or not the interior is filled (false for only the boarder).
     */
    public void rect(int x, int y, int w, int h, boolean filled){
        if(filled){
            for(int iy = 0; iy < h; iy++){
                for(int ix = 0; ix < w; ix++){
                    int pos = ix + (iy + y) * width + x;
                    chars[pos] = brush;
                    colors[pos] = color;
                }
            }
        }else{
            for(int ix = 0; ix < w; ix++){
                int pos = x + ix + y * width;
                chars[pos] = brush;
                colors[pos] = color;
                pos = x + ix + (y + (h - 1)) * width;
                chars[pos] = brush;
                colors[pos] = color;
            }
            for(int iy = 1; iy < h - 1; iy++){
                int pos = x + (iy + y) * width;
                chars[pos] = brush;
                colors[pos] = color;
                pos = x + (w - 1) + (iy + y) * width;
                chars[pos] = brush;
                colors[pos] = color;
            }
        }
    }

    /**
     * Paints a single point with the brush settings.
     * @param x x of point.
     * @param y y of point.
     */
    public void point(int x, int y){
        int pos = x + y * width;
        chars[pos] = brush;
        colors[pos] = color;
    }

    /**
     * draws an unfilled rect using ASCII border chars
     * @param x x position of top/left.
     * @param y y position of top/left.
     * @param w width of rect.
     * @param h height of rect.
     */
    public void drawPerimieter(int x, int y, int w, int h){
        brush = '═';
        for(int ix = 1; ix < w - 1; ix++){
            int pos = x + ix + y * width;
            chars[pos] = brush;
            colors[pos] = color;
            pos = x + ix + (y + (h - 1)) * width;
            chars[pos] = brush;
            colors[pos] = color;
        }
        brush = '║';
        for(int iy = 1; iy < h - 1; iy++){
            int pos = x + (iy + y) * width;
            chars[pos] = brush;
            colors[pos] = color;
            pos = x + (w - 1) + (iy + y) * width;
            chars[pos] = brush;
            colors[pos] = color;
        }
        brush = '╔';
        int pos = x + y * width;
        chars[pos] = brush;
        colors[pos] = color;
        brush = '╝';
        pos = (x + w - 1) + (y + h - 1) * width;
        chars[pos] = brush;
        colors[pos] = color;
        brush = '╚';
        pos = x + (y + h - 1) * width;
        chars[pos] = brush;
        colors[pos] = color;
        brush = '╗';
        pos = (x + w - 1) + y * width;
        chars[pos] = brush;
        colors[pos] = color;
    }

    //--------------------------- Sequencing ------------------------------

    /**
     * Called once on init
     */
    public abstract void load();

    /**
     * Called once when the window is closed by an in game command
     */
    public abstract void save();

    /**
     * Called once per frame, rate can be modified by calling frameRate();
     */
    public abstract void render();

    /**
     * Called once per game tick, rate can be modified by calling tickCap();
     */
    public abstract void tick();

    private static class RenderThread extends Thread{
        private ASCIICanvas parent;
        private int tickSpeed;
        private long lastTick;
        public RenderThread(ASCIICanvas parent){
            this.parent = parent;
            tickSpeed = 15;
        }

        void setTickSpeed(int tickSpeed){
            this.tickSpeed = tickSpeed;
        }

        @Override
        public void run() {
            while(true) {
                if (System.currentTimeMillis() > lastTick + (1000 / tickSpeed)) {
                    parent.onRender();
                    lastTick = System.currentTimeMillis();
                }
            }
        }
    }

    private static class TickThread extends Thread{
        private ASCIICanvas parent;
        private int tickSpeed;
        private long lastTick;
        public TickThread(ASCIICanvas parent){
            this.parent = parent;
            tickSpeed = 15;
        }

        void setTickSpeed(int tickSpeed){
            this.tickSpeed = tickSpeed;
        }

        @Override
        public void run() {
            while(true) {
                if (System.currentTimeMillis() > lastTick + (1000 / tickSpeed)) {
                    parent.onTick();
                    lastTick = System.currentTimeMillis();
                }
            }
        }
    }
}
