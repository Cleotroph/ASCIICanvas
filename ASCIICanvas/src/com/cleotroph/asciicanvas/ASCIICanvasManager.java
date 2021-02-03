package com.cleotroph.asciicanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;

public class ASCIICanvasManager extends Canvas implements Runnable {

    // size of canvas element (distinct from ascii canvas dimensions)
    private static int width, height;

    // Thread for calling game tick
    private Thread thread;

    // Denotes the type of window specified in the prelauncher
    private static int resMode = 0;

    // Denotes window mode of game
    private static boolean fullScreen = true;

    // Instance reference for runnable
    private static final ASCIICanvasManager ASCII_CANVAS_MANAGER = new ASCIICanvasManager();

    // Tick speed management
    private static long lastTickTime = 0;
    private static int tickCap = 15;

    // Reference to user defined game class
    private static Game game;

    // Construct window
    private ASCIICanvasManager() {
        setSize(width, height);
    }

    /**
     * Used to set the tick speed of the game.
     * @param ticks_per_sec number of ticks per second to cap at.
     */
    public void setTickCap(int ticks_per_sec){
        tickCap = ticks_per_sec;
    }

    // calls gameTick every n ms defined by tickCap
    @Override
    public void run() {
        ASCIICanvas.start();
        while(true) {
            if (System.currentTimeMillis() > lastTickTime + (1000 / tickCap)) {
                game.gameTick();
                lastTickTime = System.currentTimeMillis();
            }
        }
    }

    // initializes the thread in charge of calling gameTick.
    private void start() {
        game.load();
        thread = new Thread(this);
        thread.start();
    }

    /**
     * stops the program after calling save on game. Should be called when the game is meant to close.
      */
    public static void stop() {
        game.save();
        System.exit(0);
    }

    // used to call render in the user defined game class
    static void callRender(ASCIICanvas c){
        game.render(c);
    }

    /* Pre-launcher class
     * manages the window opened prior to the game opening for setting resolution and window mode*/

    public static class PreLauncher  {

        private static volatile boolean open = true;
        static void Launch(String name, Game gameObj){
            game = gameObj;
            ASCII_CANVAS_MANAGER.addKeyListener(game);
            if(true) {
                int[] widths = {1920, 1280, 2560};
                int[] heights = {1080, 720, 1440};
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
                    PreLauncher.open = false;
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
            }
            Frame frame = new Frame(name);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    stop();
                }
            });
            frame.setBackground(new Color(0));
            if(fullScreen){
                frame.setSize(width, height);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setUndecorated(true);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.add(ASCII_CANVAS_MANAGER);
            }else{
                frame.add(ASCII_CANVAS_MANAGER);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setResizable(true);
                frame.setSize(width, height);
                frame.setVisible(true);
            }

            final ScreenBuffer screenBuffer = new ASCIICanvasManager.ScreenBuffer();
            screenBuffer.start();
            ASCII_CANVAS_MANAGER.start();
        }
    }

    // Screen buffer class

    public static class ScreenBuffer extends Thread {
        // width in character
        private static final int cwidth = 96;

        // height in characters
        private static final int cheight = 54;

        // boolean to specify the buffer which is in read mode (true: A = read, B = write)
        private static volatile boolean bufferPointer;

        // buffers for ascii canvas data
        private static volatile char[] charBuffA = new char[cwidth * cheight];
        private static volatile int[] colorBuffA = new int[cwidth * cheight];
        private static volatile char[] charBuffB = new char[cwidth * cheight];
        private static volatile int[] colorBuffB = new int[cwidth * cheight];

        // Color buffer, default populated with some useful colors
        private static Color[] colors = {
                new Color(0xFFFFFF),
                new Color(0xFF0000),
                new Color(0x00FF00),
                new Color(0x0000FF),
                new Color(0xFFFF00),
                new Color(0x00FFFF),
                new Color(0xFF00FF),
                new Color(0xC0C0C0),
                new Color(0x808080),
                new Color(0x800000),
                new Color(0xC0C000),
                new Color(0x008000),
                new Color(0x800080),
                new Color(0x008080),
                new Color(0x000080),
                new Color(0x333333)
        };

        // Used for AWT rendering after generating image from ascii canvas
        private static BufferStrategy bs;

        // Frame rate limiting vars
        private static long lastFrameTime = 0;
        private static int frameCap = 15;

        // ASCII font
        private static Font monoFont;

        // width of characters
        private static int padding;

        /**
         * Can be called to overwrite the default color buffer with a new color map.
         * @param new_colors array of Colors in order of their id.
         */
        public static void overwriteColors(Color[] new_colors){
            colors = new_colors;
        }

        /**
         * Called to set the actual frame rate of the window refresh rather than the canvas refresh.
         * @param fps number of times a frame can be drawn per second.
         */
        public static void setFrameRate(int fps){
            frameCap = fps;
        }

        // thread management, calls Render() once per frame
        @Override
        public void run() {
            if(monoFont == null){
                int[] fonts = {20, 14, 28};
                padding = fonts[resMode];
                monoFont = new Font(Font.MONOSPACED, Font.BOLD, fonts[resMode]);
            }
            while(true) {
                if (System.currentTimeMillis() > lastFrameTime + (1000 / frameCap)) {
                    Render();
                    lastFrameTime = System.currentTimeMillis();
                }
            }
        }

        // Called on render of canvas to screen (distinct from canvas render which generates the canvas)
        private static void Render(){
            // Manage ASCII -> graphics buffer; Don't want to read from the active write buffer.
            char[] charBuff = bufferPointer ? charBuffA : charBuffB;
            int[] colorBuff = bufferPointer ? colorBuffA : colorBuffB;

            // Manage AWT graphic buffering (buffer frames internally)
            bs = ASCII_CANVAS_MANAGER.getBufferStrategy();
            if (bs == null) {
                ASCII_CANVAS_MANAGER.createBufferStrategy(2);
                bs = ASCII_CANVAS_MANAGER.getBufferStrategy();
                Graphics g = bs.getDrawGraphics();
            }
            Graphics g = bs.getDrawGraphics();
            g.setFont(monoFont);

            // Draw BG
            g.setColor(new Color(0));
            g.fillRect(0, 0, width, height);

            // Draw Chars
            for (int y = 0; y < cheight; y++) {
                for (int x = 0; x < cwidth; x++) {
                    g.setColor(colors[colorBuff[x + y * cwidth]]);
                    g.drawChars(charBuff, x + y * cwidth, 1, padding * x, padding * (y + 1));
                }
            }

            bs.show();
        }

        // Swaps the read and write buffers with a pointer swap, any references to buffers must be refreshed!
        static void swapBuffer(){
            bufferPointer = !bufferPointer;
        }

        // Obtain references to buffers
        static char[] getCharBuffer(){
            return !bufferPointer ? charBuffA : charBuffB;
        }

        static int[] getColorBuffer(){
            return !bufferPointer ? colorBuffA : colorBuffB;
        }

        // Get canvas dimensions
        static int getWidth(){
            return cwidth;
        }

        static int getHeight(){
            return cheight;
        }

        // Synchronizes buffer for when clear is not called; MUST BE CALLED AT THE END OF THE DRAW LOOP
        static void syncBuffer(){
            if(bufferPointer){
                charBuffA = charBuffB.clone();
                colorBuffA = colorBuffB.clone();
            }else{
                charBuffB = charBuffA.clone();
                colorBuffB = colorBuffA.clone();
            }
        }

        // Wipes the buffer that is currently in write mode
        static void clear(){
            if(!bufferPointer){
                charBuffA = new char[cwidth * cheight];
            }else{
                charBuffB = new char[cwidth * cheight];
            }
        }
    }
}
