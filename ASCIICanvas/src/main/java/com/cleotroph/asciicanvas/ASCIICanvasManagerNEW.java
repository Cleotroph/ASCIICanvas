package com.cleotroph.asciicanvas;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

public class ASCIICanvasManagerNEW extends Canvas {

    private int width, height;
    ScreenBuffer screenBuffer;
    private ASCIICanvasNEW parent;

    public ASCIICanvasManagerNEW(int x, int y, int cx, int cy, int size, ASCIICanvasNEW parent, Frame frame){
        this.parent = parent;
        width = x;
        height = y;
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                stop();
            }
        });
        frame.add(this);
        setSize(width, height);
        screenBuffer = new ScreenBuffer(cx, cy, size, this);
    }

    void start(){
        screenBuffer.start();
    }

    void stop() {
        parent.save();
        System.exit(0);
    }

    public static class ScreenBuffer extends Thread {
        // width in character
        private final int cwidth = 96;

        // height in characters
        private final int cheight = 54;

        // boolean to specify the buffer which is in read mode (true: A = read, B = write)
        private volatile boolean bufferPointer;

        // buffers for ascii canvas data
        private volatile char[] charBuffA;
        private volatile int[] colorBuffA;
        private volatile char[] charBuffB;
        private volatile int[] colorBuffB;

        // Color buffer, default populated with some useful colors
        private Color[] colors = {
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
        private BufferStrategy bs;

        // Frame rate limiting vars
        private long lastFrameTime = 0;
        private int frameCap = 15;

        // ASCII font
        private Font monoFont;

        // width of characters
        private int padding;

        private ASCIICanvasManagerNEW cManager;

        public ScreenBuffer(int x, int y, int cSize, ASCIICanvasManagerNEW cManager) {
            this.cManager = cManager;
            if (monoFont == null) {
                padding = cSize;
                monoFont = new Font(Font.MONOSPACED, Font.BOLD, cSize);
            }
            charBuffA = new char[cwidth * cheight];
            charBuffB = new char[cwidth * cheight];
            colorBuffA = new int[cwidth * cheight];
            colorBuffB = new int[cwidth * cheight];
        }

        /**
         * Can be called to overwrite the default color buffer with a new color map.
         * @param new_colors array of Colors in order of their id.
         */
        public void overwriteColors(Color[] new_colors){
            colors = new_colors;
        }

        /**
         * Called to set the actual frame rate of the window refresh rather than the canvas refresh.
         * @param fps number of times a frame can be drawn per second.
         */
        public void setFrameRate(int fps){
            frameCap = fps;
        }

        // thread management, calls Render() once per frame
        @Override
        public void run() {
            while(true) {
                if (System.currentTimeMillis() > lastFrameTime + (1000 / frameCap)) {
                    Render();
                    lastFrameTime = System.currentTimeMillis();
                }
            }
        }

        // Called on render of canvas to screen (distinct from canvas render which generates the canvas)
        private void Render(){
            // Manage ASCII -> graphics buffer; Don't want to read from the active write buffer.
            char[] charBuff = bufferPointer ? charBuffA : charBuffB;
            int[] colorBuff = bufferPointer ? colorBuffA : colorBuffB;

            // Manage AWT graphic buffering (buffer frames internally)
            bs = cManager.getBufferStrategy();
            if (bs == null) {
                cManager.createBufferStrategy(2);
                bs = cManager.getBufferStrategy();
            }
            Graphics g = bs.getDrawGraphics();
            g.setFont(monoFont);

            // Draw BG
            g.setColor(new Color(0));
            g.fillRect(0, 0, cManager.width, cManager.height);

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
        void swapBuffer(){
            bufferPointer = !bufferPointer;
        }

        // Obtain references to buffers
        char[] getCharBuffer(){
            return !bufferPointer ? charBuffA : charBuffB;
        }

        int[] getColorBuffer(){
            return !bufferPointer ? colorBuffA : colorBuffB;
        }

        // Get canvas dimensions
        int getWidth(){
            return cwidth;
        }

        int getHeight(){
            return cheight;
        }

        // Synchronizes buffer for when clear is not called; MUST BE CALLED AT THE END OF THE DRAW LOOP
        void syncBuffer(){
            if(bufferPointer){
                charBuffA = charBuffB.clone();
                colorBuffA = colorBuffB.clone();
            }else{
                charBuffB = charBuffA.clone();
                colorBuffB = colorBuffA.clone();
            }
        }

        // Wipes the buffer that is currently in write mode
        void clear(){
            if(!bufferPointer){
                charBuffA = new char[cwidth * cheight];
            }else{
                charBuffB = new char[cwidth * cheight];
            }
        }
    }

}
