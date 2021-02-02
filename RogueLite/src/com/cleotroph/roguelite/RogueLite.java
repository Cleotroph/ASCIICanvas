package com.cleotroph.roguelite;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;

public class RogueLite extends Canvas implements Runnable {

    private static int width, height;
    private Thread thread;
    private static int resMode = 0;
    private static boolean fullScreen = true;
    public static final RogueLite rogueLite = new RogueLite();
    private static long lastTickTime = 0;
    private static final int tickCap = 15;

    public RogueLite() {
        setSize(width, height);
        addKeyListener(Game.listenerInstance);
    }

    @Override
    public void run() {
        ASCIICanvas.start();
        while(true) {
            if (System.currentTimeMillis() > lastTickTime + (1000 / tickCap)) {
                Game.gameTick();
                lastTickTime = System.currentTimeMillis();
            }
        }
    }

    private void start() {
        thread = new Thread(this);
        thread.start();
    }

    public static void stop() {
        System.exit(0);
    }

    // Pre-launcher class

    public static class PreLauncher  {
        private static volatile boolean open = true;
        public static void main(String[] args){
            if(true) {
                int[] widths = {1920, 1280, 2560};
                int[] heights = {1080, 720, 1440};
                JFrame prelauncher = new JFrame("prelauncher");
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
            Frame frame = new Frame("RogueLite");
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    rogueLite.stop();
                    System.exit(0);
                }
            });
            frame.setBackground(new Color(0));
            if(fullScreen){
                frame.setSize(width, height);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setUndecorated(true);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.add(rogueLite);
            }else{
                frame.add(rogueLite);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setResizable(true);
                frame.setSize(width, height);
                frame.setVisible(true);
            }
            //TODO: fix canvas scaling (doesn't rescale canvas, only window)

            final ScreenBuffer screenBuffer = new RogueLite.ScreenBuffer();
            screenBuffer.start();
            rogueLite.start();
        }
    }



    // Screen buffer class

    public static class ScreenBuffer extends Thread {
        private static final int cwidth = 96;
        private static final int cheight = 54;
        private static volatile boolean bufferPointer;
        private static volatile char[] charBuffA = new char[cwidth * cheight];
        private static volatile int[] colorBuffA = new int[cwidth * cheight];
        private static volatile char[] charBuffB = new char[cwidth * cheight];
        private static volatile int[] colorBuffB = new int[cwidth * cheight];
        private static final Color[] colors = {
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
        private static BufferStrategy bs;
        private static long lastFrameTime = 0;
        private static final int frameCap = 15;
        private static final Font monoFont = new Font(Font.MONOSPACED, Font.BOLD, 20);

        @Override
        public void run() {
            while(true) {
                if (System.currentTimeMillis() > lastFrameTime + (1000 / frameCap)) {
                    Render();
                    lastFrameTime = System.currentTimeMillis();
                }
            }
        }

        private static void Render(){
            // Manage ASCII -> graphics buffer;
            char[] charBuff = bufferPointer ? charBuffA : charBuffB;
            int[] colorBuff = bufferPointer ? colorBuffA : colorBuffB;

            // Manage AWT graphic buffering (buffer frames internally)
            bs = rogueLite.getBufferStrategy();
            if (bs == null) {
                rogueLite.createBufferStrategy(2);
                bs = rogueLite.getBufferStrategy();
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
                    g.drawChars(charBuff, x + y * cwidth, 1, 20 * x, 20 * (y + 1));
                }
            }

            bs.show();
        }


        static void swapBuffer(){
            bufferPointer = !bufferPointer;
        }

        static char[] getCharBuffer(){
            return !bufferPointer ? charBuffA : charBuffB;
        }

        static int[] getColorBuffer(){
            return !bufferPointer ? colorBuffA : colorBuffB;
        }

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

        static void clear(){
            if(!bufferPointer){
                charBuffA = new char[cwidth * cheight];
            }else{
                charBuffB = new char[cwidth * cheight];
            }
        }
    }
}
